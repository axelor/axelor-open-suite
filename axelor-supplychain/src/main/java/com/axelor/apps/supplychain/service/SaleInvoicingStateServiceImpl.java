package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;

public class SaleInvoicingStateServiceImpl implements SaleInvoicingStateService {
  @Override
  public int getInvoicingState(
      BigDecimal amountInvoiced, BigDecimal exTaxTotal, boolean atLeastOneInvoiceIsVentilated) {
    int invoicingState = 0;

    if (amountInvoiced.compareTo(BigDecimal.ZERO) > 0) {
      if (amountInvoiced.compareTo(exTaxTotal) < 0) {
        invoicingState = SALE_ORDER_INVOICE_PARTIALLY_INVOICED;
      }
      if (amountInvoiced.compareTo(exTaxTotal) >= 0) {
        invoicingState = SALE_ORDER_INVOICE_INVOICED;
      }
    }

    if (amountInvoiced.compareTo(BigDecimal.ZERO) == 0) {
      if (atLeastOneInvoiceIsVentilated && exTaxTotal.compareTo(BigDecimal.ZERO) == 0) {
        invoicingState = SALE_ORDER_INVOICE_INVOICED;
      } else {
        invoicingState = SALE_ORDER_INVOICE_NOT_INVOICED;
      }
    }

    return invoicingState;
  }
}
