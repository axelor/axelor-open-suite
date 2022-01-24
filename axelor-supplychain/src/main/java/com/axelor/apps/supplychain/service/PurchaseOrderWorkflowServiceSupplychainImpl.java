/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.purchase.service.PurchaseOrderWorkflowServiceImpl;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PurchaseOrderWorkflowServiceSupplychainImpl extends PurchaseOrderWorkflowServiceImpl {

  protected AppSupplychainService appSupplychainService;
  protected PurchaseOrderStockService purchaseOrderStockService;
  protected AppAccountService appAccountService;
  protected BudgetSupplychainService budgetSupplychainService;
  protected PurchaseOrderSupplychainService purchaseOrderSupplychainService;

  @Inject
  public PurchaseOrderWorkflowServiceSupplychainImpl(
      PurchaseOrderService purchaseOrderService,
      PurchaseOrderRepository purchaseOrderRepo,
      AppPurchaseService appPurchaseService,
      AppSupplychainService appSupplychainService,
      PurchaseOrderStockService purchaseOrderStockService,
      AppAccountService appAccountService,
      BudgetSupplychainService budgetSupplychainService,
      PurchaseOrderSupplychainService purchaseOrderSupplychainService) {
    super(purchaseOrderService, purchaseOrderRepo, appPurchaseService);
    this.appSupplychainService = appSupplychainService;
    this.purchaseOrderStockService = purchaseOrderStockService;
    this.appAccountService = appAccountService;
    this.budgetSupplychainService = budgetSupplychainService;
    this.purchaseOrderSupplychainService = purchaseOrderSupplychainService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validatePurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException {
    super.validatePurchaseOrder(purchaseOrder);

    if (!Beans.get(AppSupplychainService.class).isApp("supplychain")) {
      return;
    }

    if (appSupplychainService.getAppSupplychain().getSupplierStockMoveGenerationAuto()
        && !purchaseOrderStockService.existActiveStockMoveForPurchaseOrder(purchaseOrder.getId())) {
      purchaseOrderStockService.createStockMoveFromPurchaseOrder(purchaseOrder);
    }

    if (appAccountService.getAppBudget().getApp().getActive()
        && !appAccountService.getAppBudget().getManageMultiBudget()) {
      purchaseOrderSupplychainService.generateBudgetDistribution(purchaseOrder);
    }
    int intercoPurchaseCreatingStatus =
        Beans.get(AppSupplychainService.class)
            .getAppSupplychain()
            .getIntercoPurchaseCreatingStatusSelect();
    if (purchaseOrder.getInterco()
        && intercoPurchaseCreatingStatus == PurchaseOrderRepository.STATUS_VALIDATED) {
      Beans.get(IntercoService.class).generateIntercoSaleFromPurchase(purchaseOrder);
    }

    budgetSupplychainService.updateBudgetLinesFromPurchaseOrder(purchaseOrder);
  }

  @Override
  @Transactional
  public void cancelPurchaseOrder(PurchaseOrder purchaseOrder) {
    super.cancelPurchaseOrder(purchaseOrder);

    if (Beans.get(AppSupplychainService.class).isApp("supplychain")) {
      budgetSupplychainService.updateBudgetLinesFromPurchaseOrder(purchaseOrder);

      if (purchaseOrder.getPurchaseOrderLineList() != null) {
        purchaseOrder.getPurchaseOrderLineList().stream()
            .forEach(poLine -> poLine.clearBudgetDistributionList());
      }
    }
  }
}
