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

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.service.StockLocationLineFetchServiceImpl;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class StockLocationLineFetchServiceSupplychainImpl
    extends StockLocationLineFetchServiceImpl {

  protected AppSupplychainService appSupplychainService;

  @Inject
  public StockLocationLineFetchServiceSupplychainImpl(AppSupplychainService appSupplychainService) {
    this.appSupplychainService = appSupplychainService;
  }

  @Override
  public BigDecimal getTrackingNumberAvailableQty(
      StockLocation stockLocation, TrackingNumber trackingNumber) {

    if (!appSupplychainService.isApp("supplychain")
        || !appSupplychainService.getAppSupplychain().getManageStockReservation()) {
      return super.getTrackingNumberAvailableQty(stockLocation, trackingNumber);
    }

    StockLocationLine detailStockLocationLine =
        getDetailLocationLine(stockLocation, trackingNumber.getProduct(), trackingNumber);

    BigDecimal availableQty = BigDecimal.ZERO;

    if (detailStockLocationLine != null) {
      availableQty =
          detailStockLocationLine
              .getCurrentQty()
              .subtract(detailStockLocationLine.getReservedQty());
    }
    return availableQty;
  }

  @Override
  public BigDecimal getAvailableQty(StockLocation stockLocation, Product product) {

    if (!appSupplychainService.isApp("supplychain")) {
      return super.getAvailableQty(stockLocation, product);
    }

    StockLocationLine stockLocationLine = getStockLocationLine(stockLocation, product);
    BigDecimal availableQty = BigDecimal.ZERO;
    if (stockLocationLine != null) {
      availableQty = stockLocationLine.getCurrentQty().subtract(stockLocationLine.getReservedQty());
    }
    return availableQty;
  }
}
