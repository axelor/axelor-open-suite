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
package com.axelor.apps.quality.service;

import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.quality.service.app.AppQualityService;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.service.StockLocationLineFetchService;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.apps.supplychain.db.SupplyChainConfig;
import com.axelor.apps.supplychain.service.ReservedQtyServiceImpl;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import jakarta.inject.Inject;

public class ReservedQtyServiceQualityImpl extends ReservedQtyServiceImpl {

  protected final AppQualityService appQualityService;
  protected final NonCompliantReceptionService nonCompliantReceptionService;

  @Inject
  public ReservedQtyServiceQualityImpl(
      StockLocationLineService stockLocationLineService,
      StockMoveLineRepository stockMoveLineRepository,
      UnitConversionService unitConversionService,
      SupplyChainConfigService supplyChainConfigService,
      AppBaseService appBaseService,
      StockLocationLineFetchService stockLocationLineFetchService,
      AppQualityService appQualityService,
      NonCompliantReceptionService nonCompliantReceptionService) {
    super(
        stockLocationLineService,
        stockMoveLineRepository,
        unitConversionService,
        supplyChainConfigService,
        appBaseService,
        stockLocationLineFetchService);
    this.appQualityService = appQualityService;
    this.nonCompliantReceptionService = nonCompliantReceptionService;
  }

  @Override
  protected boolean isAutoAllocateOnReceipt(
      StockMoveLine stockMoveLine, SupplyChainConfig supplyChainConfig) {
    if (appQualityService.isApp("quality")
        && nonCompliantReceptionService.isRedirectedToQuarantine(stockMoveLine)) {
      return false;
    }
    return super.isAutoAllocateOnReceipt(stockMoveLine, supplyChainConfig);
  }
}
