package com.axelor.apps.supplychain.service.invoice.tax;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.invoice.tax.InvoiceTaxComputeServiceImpl;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Optional;

public class InvoiceTaxComputeServiceSupplychainImpl extends InvoiceTaxComputeServiceImpl {

  @Inject
  public InvoiceTaxComputeServiceSupplychainImpl(CurrencyScaleService currencyScaleService) {
    super(currencyScaleService);
  }

  @Override
  public BigDecimal computeTaxAmount(
      InvoiceLineTax invoiceLineTax,
      BigDecimal exTaxBase,
      BigDecimal taxValue,
      BigDecimal inTaxTotal) {
    Invoice invoice = invoiceLineTax.getInvoice();
    BigDecimal taxAmount = super.computeTaxAmount(invoiceLineTax, exTaxBase, taxValue, inTaxTotal);

    if (invoice == null || invoice.getCompany() == null) {
      return taxAmount;
    }

    try {
      if ((InvoiceToolService.isPurchase(invoice)
              && Optional.of(invoice)
                  .map(Invoice::getPurchaseOrder)
                  .map(PurchaseOrder::getInAti)
                  .orElse(false))
          || (!InvoiceToolService.isPurchase(invoice)
              && Optional.of(invoice)
                  .map(Invoice::getSaleOrder)
                  .map(SaleOrder::getInAti)
                  .orElse(false))) {
        BigDecimal diff = taxAmount.subtract(inTaxTotal.subtract(exTaxBase)).abs();
        if (diff.compareTo(BigDecimal.ZERO) >= 0 && diff.compareTo(new BigDecimal("0.01")) <= 0) {
          return inTaxTotal.subtract(exTaxBase);
        }
      }
    } catch (AxelorException e) {
      TraceBackService.trace(e);
    }

    return taxAmount;
  }
}
