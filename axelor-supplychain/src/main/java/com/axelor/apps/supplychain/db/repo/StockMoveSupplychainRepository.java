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
package com.axelor.apps.supplychain.db.repo;

import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveManagementRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.service.StockMoveServiceSupplychain;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;
import javax.persistence.PersistenceException;

public class StockMoveSupplychainRepository extends StockMoveManagementRepository {

  @Inject StockMoveServiceSupplychain stockMoveServiceSupplychain;

  @Override
  public StockMove copy(StockMove entity, boolean deep) {

    StockMove copy = super.copy(entity, deep);

    copy.setInvoiceSet(null);
    copy.setOriginTypeSelect(null);
    copy.setOrigin(null);
    copy.setOriginId(null);
    if (copy.getStockMoveLineList() != null) {
      for (StockMoveLine stockMoveLine : copy.getStockMoveLineList()) {
        stockMoveLine.setReservedQty(BigDecimal.ZERO);
        stockMoveLine.setRequestedReservedQty(BigDecimal.ZERO);
        stockMoveLine.setIsQtyRequested(false);
        stockMoveLine.setReservationDateTime(null);
        stockMoveLine.setQtyInvoiced(null);
        stockMoveLine.setSaleOrderLine(null);
        stockMoveLine.setPurchaseOrderLine(null);
      }
    }
    copy.setInvoicingStatusSelect(StockMoveRepository.STATUS_NOT_INVOICED);

    return copy;
  }

  @Override
  public StockMove save(StockMove stockMove) {
    try {
      if (Boolean.TRUE.equals(
          Beans.get(AppSaleService.class).getAppSale().getEnablePackManagement())) {
        stockMoveServiceSupplychain.computePack(stockMove);
      } else {
        stockMoveServiceSupplychain.resetPack(stockMove);
      }
      return super.save(stockMove);
    } catch (Exception e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }
  }
}
