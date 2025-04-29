/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service.saleorderline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineCheckServiceImpl;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.google.inject.Inject;

public class SaleOrderLineCheckSupplychainServiceImpl extends SaleOrderLineCheckServiceImpl
    implements SaleOrderLineCheckSupplychainService {

  protected StockLocationLineService stockLocationLineService;
  protected AppSupplychainService appSupplychainService;

  @Inject
  public SaleOrderLineCheckSupplychainServiceImpl(
      StockLocationLineService stockLocationLineService,
      AppSupplychainService appSupplychainService) {
    this.stockLocationLineService = stockLocationLineService;
    this.appSupplychainService = appSupplychainService;
  }

  @Override
  public void productOnChangeCheck(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    super.productOnChangeCheck(saleOrderLine, saleOrder);
    checkStocks(saleOrderLine, saleOrder);
  }

  @Override
  public void qtyOnChangeCheck(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    super.qtyOnChangeCheck(saleOrderLine, saleOrder);
    checkStocks(saleOrderLine, saleOrder);
  }

  @Override
  public void unitOnChangeCheck(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    super.unitOnChangeCheck(saleOrderLine, saleOrder);
    checkStocks(saleOrderLine, saleOrder);
  }

  @Override
  public void saleSupplySelectOnChangeCheck(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    checkStocks(saleOrderLine, saleOrder);
  }

  protected void checkStocks(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {

    if (!appSupplychainService.isApp("supplychain")
        || !appSupplychainService.getAppSupplychain().getCheckSaleStocks()) {
      return;
    }
    Product product = saleOrderLine.getProduct();
    StockLocation stockLocation = saleOrder.getStockLocation();
    Unit unit = saleOrderLine.getUnit();
    if (product == null
        || stockLocation == null
        || unit == null
        || saleOrderLine.getSaleSupplySelect() != SaleOrderLineRepository.SALE_SUPPLY_FROM_STOCK) {
      return;
    }
    stockLocationLineService.checkIfEnoughStock(
        stockLocation, product, unit, saleOrderLine.getQty());
  }
}
