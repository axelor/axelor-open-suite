package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.FinancialDiscount;
import com.axelor.apps.account.db.Invoice;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class InvoiceFinancialDiscountServiceImpl implements InvoiceFinancialDiscountService {

  protected InvoiceService invoiceService;

  @Inject
  public InvoiceFinancialDiscountServiceImpl(InvoiceService invoiceService) {
    this.invoiceService = invoiceService;
  }

  @Override
  public void setFinancialDiscountInformations(Invoice invoice) {

    Objects.requireNonNull(invoice);

    if (invoice.getFinancialDiscount() != null) {
      FinancialDiscount financialDiscount = invoice.getFinancialDiscount();
      invoice.setLegalNotice(financialDiscount.getLegalNotice());
      invoice.setFinancialDiscountRate(financialDiscount.getDiscountRate());
      invoice.setFinancialDiscountTotalAmount(
          computeFinancialDiscountTotalAmount(invoice, financialDiscount));
      invoice.setRemainingAmountAfterFinDiscount(
          invoice.getInTaxTotal().subtract(invoice.getFinancialDiscountTotalAmount()));

      if (invoice.getDueDate() != null) {
        invoice.setFinancialDiscountDeadlineDate(
            invoiceService.getFinancialDiscountDeadlineDate(invoice, financialDiscount));
      }
    } else {
      resetFinancialDiscountInformations(invoice);
    }
  }

  protected BigDecimal computeFinancialDiscountTotalAmount(
      Invoice invoice, FinancialDiscount financialDiscount) {

    return financialDiscount
        .getDiscountRate()
        .multiply(invoice.getInTaxTotal())
        .divide(
            new BigDecimal(100),
            invoice.getFinancialDiscountTotalAmount().scale(),
            RoundingMode.HALF_UP);
  }

  @Override
  public void resetFinancialDiscountInformations(Invoice invoice) {

    Objects.requireNonNull(invoice);

    invoice.setLegalNotice(null);
    invoice.setFinancialDiscountRate(BigDecimal.ZERO);
    invoice.setFinancialDiscountTotalAmount(BigDecimal.ZERO);
    invoice.setRemainingAmountAfterFinDiscount(BigDecimal.ZERO);
  }
}
