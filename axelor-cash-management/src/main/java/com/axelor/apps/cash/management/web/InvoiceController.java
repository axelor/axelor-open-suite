package com.axelor.apps.cash.management.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.cash.management.service.InvoiceEstimatedPaymentService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.time.LocalDate;

public class InvoiceController {

  public void fillEstimatedPaymentDate(ActionRequest request, ActionResponse response) {

    Invoice invoice = request.getContext().asType(Invoice.class);
    try {
      LocalDate estimatedPaymentDate =
          Beans.get(InvoiceEstimatedPaymentService.class).computeEstimatedPaymentDate(invoice);
      response.setValue("estimatedPaymentDate", estimatedPaymentDate);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
