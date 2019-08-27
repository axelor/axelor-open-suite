package com.axelor.apps.gst.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.gst.service.InvoiceService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class InvoiceController {

  @Inject private InvoiceService invoiceService;

  public void calculate(ActionRequest req, ActionResponse res) {
    try {

      Invoice invoice = req.getContext().asType(Invoice.class);

      Invoice invoiceCalculated = invoiceService.calculate(invoice);

      res.setValue("invoiceLineList", invoiceCalculated.getInvoiceLineList());
      res.setValue("netAmount", invoiceCalculated.getNetAmount());
      res.setValue("netIGST", invoiceCalculated.getNetIGST());
      res.setValue("netSGST", invoiceCalculated.getNetSGST());
      res.setValue("netCGST", invoiceCalculated.getNetCGST());
      res.setValue("grossAmount", invoiceCalculated.getGrossAmount());
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Error in Invoice controller calculate::" + e.getMessage());
    }
  }
}
