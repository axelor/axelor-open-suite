package com.axelor.apps.sale.web;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingService.SaleOrderMergingResult;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingViewService;
import com.axelor.apps.tool.MapTools;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderMergingController {

  public void mergeSaleOrder(ActionRequest request, ActionResponse response) {

    String lineToMerge;
    if (request.getContext().get("saleQuotationToMerge") != null) {
      lineToMerge = "saleQuotationToMerge";
    } else {
      lineToMerge = "saleOrderToMerge";
    }
    try {
      List<SaleOrder> saleOrdersToMerge =
          MapTools.makeList(SaleOrder.class, request.getContext().get(lineToMerge));
      if (CollectionUtils.isNotEmpty(saleOrdersToMerge)) {
        SaleOrderMergingResult result =
            Beans.get(SaleOrderMergingService.class).mergeSaleOrders(saleOrdersToMerge);
        if (result.isConfirmationNeeded()) {
          ActionViewBuilder confirmView =
              Beans.get(SaleOrderMergingViewService.class)
                  .buildConfirmView(result, lineToMerge, saleOrdersToMerge);
          response.setView(confirmView.map());
          return;
        }
        if (result.getSaleOrder() != null) {
          // Open the generated sale order in a new tab
          response.setView(
              ActionView.define("Sale order")
                  .model(SaleOrder.class.getName())
                  .add("grid", "sale-order-grid")
                  .add("form", "sale-order-form")
                  .param("search-filters", "sale-order-filters")
                  .param("forceEdit", "true")
                  .context("_showRecord", String.valueOf(result.getSaleOrder().getId()))
                  .map());
          response.setCanClose(true);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void mergeSaleOrderFromPopUp(ActionRequest request, ActionResponse response) {
    String lineToMerge;
    if (request.getContext().get("saleQuotationToMerge") != null) {
      lineToMerge = "saleQuotationToMerge";
    } else {
      lineToMerge = "saleOrderToMerge";
    }
    try {
      List<SaleOrder> saleOrdersToMerge =
          MapTools.makeList(SaleOrder.class, request.getContext().get(lineToMerge));
      if (CollectionUtils.isNotEmpty(saleOrdersToMerge)) {
        SaleOrderMergingResult result =
            Beans.get(SaleOrderMergingService.class)
                .mergeSaleOrdersWithContext(saleOrdersToMerge, request.getContext());
        if (result.getSaleOrder() != null) {
          // Open the generated sale order in a new tab
          response.setView(
              ActionView.define("Sale order")
                  .model(SaleOrder.class.getName())
                  .add("grid", "sale-order-grid")
                  .add("form", "sale-order-form")
                  .param("search-filters", "sale-order-filters")
                  .param("forceEdit", "true")
                  .context("_showRecord", String.valueOf(result.getSaleOrder().getId()))
                  .map());
          response.setCanClose(true);
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
