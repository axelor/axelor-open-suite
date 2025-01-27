package com.axelor.apps.production.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.service.SaleOrderLineDetailsService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.helpers.ContextHelper;

public class SaleOrderLineDetailsController {

  public void productOnChange(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Context context = request.getContext();
    SaleOrderLineDetails saleOrderLineDetails = context.asType(SaleOrderLineDetails.class);
    SaleOrderLineDetailsService saleOrderLineDetailsService =
        Beans.get(SaleOrderLineDetailsService.class);
    SaleOrder saleOrder = getSaleOrder(context);
    response.setValues(
        saleOrderLineDetailsService.productOnChange(saleOrderLineDetails, saleOrder));
  }

  protected SaleOrder getSaleOrder(Context context) {
    SaleOrder saleOrder = ContextHelper.getOriginParent(context, SaleOrder.class);
    if (saleOrder == null) {
      saleOrder = context.getParent().asType(SaleOrderLine.class).getSaleOrder();
    }
    return saleOrder;
  }
}
