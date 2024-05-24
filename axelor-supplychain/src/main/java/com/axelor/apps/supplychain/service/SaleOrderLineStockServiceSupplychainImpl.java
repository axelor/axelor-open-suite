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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineStockServiceImpl;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class SaleOrderLineStockServiceSupplychainImpl extends SaleOrderLineStockServiceImpl {

  protected AppBaseService appBaseService;

  @Inject
  public SaleOrderLineStockServiceSupplychainImpl(AppBaseService appAccountService) {
    this.appBaseService = appAccountService;
  }

  @Override
  public BigDecimal getAvailableStock(SaleOrder saleOrder, SaleOrderLine saleOrderLine) {

    if (!appBaseService.isApp("supplychain")) {
      return super.getAvailableStock(saleOrder, saleOrderLine);
    }

    StockLocationLine stockLocationLine =
        Beans.get(StockLocationLineService.class)
            .getStockLocationLine(saleOrder.getStockLocation(), saleOrderLine.getProduct());

    if (stockLocationLine == null) {
      return BigDecimal.ZERO;
    }
    return stockLocationLine.getCurrentQty().subtract(stockLocationLine.getReservedQty());
  }

  @Override
  public BigDecimal getAllocatedStock(SaleOrder saleOrder, SaleOrderLine saleOrderLine) {

    if (!appBaseService.isApp("supplychain")) {
      return super.getAllocatedStock(saleOrder, saleOrderLine);
    }

    StockLocationLine stockLocationLine =
        Beans.get(StockLocationLineService.class)
            .getStockLocationLine(saleOrder.getStockLocation(), saleOrderLine.getProduct());

    if (stockLocationLine == null) {
      return BigDecimal.ZERO;
    }
    return stockLocationLine.getReservedQty();
  }
}
