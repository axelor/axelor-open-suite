package com.axelor.apps.production.service;

import com.axelor.apps.base.service.DMSService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderLineRepository;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.service.PurchaseOrderCreateService;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.apps.supplychain.service.PurchaseOrderCreateSupplychainService;
import com.axelor.apps.supplychain.service.PurchaseOrderMergingServiceSupplyChainImpl;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderMergingServiceProductionImpl
    extends PurchaseOrderMergingServiceSupplyChainImpl {

  protected ManufOrderRepository manufOrderRepository;

  @Inject
  public PurchaseOrderMergingServiceProductionImpl(
      AppPurchaseService appPurchaseService,
      PurchaseOrderService purchaseOrderService,
      PurchaseOrderCreateService purchaseOrderCreateService,
      PurchaseOrderRepository purchaseOrderRepository,
      DMSService dmsService,
      PurchaseOrderLineRepository purchaseOrderLineRepository,
      PurchaseOrderCreateSupplychainService purchaseOrderCreateSupplychainService,
      ManufOrderRepository manufOrderRepository) {
    super(
        appPurchaseService,
        purchaseOrderService,
        purchaseOrderCreateService,
        purchaseOrderRepository,
        dmsService,
        purchaseOrderLineRepository,
        purchaseOrderCreateSupplychainService);
    this.manufOrderRepository = manufOrderRepository;
  }

  @Override
  protected PurchaseOrder updateDatabase(
      PurchaseOrder purchaseOrderMerged, List<PurchaseOrder> purchaseOrdersToMerge) {
    List<ManufOrder> manufOrderList = this.getManufOrdersOfPurchaseOrders(purchaseOrdersToMerge);
    manufOrderList.forEach(ManufOrder::clearPurchaseOrderSet);
    purchaseOrderMerged = super.updateDatabase(purchaseOrderMerged, purchaseOrdersToMerge);

    for (ManufOrder manufOrder : manufOrderList) {
      manufOrder.addPurchaseOrderSetItem(purchaseOrderMerged);
    }
    return purchaseOrderMerged;
  }

  protected List<ManufOrder> getManufOrdersOfPurchaseOrders(List<PurchaseOrder> purchaseOrderList) {
    List<ManufOrder> manufOrderList = new ArrayList<>();
    for (PurchaseOrder purchaseOrder : purchaseOrderList) {
      manufOrderList.addAll(
          manufOrderRepository
              .all()
              .filter(":purchaseOrder MEMBER OF self.purchaseOrderSet")
              .bind("purchaseOrder", purchaseOrder)
              .fetch());
    }
    return manufOrderList;
  }
}
