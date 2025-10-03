package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class PurchaseOrderChangeValidationSupplychainServiceImpl
    implements PurchaseOrderChangeValidationSupplychainService {

  protected final PurchaseOrderReceiptStateService purchaseOrderReceiptStateService;
  protected final PurchaseOrderStockService purchaseOrderStockService;
  protected final AppSupplychainService appSupplychainService;

  @Inject
  public PurchaseOrderChangeValidationSupplychainServiceImpl(
      PurchaseOrderReceiptStateService purchaseOrderReceiptStateService,
      PurchaseOrderStockService purchaseOrderStockService,
      AppSupplychainService appSupplychainService) {
    this.purchaseOrderReceiptStateService = purchaseOrderReceiptStateService;
    this.purchaseOrderStockService = purchaseOrderStockService;
    this.appSupplychainService = appSupplychainService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validatePurchaseOrderChange(PurchaseOrder purchaseOrder) throws AxelorException {

    purchaseOrderReceiptStateService.updatePurchaseOrderLineReceiptState(purchaseOrder);
    purchaseOrderReceiptStateService.updateReceiptState(purchaseOrder);
    if (appSupplychainService.getAppSupplychain().getSupplierStockMoveGenerationAuto()) {
      purchaseOrderStockService.createStockMoveFromPurchaseOrder(purchaseOrder);
    }
  }
}
