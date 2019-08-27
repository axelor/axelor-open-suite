package com.axelor.apps.gst.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.gst.service.AddressService;
import com.axelor.apps.gst.service.InvoiceLineServiceGST;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class InvoiceLineController {

  @Inject private InvoiceLineServiceGST invoiceLineService;
  @Inject private AddressService addressService;

  public void calculate(ActionRequest req, ActionResponse res) {
    try {

      InvoiceLine invoiceLine = req.getContext().asType(InvoiceLine.class);
      Invoice invoice = req.getContext().getParent().asType(Invoice.class);
      boolean isNullAddress = false;
      boolean isSameState = false;

      if (invoice.getCompany() == null
          || invoice.getCompany() == null
          || invoice.getCompany().getAddress() == null
          || invoice.getCompany().getAddress().getState() == null
          || invoice.getAddress() == null
          || invoice.getAddress().getState() == null) {
        System.out.println("address..is null");
        isNullAddress = true;
      } else {
        Address companyAddress = invoice.getCompany().getAddress();
        Address invoiceAddress = invoice.getAddress();
        //              Address shippingAddress = invoice.getShipingAddress();
        Address shippingAddress = invoice.getCompany().getAddress();

        isSameState =
            addressService.checkAddressStateForInvoice(
                companyAddress,
                invoiceAddress,
                shippingAddress,
                invoice.getIsUseInvoiceAddressAsShiping());
      }

      invoiceLine =
          invoiceLineService.calculateInvoiceLine(invoiceLine, isSameState, isNullAddress);

      res.setValue("igst", invoiceLine.getIgst());
      res.setValue("sgst", invoiceLine.getSgst());
      res.setValue("cgst", invoiceLine.getCgst());
      res.setValue("grossAmount", invoiceLine.getGrossAmount());

    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Error in Invoice controller calculate::" + e.getMessage());
    }
  }
}
