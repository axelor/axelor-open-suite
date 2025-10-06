package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseRequest;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.db.repo.PurchaseRequestRepository;
import com.axelor.apps.purchase.service.PurchaseOrderCreateService;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.purchase.service.purchase.request.PurchaseRequestToPoCreateServiceImpl;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class PurchaseRequestToPoCreateServiceSupplychainImpl
    extends PurchaseRequestToPoCreateServiceImpl {

  @Inject
  public PurchaseRequestToPoCreateServiceSupplychainImpl(
      PurchaseOrderService purchaseOrderService,
      PurchaseOrderCreateService purchaseOrderCreateService,
      PurchaseOrderLineService purchaseOrderLineService,
      PurchaseOrderRepository purchaseOrderRepo,
      PurchaseRequestRepository purchaseRequestRepo,
      AppBaseService appBaseService) {
    super(
        purchaseOrderService,
        purchaseOrderCreateService,
        purchaseOrderLineService,
        purchaseOrderRepo,
        purchaseRequestRepo,
        appBaseService);
  }

  @Override
  protected PurchaseOrder createPurchaseOrder(PurchaseRequest purchaseRequest)
      throws AxelorException {
    PurchaseOrder purchaseOrder = super.createPurchaseOrder(purchaseRequest);
    if (appBaseService.isApp("supplychain")) {
      purchaseOrder.setStockLocation(purchaseRequest.getStockLocation());
    }
    return purchaseOrder;
  }

  @Override
  protected String getGroupBySupplierKey(PurchaseRequest purchaseRequest) {
    String key = super.getGroupBySupplierKey(purchaseRequest);

    if (!Beans.get(AppSupplychainService.class).isApp("supplychain")) {
      return key;
    }

    StockLocation stockLocation = purchaseRequest.getStockLocation();
    if (stockLocation != null) {
      key = key + "_" + stockLocation.getId().toString();
    }
    return key;
  }
}
