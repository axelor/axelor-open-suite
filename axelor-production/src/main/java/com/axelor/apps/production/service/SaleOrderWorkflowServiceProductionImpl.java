/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.productionorder.ProductionOrderSaleOrderService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.supplychain.service.AccountingSituationSupplychainService;
import com.axelor.apps.supplychain.service.SaleOrderPurchaseService;
import com.axelor.apps.supplychain.service.SaleOrderStockService;
import com.axelor.apps.supplychain.service.SaleOrderWorkflowServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderWorkflowServiceProductionImpl
    extends SaleOrderWorkflowServiceSupplychainImpl {

  protected ProductionOrderSaleOrderService productionOrderSaleOrderService;
  protected AppProductionService appProductionService;

  @Inject
  public SaleOrderWorkflowServiceProductionImpl(
      SequenceService sequenceService,
      PartnerRepository partnerRepo,
      SaleOrderRepository saleOrderRepo,
      AppSaleService appSaleService,
      UserService userService,
      SaleOrderStockService saleOrderStockService,
      SaleOrderPurchaseService saleOrderPurchaseService,
      AppSupplychainService appSupplychainService,
      AccountingSituationSupplychainService accountingSituationSupplychainService,
      ProductionOrderSaleOrderService productionOrderSaleOrderService,
      AppProductionService appProductionService) {

    super(
        sequenceService,
        partnerRepo,
        saleOrderRepo,
        appSaleService,
        userService,
        saleOrderStockService,
        saleOrderPurchaseService,
        appSupplychainService,
        accountingSituationSupplychainService);

    this.productionOrderSaleOrderService = productionOrderSaleOrderService;
    this.appProductionService = appProductionService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void confirmSaleOrder(SaleOrder saleOrder) throws AxelorException {
    super.confirmSaleOrder(saleOrder);

    if (appProductionService.getAppProduction().getProductionOrderGenerationAuto()) {
      productionOrderSaleOrderService.generateProductionOrder(saleOrder);
    }
  }
}
