/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.service.StockLocationLineFetchService;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.apps.stock.service.StockMoveLineStockLocationServiceImpl;
import com.axelor.apps.stock.service.app.AppStockService;
import jakarta.inject.Inject;

public class StockMoveLineStockLocationServiceSupplychainImpl
    extends StockMoveLineStockLocationServiceImpl {

  @Inject
  public StockMoveLineStockLocationServiceSupplychainImpl(
      AppBaseService appBaseService,
      AppStockService appStockService,
      StockLocationService stockLocationService,
      StockLocationLineFetchService stockLocationLineFetchService) {
    super(appBaseService, appStockService, stockLocationService, stockLocationLineFetchService);
  }

  @Override
  protected boolean isStockLocationMatchingParent(
      StockLocation detailStockLocation, StockLocation parentStockLocation) {
    return detailStockLocation.equals(parentStockLocation)
        || (parentStockLocation.equals(detailStockLocation.getParentStockLocation())
            && detailStockLocation.getUsableOnSaleOrder());
  }
}
