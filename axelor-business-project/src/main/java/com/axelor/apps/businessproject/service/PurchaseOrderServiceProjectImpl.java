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

  private AnalyticMoveLineRepository analyticMoveLineRepository;

  @Inject
  public PurchaseOrderServiceProjectImpl(
      AppSupplychainService appSupplychainService,
      AccountConfigService accountConfigService,
      AppAccountService appAccountService,
      AppBaseService appBaseService,
      PurchaseOrderStockService purchaseOrderStockService,
      BudgetSupplychainService budgetSupplychainService,
      AnalyticMoveLineRepository analyticMoveLineRepository) {
    super(
        appSupplychainService,
        accountConfigService,
        appAccountService,
        appBaseService,
        purchaseOrderStockService,
        budgetSupplychainService);
    this.analyticMoveLineRepository = analyticMoveLineRepository;
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

  @Transactional(rollbackOn = Exception.class)
  public PurchaseOrder updateLines(PurchaseOrder purchaseOrder) {
    for (PurchaseOrderLine orderLine : purchaseOrder.getPurchaseOrderLineList()) {
      orderLine.setProject(purchaseOrder.getProject());
      for (AnalyticMoveLine analyticMoveLine : orderLine.getAnalyticMoveLineList()) {
        analyticMoveLine.setProject(purchaseOrder.getProject());
        analyticMoveLineRepository.save(analyticMoveLine);
      }
    }
    return purchaseOrder;
  }
}
