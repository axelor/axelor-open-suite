package com.axelor.apps.budget.service.purchaseorder;

import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.service.AppBudgetService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.purchase.service.PurchaseOrderTypeSelectService;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.apps.supplychain.service.PurchaseOrderStockService;
import com.axelor.apps.supplychain.service.PurchaseOrderSupplychainService;
import com.axelor.apps.supplychain.service.PurchaseOrderWorkflowServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;

public class PurchaseOrderWorkflowBudgetServiceImpl
    extends PurchaseOrderWorkflowServiceSupplychainImpl {

  protected final AppBudgetService appBudgetService;
  protected final PurchaseOrderBudgetService purchaseOrderBudgetService;

  @Inject
  public PurchaseOrderWorkflowBudgetServiceImpl(
      PurchaseOrderService purchaseOrderService,
      PurchaseOrderRepository purchaseOrderRepo,
      AppPurchaseService appPurchaseService,
      AppSupplychainService appSupplychainService,
      PurchaseOrderStockService purchaseOrderStockService,
      AppAccountService appAccountService,
      PurchaseOrderSupplychainService purchaseOrderSupplychainService,
      PurchaseOrderTypeSelectService purchaseOrderTypeSelectService,
      AppBudgetService appBudgetService,
      PurchaseOrderBudgetService purchaseOrderBudgetService) {
    super(
        purchaseOrderService,
        purchaseOrderRepo,
        appPurchaseService,
        appSupplychainService,
        purchaseOrderStockService,
        appAccountService,
        purchaseOrderSupplychainService,
        purchaseOrderTypeSelectService);
    this.appBudgetService = appBudgetService;
    this.purchaseOrderBudgetService = purchaseOrderBudgetService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validatePurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException {
    super.validatePurchaseOrder(purchaseOrder);

    if (!appBudgetService.isApp("budget")) {
      return;
    }

    if (!appBudgetService.getAppBudget().getManageMultiBudget()) {
      purchaseOrderBudgetService.generateBudgetDistribution(purchaseOrder);
    }

    purchaseOrderBudgetService.updateBudgetLinesFromPurchaseOrder(purchaseOrder);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void cancelPurchaseOrder(PurchaseOrder purchaseOrder) throws AxelorException {
    super.cancelPurchaseOrder(purchaseOrder);

    if (appBudgetService.getAppBudget() != null) {
      purchaseOrderBudgetService.updateBudgetLinesFromPurchaseOrder(purchaseOrder);

      if (purchaseOrder.getPurchaseOrderLineList() != null) {
        purchaseOrder.getPurchaseOrderLineList().stream()
            .forEach(
                poLine -> {
                  poLine.clearBudgetDistributionList();
                  poLine.setBudget(null);
                });
      }
    }
  }
}
