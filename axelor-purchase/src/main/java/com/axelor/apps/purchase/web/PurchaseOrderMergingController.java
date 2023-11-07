package com.axelor.apps.purchase.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.service.PurchaseOrderMergingService;
import com.axelor.apps.purchase.service.PurchaseOrderMergingService.PurchaseOrderMergingResult;
import com.axelor.apps.purchase.service.PurchaseOrderMergingViewService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.helpers.MapHelper;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class PurchaseOrderMergingController {
  // Generate single purchase order from several

  public void mergePurchaseOrder(ActionRequest request, ActionResponse response) {
    try {
      List<PurchaseOrder> purchaseOrdersToMerge =
          MapHelper.getCollection(
              request.getContext(), PurchaseOrder.class, "purchaseOrderToMerge");
      if (CollectionUtils.isNotEmpty(purchaseOrdersToMerge)) {
        PurchaseOrderMergingResult result =
            Beans.get(PurchaseOrderMergingService.class).mergePurchaseOrders(purchaseOrdersToMerge);
        if (result.isConfirmationNeeded()) {
          ActionView.ActionViewBuilder confirmView =
              Beans.get(PurchaseOrderMergingViewService.class)
                  .buildConfirmView(result, purchaseOrdersToMerge);
          response.setView(confirmView.map());
          return;
        }
        setResponseView(response, result);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void mergePurchaseOrderFromPopUp(ActionRequest request, ActionResponse response) {
    try {
      List<PurchaseOrder> purchaseOrdersToMerge =
          MapHelper.getCollection(
              request.getContext(), PurchaseOrder.class, "purchaseOrderToMerge");
      if (CollectionUtils.isNotEmpty(purchaseOrdersToMerge)) {
        PurchaseOrderMergingResult result =
            Beans.get(PurchaseOrderMergingService.class)
                .mergePurchaseOrdersWithContext(purchaseOrdersToMerge, request.getContext());
        setResponseView(response, result);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  protected void setResponseView(ActionResponse response, PurchaseOrderMergingResult result) {
    if (result.getPurchaseOrder() != null) {
      // Open the generated purchase order in a new tab
      response.setView(
          ActionView.define(I18n.get("Purchase order"))
              .model(PurchaseOrder.class.getName())
              .add("grid", "purchase-order-grid")
              .add("form", "purchase-order-form")
              .param("search-filters", "purchase-order-filters")
              .param("forceEdit", "true")
              .context("_showRecord", String.valueOf(result.getPurchaseOrder().getId()))
              .map());
      response.setCanClose(true);
    }
  }
}
