package com.axelor.apps.sale.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.base.service.tax.OrderLineTaxService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLineTax;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class SaleOrderLineTaxController {

  @ErrorException
  public void recomputeAmounts(ActionRequest request, ActionResponse response)
      throws AxelorException {
    SaleOrderLineTax saleOrderLineTax = request.getContext().asType(SaleOrderLineTax.class);
    if (!Beans.get(OrderLineTaxService.class).isManageByAmount(saleOrderLineTax)) {
      return;
    }

    SaleOrder saleOrder = saleOrderLineTax.getSaleOrder();
    if (saleOrder == null) {
      saleOrder = request.getContext().getParent().asType(SaleOrder.class);
    }

    response.setValue(
        "inTaxTotal",
        Beans.get(OrderLineTaxService.class)
            .computeInTaxTotal(saleOrderLineTax, saleOrder.getCurrency()));
  }
}
