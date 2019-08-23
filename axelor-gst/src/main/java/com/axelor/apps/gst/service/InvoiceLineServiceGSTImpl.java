package com.axelor.apps.gst.service;

import com.axelor.apps.account.db.InvoiceLine;
import java.math.BigDecimal;

public class InvoiceLineServiceGSTImpl implements InvoiceLineServiceGST {

  @Override
  public InvoiceLine calculateInvoiceLine(
      InvoiceLine invoiceLine, Boolean isInvoiceIsShipping, Boolean isNullAddress) {

    BigDecimal netAmt = BigDecimal.ZERO;
    BigDecimal igst = BigDecimal.ZERO;
    BigDecimal sgst = BigDecimal.ZERO;
    BigDecimal cgst = BigDecimal.ZERO;
    BigDecimal finalGST = BigDecimal.ZERO;
    BigDecimal grossAmt = BigDecimal.ZERO;

    if (!isNullAddress) {
      if (invoiceLine.getQty() != null
          && invoiceLine.getPrice() != null
          && invoiceLine.getQty() != BigDecimal.ZERO) {
        BigDecimal qty = invoiceLine.getQty();
        BigDecimal price = invoiceLine.getPrice();
        netAmt = qty.multiply(price);
      }

      //      GST

      BigDecimal gstAmount = netAmt.multiply(invoiceLine.getGstRate().divide(new BigDecimal(100)));
      if (isInvoiceIsShipping) {
        // Net amount*GST rate/2:  if state is same in invoice address and company address on
        // invoice
        sgst = gstAmount.divide(new BigDecimal(2));
        cgst = gstAmount.divide(new BigDecimal(2));
      } else {
        igst = gstAmount;
      }
    }

    invoiceLine.setSgst(sgst);
    invoiceLine.setCgst(cgst);
    invoiceLine.setIgst(igst);

    // Net amount + (IGST or SGST + CGST)
    finalGST =
        finalGST.add(invoiceLine.getIgst()).add(invoiceLine.getSgst()).add(invoiceLine.getCgst());
    grossAmt = netAmt.add(finalGST);
    invoiceLine.setGrossAmount(grossAmt);
    return invoiceLine;
  }
}
