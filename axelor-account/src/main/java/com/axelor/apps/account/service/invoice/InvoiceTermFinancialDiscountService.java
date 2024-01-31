package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.FinancialDiscount;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface InvoiceTermFinancialDiscountService {
  void computeFinancialDiscount(InvoiceTerm invoiceTerm, Invoice invoice);

  void computeFinancialDiscount(
      InvoiceTerm invoiceTerm,
      BigDecimal totalAmount,
      FinancialDiscount financialDiscount,
      BigDecimal financialDiscountAmount,
      BigDecimal remainingAmountAfterFinDiscount);

  BigDecimal computeCustomizedPercentageUnscaled(BigDecimal amount, BigDecimal inTaxTotal);

  void computeAmountRemainingAfterFinDiscount(InvoiceTerm invoiceTerm);

  LocalDate computeFinancialDiscountDeadlineDate(InvoiceTerm invoiceTerm);

  BigDecimal getFinancialDiscountTaxAmount(InvoiceTerm invoiceTerm);
}
