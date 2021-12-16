/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.administration.AbstractBatchService;
import com.axelor.apps.supplychain.db.SupplychainBatch;
import com.axelor.apps.supplychain.db.repo.SupplychainBatchRepository;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.time.LocalDate;

public class SupplychainBatchService extends AbstractBatchService {

  @Override
  protected Class<? extends Model> getModelClass() {
    return SupplychainBatch.class;
  }

  @Override
  public Batch run(Model batchModel) throws AxelorException {

    Batch batch;
    SupplychainBatch supplychainBatch = (SupplychainBatch) batchModel;

    switch (supplychainBatch.getActionSelect()) {
      case SupplychainBatchRepository.ACTION_ACCOUNTING_CUT_OFF:
        batch = accountingCutOff(supplychainBatch);
        break;
      case SupplychainBatchRepository.ACTION_INVOICE_OUTGOING_STOCK_MOVES:
        batch = invoiceOutgoingStockMoves(supplychainBatch);
        break;
      case SupplychainBatchRepository.ACTION_INVOICE_ORDERS:
        batch = invoiceOrders(supplychainBatch);
        break;
      case SupplychainBatchRepository.ACTION_UPDATE_STOCK_HISTORY:
        batch = updateStockHistory(supplychainBatch);
        break;
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.BASE_BATCH_1),
            supplychainBatch.getActionSelect(),
            supplychainBatch.getCode());
    }

    return batch;
  }

  public Batch accountingCutOff(SupplychainBatch supplychainBatch) {
    return Beans.get(BatchAccountingCutOff.class).run(supplychainBatch);
  }

  public Batch invoiceOutgoingStockMoves(SupplychainBatch supplychainBatch) {
    return Beans.get(BatchOutgoingStockMoveInvoicing.class).run(supplychainBatch);
  }

  public Batch invoiceOrders(SupplychainBatch supplychainBatch) {
    switch (supplychainBatch.getInvoiceOrdersTypeSelect()) {
      case SupplychainBatchRepository.INVOICE_ORDERS_TYPE_SALE:
        return Beans.get(BatchOrderInvoicingSale.class).run(supplychainBatch);
      case SupplychainBatchRepository.INVOICE_ORDERS_TYPE_PURCHASE:
        return Beans.get(BatchOrderInvoicingPurchase.class).run(supplychainBatch);
      default:
        throw new IllegalArgumentException(
            String.format(
                "Unknown invoice orders type: %d", supplychainBatch.getInvoiceOrdersTypeSelect()));
    }
  }

  public void checkDates(SupplychainBatch supplychainBatch) throws AxelorException {
    LocalDate date = supplychainBatch.getMoveDate();
    if (date != null
        && date.getDayOfMonth()
            != date.plusMonths(1).withDayOfMonth(1).minusDays(1).getDayOfMonth()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(com.axelor.apps.supplychain.exception.IExceptionMessage.BATCH_MOVE_DATE_ERROR));
    }
  }


  public Batch updateStockHistory(SupplychainBatch supplychainBatch) {
    return Beans.get(BatchUpdateStockHistory.class).run(supplychainBatch);
  }

  public String getMoveLinesToProcessIdList(
      Company company, Journal researchJournal, LocalDate moveDate, Integer limit, Integer offset) {
    Query<MoveLine> moveLineQuery = Beans.get(MoveLineRepository.class).all();

    String moveLineQueryStr =
        "self.account.manageCutOffPeriod IS TRUE "
            + "AND self.cutOffStartDate IS NOT NULL "
            + "AND self.cutOffEndDate IS NOT NULL "
            + "AND self.move.journal = :researchJournal "
            + "AND YEAR(self.move.date) = YEAR(:moveDate) "
            + "AND self.move.company = :company";

    moveLineQuery
        .filter(moveLineQueryStr)
        .bind("researchJournal", researchJournal)
        .bind("moveDate", moveDate)
        .bind("company", company)
        .order("id");

    if (limit != null && offset != null) {
      return StringTool.getIdListString(moveLineQuery.fetch(limit, offset));
    } else {
      return StringTool.getIdListString(moveLineQuery.fetch());
    }
  }

  public String getStockMoveLinesToProcessIdList(
      Company company,
      LocalDate moveDate,
      int accountingCutOffTypeSelect,
      Integer limit,
      Integer offset) {
    Query<StockMoveLine> stockMoveLineQuery = Beans.get(StockMoveLineRepository.class).all();

    String stockMoveLineQueryStr =
        "(self.qtyInvoiced = 0 OR self.qtyInvoiced <> self.realQty) "
            + "AND self.stockMove.invoicingStatusSelect = :statusInvoiced "
            + "AND self.stockMove.statusSelect = :statusRealized "
            + "AND self.stockMove.typeSelect = :typeSelect "
            + "AND self.stockMove.realDate <= :moveDate "
            + "AND self.company = :company";

    stockMoveLineQuery
        .filter(stockMoveLineQueryStr)
        .bind("statusInvoiced", StockMoveRepository.STATUS_INVOICED)
        .bind("statusRealized", StockMoveRepository.STATUS_REALIZED)
        .bind(
            "typeSelect",
            accountingCutOffTypeSelect
                    == SupplychainBatchRepository.ACCOUNTING_CUT_OFF_TYPE_SUPPLIER_INVOICES
                ? StockMoveRepository.TYPE_INCOMING
                : StockMoveRepository.TYPE_OUTGOING)
        .bind("moveDate", moveDate)
        .bind("company", company)
        .order("id");

    if (limit != null && offset != null) {
      return StringTool.getIdListString(stockMoveLineQuery.fetch(limit, offset));
    } else {
      return StringTool.getIdListString(stockMoveLineQuery.fetch());
    }
  }
}
