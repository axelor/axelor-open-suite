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
package com.axelor.apps.production.service;

import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.ShippingCoefService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import com.axelor.apps.stock.service.StockLocationLineFetchService;
import com.axelor.apps.stock.service.StockLocationLineHistoryService;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.apps.stock.service.StockMoveToolService;
import com.axelor.apps.stock.service.TrackingNumberCreateService;
import com.axelor.apps.stock.service.TrackingNumberService;
import com.axelor.apps.stock.service.WeightedAveragePriceService;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.apps.supplychain.db.repo.SupplychainBatchRepository;
import com.axelor.apps.supplychain.service.StockMoveLineServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class StockMoveLineProductionServiceImpl extends StockMoveLineServiceSupplychainImpl {

  @Inject
  public StockMoveLineProductionServiceImpl(
      TrackingNumberService trackingNumberService,
      AppBaseService appBaseService,
      AppStockService appStockService,
      StockMoveToolService stockMoveToolService,
      StockMoveLineRepository stockMoveLineRepository,
      StockLocationLineService stockLocationLineService,
      UnitConversionService unitConversionService,
      WeightedAveragePriceService weightedAveragePriceService,
      TrackingNumberRepository trackingNumberRepo,
      ShippingCoefService shippingCoefService,
      AccountManagementService accountManagementService,
      PriceListService priceListService,
      ProductCompanyService productCompanyService,
      SupplychainBatchRepository supplychainBatchRepo,
      SupplyChainConfigService supplychainConfigService,
      StockLocationLineHistoryService stockLocationLineHistoryService,
      InvoiceLineRepository invoiceLineRepository,
      AppSupplychainService appSupplychainService,
      StockLocationLineFetchService stockLocationLineFetchService,
      TrackingNumberCreateService trackingNumberCreateService) {
    super(
        trackingNumberService,
        appBaseService,
        appStockService,
        stockMoveToolService,
        stockMoveLineRepository,
        stockLocationLineService,
        unitConversionService,
        weightedAveragePriceService,
        trackingNumberRepo,
        shippingCoefService,
        accountManagementService,
        priceListService,
        productCompanyService,
        supplychainBatchRepo,
        supplychainConfigService,
        stockLocationLineHistoryService,
        invoiceLineRepository,
        appSupplychainService,
        stockLocationLineFetchService,
        trackingNumberCreateService);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  protected void fillOriginTrackingNumber(StockMoveLine stockMoveLine) {
    super.fillOriginTrackingNumber(stockMoveLine);

    if (appBaseService.isApp("production")) {
      TrackingNumber trackingNumber = stockMoveLine.getTrackingNumber();
      if (trackingNumber != null
          && stockMoveLine.getStockMove() != null
          && stockMoveLine.getStockMove().getManufOrder() != null) {
        trackingNumber.setOriginMoveTypeSelect(
            TrackingNumberRepository.ORIGIN_MOVE_TYPE_MANUFACTURING);
        trackingNumber.setOriginManufOrder(stockMoveLine.getStockMove().getManufOrder());

        trackingNumberRepo.save(trackingNumber);
      }
    }
  }
}
