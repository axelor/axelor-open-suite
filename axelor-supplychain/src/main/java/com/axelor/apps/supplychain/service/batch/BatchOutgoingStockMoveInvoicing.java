/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.service.batch;

import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.db.SupplychainBatch;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.StockMoveInvoiceService;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.List;
import javax.persistence.TypedQuery;

public class BatchOutgoingStockMoveInvoicing extends BatchStrategy {

  protected StockMoveInvoiceService stockMoveInvoiceService;

  @Inject
  public BatchOutgoingStockMoveInvoicing(StockMoveInvoiceService stockMoveInvoiceService) {
    this.stockMoveInvoiceService = stockMoveInvoiceService;
  }

  @Override
  protected void process() {
    SupplychainBatch supplychainBatch = batch.getSupplychainBatch();
    List<Long> anomalyList = Lists.newArrayList(0L);

    TypedQuery<StockMove> query =
        JPA.em()
            .createQuery(
                "SELECT self FROM StockMove self "
                    + "WHERE self.statusSelect = :statusSelect "
                    + "AND self.typeSelect = :typeSelect "
                    + "AND self.invoicingStatusSelect !=  :invoicingStatusSelect "
                    + "AND (SELECT count(invoice.id) FROM Invoice invoice WHERE invoice.statusSelect != :invoiceStatusCanceled AND invoice MEMBER OF self.invoiceSet) = 0"
                    + "AND self.id NOT IN (:anomalyList) "
                    + "AND self.partner.id NOT IN ("
                    + Beans.get(BlockingService.class)
                        .listOfBlockedPartner(
                            supplychainBatch.getCompany(), BlockingRepository.INVOICING_BLOCKING)
                    + ") "
                    + "AND :batch NOT MEMBER OF self.batchSet "
                    + "ORDER BY self.id",
                StockMove.class)
            .setParameter("statusSelect", StockMoveRepository.STATUS_REALIZED)
            .setParameter("typeSelect", StockMoveRepository.TYPE_OUTGOING)
            .setParameter("invoiceStatusCanceled", InvoiceRepository.STATUS_CANCELED)
            .setParameter("invoicingStatusSelect", StockMoveRepository.STATUS_DELAYED_INVOICE)
            .setParameter("anomalyList", anomalyList)
            .setParameter("batch", batch)
            .setMaxResults(FETCH_LIMIT);

    List<StockMove> stockMoveList;
    while (!(stockMoveList = query.getResultList()).isEmpty()) {
      for (StockMove stockMove : stockMoveList) {
        try {
          stockMoveInvoiceService.createInvoiceFromStockMove(stockMove, null);
          updateStockMove(stockMove);
        } catch (Exception e) {
          incrementAnomaly();
          anomalyList.add(stockMove.getId());
          query.setParameter("anomalyList", anomalyList);
          TraceBackService.trace(e, ExceptionOriginRepository.INVOICE_ORIGIN, batch.getId());
          break;
        }
      }
      JPA.clear();
    }
  }

  @Override
  protected void stop() {
    StringBuilder sb = new StringBuilder();
    sb.append(I18n.get(SupplychainExceptionMessage.BATCH_OUTGOING_STOCK_MOVE_INVOICING_REPORT));
    sb.append(
        String.format(
            I18n.get(
                SupplychainExceptionMessage.BATCH_OUTGOING_STOCK_MOVE_INVOICING_DONE_SINGULAR,
                SupplychainExceptionMessage.BATCH_OUTGOING_STOCK_MOVE_INVOICING_DONE_PLURAL,
                batch.getDone()),
            batch.getDone()));
    sb.append(
        String.format(
            I18n.get(
                BaseExceptionMessage.ABSTRACT_BATCH_ANOMALY_SINGULAR,
                BaseExceptionMessage.ABSTRACT_BATCH_ANOMALY_PLURAL,
                batch.getAnomaly()),
            batch.getAnomaly()));
    addComment(sb.toString());
    super.stop();
  }
}
