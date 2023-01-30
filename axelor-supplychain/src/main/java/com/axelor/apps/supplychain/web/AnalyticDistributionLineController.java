package com.axelor.apps.supplychain.web;

import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class AnalyticDistributionLineController {

  public void linkWithOrderLine(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {
      Class<?> parentClass = request.getContext().getParent().getContextClass();
      if ((InvoiceLine.class).equals(parentClass)) {
        InvoiceLine invoiceLine = request.getContext().getParent().asType(InvoiceLine.class);
        response.setValue("saleOrderLine", invoiceLine.getSaleOrderLine());
        response.setValue("purchaseOrderLine", invoiceLine.getPurchaseOrderLine());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
