package com.axelor.apps.gst.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.factory.CancelFactory;
import com.axelor.apps.account.service.invoice.factory.ValidateFactory;
import com.axelor.apps.account.service.invoice.factory.VentilateFactory;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.alarm.AlarmEngineService;
import com.axelor.apps.businessproject.service.InvoiceServiceProjectImpl;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class InvoiceServiceGSTImpl extends InvoiceServiceProjectImpl {

  @Inject private AddressService addressService;
  @Inject private InvoiceLineServiceGST invoiceLineServiceGST;

  @Inject
  public InvoiceServiceGSTImpl(
      ValidateFactory validateFactory,
      VentilateFactory ventilateFactory,
      CancelFactory cancelFactory,
      AlarmEngineService<Invoice> alarmEngineService,
      InvoiceRepository invoiceRepo,
      AppAccountService appAccountService,
      PartnerService partnerService,
      InvoiceLineService invoiceLineService) {
    super(
        validateFactory,
        ventilateFactory,
        cancelFactory,
        alarmEngineService,
        invoiceRepo,
        appAccountService,
        partnerService,
        invoiceLineService);
  }

  @Override
  public Invoice compute(Invoice invoice) throws AxelorException {
    super.compute(invoice);
    {
      System.out.println("call compute extednded methods..");
      BigDecimal totalNetAmt = BigDecimal.ZERO;
      BigDecimal totalIgst = BigDecimal.ZERO;
      BigDecimal totalSgst = BigDecimal.ZERO;
      BigDecimal totalCgst = BigDecimal.ZERO;
      BigDecimal totalgrossAmt = BigDecimal.ZERO;
      boolean isNullAddress = false;
      boolean isSameState = false;

      if (invoice.getCompany() == null
          || invoice.getCompany().getAddress() == null
          || invoice.getCompany().getAddress().getState() == null
          || invoice.getAddress() == null) {
        isNullAddress = true;
      } else {
        Address companyAddress = invoice.getCompany().getAddress();
        Address invoiceAddress = invoice.getAddress();
        //        Address shippingAddress = invoice.getShipingAddress();
        Address shippingAddress = invoice.getCompany().getAddress();
        isSameState =
            addressService.checkAddressStateForInvoice(
                companyAddress,
                invoiceAddress,
                shippingAddress,
                invoice.getIsUseInvoiceAddressAsShiping());
      }

      List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();

      if (invoice.getInvoiceLineList() != null && !invoice.getInvoiceLineList().isEmpty()) {

        for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
          InvoiceLine invoiceLinenew;
          invoiceLinenew =
              invoiceLineServiceGST.calculateInvoiceLine(invoiceLine, isSameState, isNullAddress);

          totalIgst = totalIgst.add(invoiceLinenew.getIgst());
          totalSgst = totalSgst.add(invoiceLinenew.getSgst());
          totalCgst = totalCgst.add(invoiceLinenew.getCgst());
          totalgrossAmt = totalgrossAmt.add(invoiceLinenew.getGrossAmount());
          invoiceLineList.add(invoiceLinenew);
        }
      }
      invoice.setNetAmount(totalNetAmt);
      invoice.setNetIGST(totalIgst);
      invoice.setNetSGST(totalSgst);
      invoice.setNetCGST(totalCgst);
      invoice.setGrossAmount(totalgrossAmt);

      invoice.setInvoiceLineList(invoiceLineList);
      return invoice;
    }
  }
}
