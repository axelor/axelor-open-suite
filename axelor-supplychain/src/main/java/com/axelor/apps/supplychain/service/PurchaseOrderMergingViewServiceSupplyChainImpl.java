package com.axelor.apps.supplychain.service;

import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.service.PurchaseOrderMergingService;
import com.axelor.apps.purchase.service.PurchaseOrderMergingService.PurchaseOrderMergingResult;
import com.axelor.apps.purchase.service.PurchaseOrderMergingViewServiceImpl;
import com.axelor.apps.purchase.service.app.AppPurchaseService;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.google.inject.Inject;
import java.util.List;

public class PurchaseOrderMergingViewServiceSupplyChainImpl
    extends PurchaseOrderMergingViewServiceImpl {

  protected AppPurchaseService appPurchaseService;
  protected PurchaseOrderMergingServiceSupplyChainImpl purchaseOrderMergingSupplyChainService;

  @Inject
  public PurchaseOrderMergingViewServiceSupplyChainImpl(
      PurchaseOrderMergingService purchaseOrderMergingService,
      AppPurchaseService appPurchaseService,
      PurchaseOrderMergingServiceSupplyChainImpl purchaseOrderMergingSupplyChainService) {
    super(purchaseOrderMergingService);
    this.appPurchaseService = appPurchaseService;
    this.purchaseOrderMergingSupplyChainService = purchaseOrderMergingSupplyChainService;
  }

  @Override
  public ActionViewBuilder buildConfirmView(
      PurchaseOrderMergingResult result, List<PurchaseOrder> purchaseOrdersToMerge) {

    ActionViewBuilder confirmView = super.buildConfirmView(result, purchaseOrdersToMerge);
    if (purchaseOrderMergingSupplyChainService.getChecks(result).isExistStockLocationDiff()) {
      confirmView.context("contextLocationToCheck", Boolean.TRUE.toString());
    }
    return confirmView;
  }
}
