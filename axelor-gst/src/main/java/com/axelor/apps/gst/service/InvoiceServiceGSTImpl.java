package com.axelor.apps.gst.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.factory.CancelFactory;
import com.axelor.apps.account.service.invoice.factory.ValidateFactory;
import com.axelor.apps.account.service.invoice.factory.VentilateFactory;
import com.axelor.apps.account.service.invoice.generator.line.InvoiceLineManagement;
import com.axelor.apps.account.service.invoice.generator.tax.TaxInvoiceLine;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.alarm.AlarmEngineService;
import com.axelor.apps.businessproject.service.InvoiceServiceProjectImpl;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class InvoiceServiceGSTImpl extends InvoiceServiceProjectImpl implements InvoiceServiceGST {

  @Inject private AddressService addressService;
  @Inject private InvoiceLineServiceGST invoiceLineServiceGST;
  @Inject private ProductRepository productRepository;
  @Inject private InvoiceRepository invoiceRepository;

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
    invoice = super.compute(invoice);

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

    if (invoice.getInvoiceLineList() != null && !invoice.getInvoiceLineList().isEmpty()) {

      for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
        invoiceLine =
            invoiceLineServiceGST.calculateInvoiceLine(invoiceLine, isSameState, isNullAddress);
        totalIgst = totalIgst.add(invoiceLine.getIgst());
        totalSgst = totalSgst.add(invoiceLine.getSgst());
        totalCgst = totalCgst.add(invoiceLine.getCgst());
        totalgrossAmt = totalgrossAmt.add(invoiceLine.getGrossAmount());
        // invoiceLineList.add(invoiceLinenew);
      }
    }
    invoice.setNetAmount(totalNetAmt.setScale(2, RoundingMode.HALF_UP));
    invoice.setNetIGST(totalIgst.setScale(2, RoundingMode.HALF_UP));
    invoice.setNetSGST(totalSgst.setScale(2, RoundingMode.HALF_UP));
    invoice.setNetCGST(totalCgst.setScale(2, RoundingMode.HALF_UP));
    invoice.setGrossAmount(totalgrossAmt.setScale(2, RoundingMode.HALF_UP));

    // invoice.setInvoiceLineList(invoiceLineList);
    return invoice;
  }

  @Override
  public Invoice calculate(Invoice invoice) throws AxelorException {

    List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
    for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {

      Product product = invoiceLine.getProduct();
      invoiceLine =
          Mapper.toBean(
              InvoiceLine.class, invoiceLineService.fillProductInformation(invoice, invoiceLine));

      invoiceLine.setProduct(product);

      /* set ex_tax  and in_tax*/

      BigDecimal exTaxTotal;
      BigDecimal companyExTaxTotal;
      BigDecimal inTaxTotal;
      BigDecimal companyInTaxTotal;
      BigDecimal priceDiscounted =
          invoiceLineService.computeDiscount(invoiceLine, invoice.getInAti());

      invoiceLine.setQty(new BigDecimal(1));

      BigDecimal taxRate = BigDecimal.ZERO;
      if (invoiceLine.getTaxLine() != null) {
        taxRate = invoiceLine.getTaxLine().getValue();
      }

      if (!invoice.getInAti()) {
        exTaxTotal = InvoiceLineManagement.computeAmount(invoiceLine.getQty(), priceDiscounted);
        inTaxTotal = exTaxTotal.add(exTaxTotal.multiply(taxRate));
      } else {
        inTaxTotal = InvoiceLineManagement.computeAmount(invoiceLine.getQty(), priceDiscounted);
        exTaxTotal = inTaxTotal.divide(taxRate.add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
      }

      companyExTaxTotal = invoiceLineService.getCompanyExTaxTotal(exTaxTotal, invoice);
      companyInTaxTotal = invoiceLineService.getCompanyExTaxTotal(inTaxTotal, invoice);

      invoiceLine.setExTaxTotal(exTaxTotal.setScale(2, RoundingMode.HALF_UP));
      invoiceLine.setInTaxTotal(inTaxTotal.setScale(2, RoundingMode.HALF_UP));
      invoiceLine.setCompanyExTaxTotal(companyExTaxTotal.setScale(2, RoundingMode.HALF_UP));
      invoiceLine.setCompanyInTaxTotal(companyInTaxTotal.setScale(2, RoundingMode.HALF_UP));
      invoiceLineList.add(invoiceLine);
      /* ................................................End
      tax.................................................................................*/

    }

    invoice.setInvoiceLineList(invoiceLineList);
    //     Create tax lines.
    List<InvoiceLineTax> invoiceTaxLines =
        (new TaxInvoiceLine(invoice, invoice.getInvoiceLineList())).creates();

    invoice.setInvoiceLineTaxList(invoiceTaxLines);
    Invoice invoiceNew = compute(invoice);

    return invoiceNew;
  }

  @Override
  public Invoice setInvoiceDetails(Invoice invoice, List<Long> productIds, Integer partnerId)
      throws AxelorException {

    List<Product> productList =
        productRepository.all().filter("self.id in (?1)", productIds).fetch();
    List<InvoiceLine> invoiceLineList =
        invoiceLineServiceGST.getInvoiceLineFromProduct(productList);
    Partner partner =
        Beans.get(PartnerRepository.class).all().filter("self.id = ?", partnerId).fetchOne();

    invoice.setInvoiceLineList(invoiceLineList);
    invoice.setPartner(partner);
    invoice.setAddress(partnerService.getInvoicingAddress(partner));

    invoice = calculate(invoice);
    return invoice;
  }

  @Override
  @Transactional
  public Long saveInvoice(Invoice invoice) throws AxelorException {

    Invoice invoiceTemp = invoiceRepository.save(calculate(invoice));
    return invoiceTemp.getId();
  }
}
