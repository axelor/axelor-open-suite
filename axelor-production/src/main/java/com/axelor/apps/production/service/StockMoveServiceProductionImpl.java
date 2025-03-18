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
package com.axelor.apps.production.service;

import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.service.PfpService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.status.SaleOrderConfirmService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.PartnerProductQualityRatingService;
import com.axelor.apps.stock.service.PartnerStockSettingsService;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveToolService;
import com.axelor.apps.stock.service.app.AppStockService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.supplychain.service.PartnerSupplychainService;
import com.axelor.apps.supplychain.service.ReservedQtyService;
import com.axelor.apps.supplychain.service.StockMoveLineServiceSupplychain;
import com.axelor.apps.supplychain.service.StockMoveServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class StockMoveServiceProductionImpl extends StockMoveServiceSupplychainImpl
    implements StockMoveProductionService {
  @Inject
  public StockMoveServiceProductionImpl(
      StockMoveLineService stockMoveLineService,
      StockMoveToolService stockMoveToolService,
      StockMoveLineRepository stockMoveLineRepository,
      AppBaseService appBaseService,
      StockMoveRepository stockMoveRepository,
      PartnerProductQualityRatingService partnerProductQualityRatingService,
      ProductRepository productRepository,
      PartnerStockSettingsService partnerStockSettingsService,
      StockConfigService stockConfigService,
      AppStockService appStockService,
      ProductCompanyService productCompanyService,
      AppSupplychainService appSupplyChainService,
      AppAccountService appAccountService,
      PurchaseOrderRepository purchaseOrderRepo,
      SaleOrderRepository saleOrderRepo,
      UnitConversionService unitConversionService,
      ReservedQtyService reservedQtyService,
      PartnerSupplychainService partnerSupplychainService,
      FixedAssetRepository fixedAssetRepository,
      PfpService pfpService,
      SaleOrderConfirmService saleOrderConfirmService,
      StockMoveLineServiceSupplychain stockMoveLineServiceSupplychain) {
    super(
        stockMoveLineService,
        stockMoveToolService,
        stockMoveLineRepository,
        appBaseService,
        stockMoveRepository,
        partnerProductQualityRatingService,
        productRepository,
        partnerStockSettingsService,
        stockConfigService,
        appStockService,
        productCompanyService,
        appSupplyChainService,
        appAccountService,
        purchaseOrderRepo,
        saleOrderRepo,
        unitConversionService,
        reservedQtyService,
        partnerSupplychainService,
        fixedAssetRepository,
        pfpService,
        saleOrderConfirmService,
        stockMoveLineServiceSupplychain);
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

  @Override
  public void cancel(StockMove stockMove) throws AxelorException {
    if (!appBaseService.isApp("production")) {
      super.cancel(stockMove);
      return;
    }

    if (stockMove.getManufOrder() != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.CAN_NOT_CANCEL_STOCK_MOVE_LINKED_TO_MANUF_ORDER));
    }
    cancelStockMoveInProduction(stockMove);
  }

  @Override
  public void cancelFromManufOrder(StockMove stockMove) throws AxelorException {
    cancelStockMoveInProduction(stockMove);
  }

  @Override
  public void cancelFromManufOrder(StockMove stockMove, CancelReason cancelReason)
      throws AxelorException {
    applyCancelReason(stockMove, cancelReason);
    cancelStockMoveInProduction(stockMove);
  }

  // future code specific to stock move cancellation in production module goes here
  protected void cancelStockMoveInProduction(StockMove stockMove) throws AxelorException {
    super.cancel(stockMove);
  }
}
