package com.axelor.apps.account.service.invoice.tax;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class InvoiceTaxComputeServiceImpl implements InvoiceTaxComputeService {

  protected CurrencyScaleService currencyScaleService;

  @Inject
  public InvoiceTaxComputeServiceImpl(CurrencyScaleService currencyScaleService) {
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  public void recomputeInvoiceTaxAmounts(Invoice invoice) {
    // In the invoice currency
    invoice.setTaxTotal(BigDecimal.ZERO);
    invoice.setInTaxTotal(BigDecimal.ZERO);

    // In the company accounting currency
    invoice.setCompanyTaxTotal(BigDecimal.ZERO);
    invoice.setCompanyInTaxTotal(BigDecimal.ZERO);

    for (InvoiceLineTax invoiceLineTax : invoice.getInvoiceLineTaxList()) {
      // In the invoice currency
      invoice.setTaxTotal(
          currencyScaleService.getScaledValue(
              invoice, invoice.getTaxTotal().add(invoiceLineTax.getTaxTotal())));

      // In the company accounting currency
      invoice.setCompanyTaxTotal(
          currencyScaleService.getCompanyScaledValue(
              invoice, invoice.getCompanyTaxTotal().add(invoiceLineTax.getCompanyTaxTotal())));
    }

    // In the invoice currency
    invoice.setInTaxTotal(
        currencyScaleService.getScaledValue(
            invoice, invoice.getExTaxTotal().add(invoice.getTaxTotal())));

    // In the company accounting currency
    invoice.setCompanyInTaxTotal(
        currencyScaleService.getCompanyScaledValue(
            invoice, invoice.getCompanyExTaxTotal().add(invoice.getCompanyTaxTotal())));
    invoice.setCompanyInTaxTotalRemaining(invoice.getCompanyInTaxTotal());

    invoice.setAmountRemaining(invoice.getInTaxTotal());
  }
}
