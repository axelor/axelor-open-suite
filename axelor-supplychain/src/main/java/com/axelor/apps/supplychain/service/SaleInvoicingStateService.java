package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;

public interface SaleInvoicingStateService {
  int SALE_ORDER_INVOICE_NOT_INVOICED = 1;
  int SALE_ORDER_INVOICE_PARTIALLY_INVOICED = 2;
  int SALE_ORDER_INVOICE_INVOICED = 3;

  int getInvoicingState(
      BigDecimal amountInvoiced, BigDecimal exTaxTotal, boolean atLeastOneInvoiceIsVentilated);
}
