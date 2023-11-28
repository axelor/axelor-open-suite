package com.axelor.apps.contract.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.contract.service.InvoiceLinePricingService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class InvoiceController {
  public void computePricing(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Invoice invoice = request.getContext().asType(Invoice.class);
    Beans.get(InvoiceLinePricingService.class).computePricing(invoice);
    response.setValue("invoiceLineList", invoice.getInvoiceLineList());
  }
}
