package com.axelor.apps.gst.service;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.TaxLineRepository;
import com.axelor.apps.account.db.repo.TaxRepository;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.service.SaleOrderLineServiceSupplyChainImpl;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class SaleOrderLineServiceGSTImpl extends SaleOrderLineServiceSupplyChainImpl {

  @Inject private AddressService addressService;
  @Inject private TaxLineRepository taxLineRepository;
  @Inject private TaxRepository taxRepository;

  @Override
  public void fillPrice(SaleOrderLine saleOrderLine, SaleOrder saleOrder, Integer packPriceSelect)
      throws AxelorException {
    System.out.println(saleOrderLine.getProduct().getGstRate());

    System.out.println("call extended method..");
    super.fillPrice(saleOrderLine, saleOrder, packPriceSelect);
    System.out.println(saleOrderLine.getTaxLine());
    //    set tax Line for product base on GSTRate  if product have no taxLine
    if (saleOrderLine.getTaxLine() == null) {
      long id =
          (long)
              (taxRepository.findByCode("GST") != null
                  ? taxRepository.findByCode("GST").getId()
                  : 0);

      TaxLine taxLine =
          taxLineRepository
              .all()
              .filter("self.value = :value and self.tax = :taxId")
              .bind("value", saleOrderLine.getGstRate().divide(new BigDecimal(100)))
              .bind("taxId", id)
              .fetchOne();

      if (taxLine != null) {
        //              productInformation.remove("error");
        saleOrderLine.setTaxLine(taxLine);
        //              productInformation.put("taxLine", taxLine);
      }
    }

    boolean isNullAddress = false;
    boolean isSameState = false;
    System.out.println(saleOrderLine.getTaxLine());
    //
    if (saleOrder.getCompany() == null
        || saleOrder.getCompany() == null
        || saleOrder.getCompany().getAddress() == null
        || saleOrder.getCompany().getAddress().getState() == null
        || saleOrder.getMainInvoicingAddress() == null
        || saleOrder.getMainInvoicingAddress().getState() == null) {
      System.err.println("address..is null extends..method..");
      isNullAddress = true;
    } else {
      Address companyAddress = saleOrder.getCompany().getAddress();
      Address invoiceAddress = saleOrder.getMainInvoicingAddress();
      Address shippingAddress = saleOrder.getDeliveryAddress();

      isSameState =
          addressService.checkAddressStateForInvoice(
              companyAddress, invoiceAddress, shippingAddress, true);
      //      saleOrder.getIsUseInvoiceAddressAsShiping()
    }

    saleOrderLine = calculatesaleOrderLine(saleOrderLine, isSameState, isNullAddress);
  }

  public SaleOrderLine calculatesaleOrderLine(
      SaleOrderLine saleOrderLine, Boolean isInvoiceIsShipping, Boolean isNullAddress) {

    BigDecimal netAmt = BigDecimal.ZERO;
    BigDecimal igst = BigDecimal.ZERO;
    BigDecimal sgst = BigDecimal.ZERO;
    BigDecimal cgst = BigDecimal.ZERO;
    BigDecimal finalGST = BigDecimal.ZERO;
    BigDecimal grossAmt = BigDecimal.ZERO;

    if (!isNullAddress) {

      BigDecimal gstAmount =
          saleOrderLine.getPrice().multiply(saleOrderLine.getGstRate().divide(new BigDecimal(100)));
      if (isInvoiceIsShipping) {
        sgst = gstAmount.divide(new BigDecimal(2));
        cgst = gstAmount.divide(new BigDecimal(2));
      } else {
        igst = gstAmount;
      }
    }

    saleOrderLine.setSgst(sgst);
    saleOrderLine.setCgst(cgst);
    saleOrderLine.setIgst(igst);

    finalGST =
        finalGST
            .add(saleOrderLine.getIgst())
            .add(saleOrderLine.getSgst())
            .add(saleOrderLine.getCgst());
    grossAmt = netAmt.add(finalGST);
    saleOrderLine.setGrossAmount(grossAmt);
    return saleOrderLine;
  }

  @Override
  public TaxLine getTaxLine(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {

    System.err.println(
        "getTaxLinegetTaxLinegetTaxLinegetTaxLinegetTaxLinegetTaxLinegetTaxLinegetTaxLine");
    TaxLine taxLine = null;
    try {
      taxLine =
          Beans.get(AccountManagementService.class)
              .getTaxLine(
                  saleOrder.getCreationDate(),
                  saleOrderLine.getProduct(),
                  saleOrder.getCompany(),
                  saleOrder.getClientPartner().getFiscalPosition(),
                  false);
    } catch (Exception e) {

      System.out.println(saleOrderLine.getGstRate());
      System.out.println(saleOrderLine.getProduct().getGstRate());

      if (saleOrderLine.getProduct().getGstRate() != null) {
        long id =
            (long)
                (taxRepository.findByCode("GST") != null
                    ? taxRepository.findByCode("GST").getId()
                    : 0);
        System.out.println(saleOrderLine.getGstRate().divide(new BigDecimal(100)));
        System.out.println(saleOrderLine.getProduct().getGstRate().divide(new BigDecimal(100)));
        taxLine =
            taxLineRepository
                .all()
                .filter("self.value = :value and self.tax = :taxId")
                .bind("value", saleOrderLine.getProduct().getGstRate().divide(new BigDecimal(100)))
                .bind("taxId", id)
                .fetchOne();
        System.out.println(taxLine);
      }
    }

    return taxLine;
  }
}
