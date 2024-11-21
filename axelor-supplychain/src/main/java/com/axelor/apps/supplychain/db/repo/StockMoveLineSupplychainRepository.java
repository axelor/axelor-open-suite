/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.account.db.repo.AccountingBatchRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineStockRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.StockMoveLineServiceSupplychain;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.util.Map;
import javax.persistence.PersistenceException;

public class StockMoveLineSupplychainRepository extends StockMoveLineStockRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    try {
      Long stockMoveLineId = (Long) json.get("id");
      StockMoveLine stockMoveLine = find(stockMoveLineId);
      StockMove stockMove = stockMoveLine.getStockMove();

      if (context.containsKey("_cutOffPreview") && (boolean) context.get("_cutOffPreview")) {
        boolean isPurchase =
            (int) context.get("_accountingCutOffTypeSelect")
                == AccountingBatchRepository.ACCOUNTING_CUT_OFF_TYPE_SUPPLIER_INVOICES;
        boolean ati = (boolean) context.get("_ati");
        boolean recoveredTax = (boolean) context.get("_recoveredTax");

        json.put(
            "$notInvoicedAmount",
            Beans.get(StockMoveLineServiceSupplychain.class)
                .getAmountNotInvoiced(stockMoveLine, isPurchase, ati, recoveredTax));
      }

      if (stockMove != null && stockMove.getStatusSelect() == StockMoveRepository.STATUS_REALIZED) {
        Beans.get(StockMoveLineServiceSupplychain.class).setInvoiceStatus(stockMoveLine);
        json.put(
            "availableStatus",
            stockMoveLine.getProduct() != null && stockMoveLine.getProduct().getStockManaged()
                ? stockMoveLine.getAvailableStatus()
                : null);
        json.put("availableStatusSelect", stockMoveLine.getAvailableStatusSelect());
      }

    } catch (Exception e) {
      TraceBackService.trace(e);
    }

    return super.populate(json, context);
  }

  @Override
  public void remove(StockMoveLine stockMoveLine) {
    try {
      if (Beans.get(StockMoveLineServiceSupplychain.class)
          .isAllocatedStockMoveLine(stockMoveLine)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(SupplychainExceptionMessage.ALLOCATED_STOCK_MOVE_LINE_DELETED_ERROR));
      } else {
        super.remove(stockMoveLine);
      }
    } catch (AxelorException e) {
      throw new PersistenceException(e.getMessage(), e);
    }
  }
}
