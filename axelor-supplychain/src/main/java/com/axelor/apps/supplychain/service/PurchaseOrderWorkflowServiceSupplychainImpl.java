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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.purchase.service.PurchaseOrderTypeSelectService;
import com.axelor.apps.purchase.service.PurchaseOrderWorkflowServiceImpl;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PurchaseOrderWorkflowServiceSupplychainImpl extends PurchaseOrderWorkflowServiceImpl {

  protected AppSupplychainService appSupplychainService;
  protected PurchaseOrderStockService purchaseOrderStockService;
  protected AppAccountService appAccountService;
  protected PurchaseOrderSupplychainService purchaseOrderSupplychainService;

  @Inject
  public PurchaseOrderWorkflowServiceSupplychainImpl(
      PurchaseOrderService purchaseOrderService,
      PurchaseOrderRepository purchaseOrderRepo,
      AppPurchaseService appPurchaseService,
      AppSupplychainService appSupplychainService,
      PurchaseOrderStockService purchaseOrderStockService,
      AppAccountService appAccountService,
      PurchaseOrderSupplychainService purchaseOrderSupplychainService,
      PurchaseOrderTypeSelectService purchaseOrderTypeSelectService) {
    super(
        purchaseOrderService,
        purchaseOrderRepo,
        appPurchaseService,
        purchaseOrderTypeSelectService);
    this.appSupplychainService = appSupplychainService;
    this.purchaseOrderStockService = purchaseOrderStockService;
    this.appAccountService = appAccountService;
    this.purchaseOrderSupplychainService = purchaseOrderSupplychainService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validatePurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException {
    super.validatePurchaseOrder(purchaseOrder);

    if (!appSupplychainService.isApp("supplychain")) {
      return;
    }

    if (appSupplychainService.getAppSupplychain().getSupplierStockMoveGenerationAuto()
        && !purchaseOrderStockService.existActiveStockMoveForPurchaseOrder(purchaseOrder.getId())) {
      purchaseOrderStockService.createStockMoveFromPurchaseOrder(purchaseOrder);
    }

    int intercoPurchaseCreatingStatus =
        appSupplychainService.getAppSupplychain().getIntercoPurchaseCreatingStatusSelect();
    if (purchaseOrder.getInterco()
        && intercoPurchaseCreatingStatus == PurchaseOrderRepository.STATUS_VALIDATED) {
      Beans.get(IntercoService.class).generateIntercoSaleFromPurchase(purchaseOrder);
    }
  }
}
