package com.axelor.apps.businessproject.service;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.supplychain.service.BudgetSupplychainService;
import com.axelor.apps.supplychain.service.PurchaseOrderServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderStockService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PurchaseOrderServiceProjectImpl extends PurchaseOrderServiceSupplychainImpl {

  @Inject
  public PurchaseOrderServiceProjectImpl(
      AppSupplychainService appSupplychainService,
      AccountConfigService accountConfigService,
      AppAccountService appAccountService,
      AppBaseService appBaseService,
      PurchaseOrderStockService purchaseOrderStockService,
      BudgetSupplychainService budgetSupplychainService) {
    super(
        appSupplychainService,
        accountConfigService,
        appAccountService,
        appBaseService,
        purchaseOrderStockService,
        budgetSupplychainService);
  }

  @Override
  @Transactional
  public void cancelPurchaseOrder(PurchaseOrder purchaseOrder) {
    super.cancelPurchaseOrder(purchaseOrder);
    for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
      for (AnalyticMoveLine analyticMoveLine : purchaseOrderLine.getAnalyticMoveLineList()) {
        analyticMoveLine.setProject(null);
        Beans.get(AnalyticMoveLineRepository.class).save(analyticMoveLine);
      }
    }
  }
}
