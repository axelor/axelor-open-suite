/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service;

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.PartnerProductQualityRatingService;
import com.axelor.apps.stock.service.PartnerStockSettingsService;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveToolService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.supplychain.service.PartnerSupplychainService;
import com.axelor.apps.supplychain.service.ReservedQtyService;
import com.axelor.apps.supplychain.service.StockMoveServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.google.inject.Inject;

public class StockMoveServiceProductionImpl extends StockMoveServiceSupplychainImpl {

  @Inject
  public StockMoveServiceProductionImpl(
      StockMoveLineService stockMoveLineService,
      StockMoveToolService stockMoveToolService,
      StockMoveLineRepository stockMoveLineRepository,
      AppBaseService appBaseService,
      StockMoveRepository stockMoveRepository,
      PartnerProductQualityRatingService partnerProductQualityRatingService,
      AppSupplychainService appSupplyChainService,
      AccountConfigService accountConfigService,
      PurchaseOrderRepository purchaseOrderRepo,
      SaleOrderRepository saleOrderRepo,
      UnitConversionService unitConversionService,
      ReservedQtyService reservedQtyService,
      ProductRepository productRepository,
      PartnerSupplychainService partnerSupplychainService,
      AppAccountService appAccountService,
      PartnerStockSettingsService partnerStockSettingsService,
      StockConfigService stockConfigService) {
    super(
        stockMoveLineService,
        stockMoveToolService,
        stockMoveLineRepository,
        appBaseService,
        stockMoveRepository,
        partnerProductQualityRatingService,
        appSupplyChainService,
        accountConfigService,
        purchaseOrderRepo,
        saleOrderRepo,
        unitConversionService,
        reservedQtyService,
        productRepository,
        partnerSupplychainService,
        appAccountService,
        partnerStockSettingsService,
        stockConfigService);
  }

  @Override
  public void setOrigin(StockMove oldStockMove, StockMove newStockMove) {
    if (oldStockMove.getManufOrder() != null) {
      newStockMove.setManufOrder(oldStockMove.getManufOrder());
    } else if (oldStockMove.getOperationOrder() != null) {
      newStockMove.setOperationOrder(oldStockMove.getOperationOrder());
    } else {
      super.setOrigin(oldStockMove, newStockMove);
    }
  }
}
