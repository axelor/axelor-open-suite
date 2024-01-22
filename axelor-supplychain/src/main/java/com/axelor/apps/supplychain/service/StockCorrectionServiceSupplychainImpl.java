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

import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.service.StockCorrectionServiceImpl;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.google.inject.Inject;
import java.util.Map;

public class StockCorrectionServiceSupplychainImpl extends StockCorrectionServiceImpl {

  @Inject
  public StockCorrectionServiceSupplychainImpl(
      StockConfigService stockConfigService,
      ProductCompanyService productCompanyService,
      StockLocationLineService stockLocationLineService,
      AppBaseService baseService,
      StockMoveService stockMoveService,
      StockMoveLineService stockMoveLineService) {
    super(
        stockConfigService,
        productCompanyService,
        stockLocationLineService,
        baseService,
        stockMoveService,
        stockMoveLineService);
  }

  @Override
  public void getDefaultQtys(
      StockLocationLine stockLocationLine, Map<String, Object> stockCorrectionQtys) {
    super.getDefaultQtys(stockLocationLine, stockCorrectionQtys);
    stockCorrectionQtys.put("reservedQty", stockLocationLine.getReservedQty());
  }
}
