package com.axelor.apps.production.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.service.SaleOrderLineDetailsPriceService;
import com.axelor.apps.production.service.SaleOrderLineDetailsService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineUtils;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.helpers.ContextHelper;
import java.util.HashMap;
import java.util.Map;

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

  public void computePrices(ActionRequest request, ActionResponse response) throws AxelorException {
    Context context = request.getContext();
    SaleOrderLineDetails saleOrderLineDetails = context.asType(SaleOrderLineDetails.class);
    SaleOrder saleOrder = getSaleOrder(context);
    response.setValues(
        Beans.get(SaleOrderLineDetailsPriceService.class)
            .computePrices(saleOrderLineDetails, saleOrder));
  }

  public void marginCoefOnChange(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Context context = request.getContext();
    SaleOrderLineDetails saleOrderLineDetails = context.asType(SaleOrderLineDetails.class);
    SaleOrderLineDetailsPriceService saleOrderLineDetailsPriceService =
        Beans.get(SaleOrderLineDetailsPriceService.class);
    SaleOrder saleOrder = getSaleOrder(context);
    Map<String, Object> values = new HashMap<>();
    values.putAll(saleOrderLineDetailsPriceService.computePrice(saleOrderLineDetails));
    values.putAll(
        saleOrderLineDetailsPriceService.computeTotalPrice(saleOrderLineDetails, saleOrder));
    response.setValues(values);
  }

  protected SaleOrder getSaleOrder(Context context) {
    SaleOrder saleOrder = ContextHelper.getOriginParent(context, SaleOrder.class);
    if (saleOrder == null) {
      saleOrder =
          SaleOrderLineUtils.getParentSol(context.getParent().asType(SaleOrderLine.class))
              .getSaleOrder();
    }
    return saleOrder;
  }
}
