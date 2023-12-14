/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.service.productionorder.ProductionOrderService;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockRules;
import com.axelor.apps.stock.db.repo.StockConfigRepository;
import com.axelor.apps.stock.db.repo.StockRulesRepository;
import com.axelor.apps.stock.service.StockRulesService;
import com.axelor.apps.supplychain.service.PurchaseOrderSupplychainService;
import com.axelor.apps.supplychain.service.StockRulesSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.message.db.repo.TemplateRepository;
import com.axelor.message.service.TemplateMessageService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StockRulesSupplychainServiceProductionImpl extends StockRulesSupplychainServiceImpl {

  protected ProductionOrderService productionOrderService;

  @Inject
  public StockRulesSupplychainServiceProductionImpl(
      StockRulesService stockRulesService,
      PurchaseOrderLineService purchaseOrderLineService,
      PurchaseOrderRepository purchaseOrderRepo,
      TemplateRepository templateRepo,
      TemplateMessageService templateMessageService,
      StockConfigRepository stockConfigRepo,
      AppPurchaseService appPurchaseService,
      PurchaseOrderService purchaseOrderService,
      AppSupplychainService appSupplychainService,
      AppBaseService appBaseService,
      PurchaseOrderSupplychainService purchaseOrderSupplychainService,
      PartnerPriceListService partnerPriceListService,
      ProductionOrderService productionOrderService) {
    super(
        stockRulesService,
        purchaseOrderLineService,
        purchaseOrderRepo,
        templateRepo,
        templateMessageService,
        stockConfigRepo,
        appPurchaseService,
        purchaseOrderService,
        appSupplychainService,
        appBaseService,
        purchaseOrderSupplychainService,
        partnerPriceListService);
    this.productionOrderService = productionOrderService;
  }

  @Override
  public void processNonCompliantStockLocationLine(
      StockRules stockRules, StockLocationLine stockLocationLine) throws AxelorException {
    super.processNonCompliantStockLocationLine(stockRules, stockLocationLine);

    StockLocation stockLocation = stockLocationLine.getStockLocation();
    if (stockLocation == null
        || stockRules == null
        || !stockRules
            .getOrderAlertSelect()
            .equals(StockRulesRepository.ORDER_ALERT_PRODUCTION_ORDER)) {
      return;
    }

    Product product = stockLocationLine.getProduct();
    BigDecimal qtyToProduce = stockRulesService.getQtyToOrder(stockLocationLine, stockRules);
    productionOrderService.generateProductionOrder(
        product, null, qtyToProduce, LocalDateTime.now());

    if (stockRules.getAlert()) {
      generateAndSendMessage(stockRules);
    }
  }
}
