package com.axelor.apps.gst.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.base.db.Address;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class InvoiceServiceImpl implements InvoiceService {
  @Inject private AddressService addressService;

  @Inject private InvoiceLineServiceGST invoiceLineService;

  DecimalFormat df = new DecimalFormat("###.###");

  @Override
  public Invoice calculate(Invoice invoice) {

    BigDecimal totalIgst = BigDecimal.ZERO;
    BigDecimal totalSgst = BigDecimal.ZERO;
    BigDecimal totalCgst = BigDecimal.ZERO;
    BigDecimal totalgrossAmt = BigDecimal.ZERO;
    boolean isNullAddress = false;
    boolean isSameState = false;

    //        if (invoice.getParty() == null
    //            || invoice.getCompany() == null
    //            || invoice.getCompany().getAddress() == null
    //            || invoice.getCompany().getAddress().getState() == null
    //            || invoice.getAddress() == null
    //            || invoice.getShipingAddress() == null
    //            || invoice.getShipingAddress().getState() == null) {
    //          isNullAddress = true;
    //        } else {
    Address companyAddress = invoice.getCompany().getAddress();
    Address invoiceAddress = invoice.getAddress();
    //          Address shippingAddress = invoice.getShipingAddress();
    Address shippingAddress = invoice.getCompany().getAddress();
    isSameState =
        addressService.checkAddressStateForInvoice(
            companyAddress,
            invoiceAddress,
            shippingAddress,
            invoice.getIsUseInvoiceAddressAsShiping());
    //        }
    //
    List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();

    if (invoice.getInvoiceLineList() != null && !invoice.getInvoiceLineList().isEmpty()) {

      for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
        InvoiceLine invoiceLinenew;
        invoiceLinenew =
            invoiceLineService.calculateInvoiceLine(invoiceLine, isSameState, isNullAddress);

        totalIgst = totalIgst.add(invoiceLinenew.getIgst());
        totalSgst = totalSgst.add(invoiceLinenew.getSgst());
        totalCgst = totalCgst.add(invoiceLinenew.getCgst());
        totalgrossAmt = totalgrossAmt.add(invoiceLinenew.getGrossAmount());
        invoiceLineList.add(invoiceLinenew);
      }
    }

    invoice.setNetIGST(totalIgst);
    invoice.setNetSGST(totalSgst);
    invoice.setNetCGST(totalCgst);
    invoice.setGrossAmount(totalgrossAmt);

    invoice.setInvoiceLineList(invoiceLineList);
    return invoice;
  }
}
