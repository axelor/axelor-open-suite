/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.service.batch;

import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.apps.base.service.BlockingService;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.db.SupplychainBatch;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.StockMoveInvoiceService;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.List;

public class BatchOutgoingStockMoveInvoicing extends AbstractBatch {

  private StockMoveInvoiceService stockMoveInvoiceService;

  @Inject
  public BatchOutgoingStockMoveInvoicing(StockMoveInvoiceService stockMoveInvoiceService) {
    this.stockMoveInvoiceService = stockMoveInvoiceService;
  }

  @Override
  protected void process() {
    SupplychainBatch supplychainBatch = batch.getSupplychainBatch();
    List<Long> anomalyList = Lists.newArrayList(0L);
    Query<StockMove> query = Beans.get(StockMoveRepository.class).all();
    query.filter(
        "self.statusSelect = :statusSelect AND self.saleOrder IS NOT NULL "
            + "AND (self.invoice IS NULL OR self.invoice.statusSelect = :invoiceStatusSelect) "
            + "AND self.id NOT IN (:anomalyList) "
            + "AND self.partner.id NOT IN ("
            + Beans.get(BlockingService.class)
                .listOfBlockedPartner(
                    supplychainBatch.getCompany(), BlockingRepository.INVOICING_BLOCKING)
            + ")");

    query.bind("statusSelect", StockMoveRepository.STATUS_REALIZED);
    query.bind("invoiceStatusSelect", InvoiceRepository.STATUS_CANCELED);
    query.bind("anomalyList", anomalyList);

    for (List<StockMove> stockMoveList;
        !(stockMoveList = query.fetch(FETCH_LIMIT)).isEmpty();
        JPA.clear()) {
      for (StockMove stockMove : stockMoveList) {
        try {
          stockMoveInvoiceService.createInvoiceFromSaleOrder(stockMove, stockMove.getSaleOrder());
          incrementDone();
        } catch (Exception e) {
          incrementAnomaly();
          anomalyList.add(stockMove.getId());
          query.bind("anomalyList", anomalyList);
          TraceBackService.trace(e, IException.INVOICE_ORIGIN, batch.getId());
          e.printStackTrace();
          break;
        }
      }
    }
  }

  @Override
  protected void stop() {
    StringBuilder sb = new StringBuilder();
    sb.append(I18n.get(IExceptionMessage.BATCH_OUTGOING_STOCK_MOVE_INVOICING_REPORT));
    sb.append(
        String.format(
            I18n.get(
                IExceptionMessage.BATCH_OUTGOING_STOCK_MOVE_INVOICING_DONE_SINGULAR,
                IExceptionMessage.BATCH_OUTGOING_STOCK_MOVE_INVOICING_DONE_PLURAL,
                batch.getDone()),
            batch.getDone()));
    sb.append(
        String.format(
            I18n.get(
                com.axelor.apps.base.exceptions.IExceptionMessage.ABSTRACT_BATCH_ANOMALY_SINGULAR,
                com.axelor.apps.base.exceptions.IExceptionMessage.ABSTRACT_BATCH_ANOMALY_PLURAL,
                batch.getAnomaly()),
            batch.getAnomaly()));
    addComment(sb.toString());
    super.stop();
  }
}
