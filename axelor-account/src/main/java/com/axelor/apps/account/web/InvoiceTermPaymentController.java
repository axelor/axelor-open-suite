package com.axelor.apps.account.web;

import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.service.invoice.InvoiceTermPaymentGroupService;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class InvoiceTermPaymentController {

  public void onLoad(ActionRequest request, ActionResponse response) {
    try {
      InvoiceTermPayment invoiceTermPayment = request.getContext().asType(InvoiceTermPayment.class);

      response.setAttrs(
          Beans.get(InvoiceTermPaymentGroupService.class).getOnLoadAttrsMap(invoiceTermPayment));
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
