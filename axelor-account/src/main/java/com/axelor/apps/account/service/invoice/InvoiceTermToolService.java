package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.MoveLine;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface InvoiceTermToolService {
  boolean isPartiallyPaid(InvoiceTerm invoiceTerm);

  boolean isEnoughAmountToPay(List<InvoiceTerm> invoiceTermList, BigDecimal amount, LocalDate date);

  boolean isNotReadonly(InvoiceTerm invoiceTerm);

  boolean isNotReadonlyExceptPfp(InvoiceTerm invoiceTerm);

  BigDecimal getAmountRemaining(InvoiceTerm invoiceTerm, LocalDate date, boolean isCompanyCurrency);

  boolean isThresholdNotOnLastUnpaidInvoiceTerm(
      MoveLine moveLine, BigDecimal thresholdDistanceFromRegulation);

  BigDecimal computeCustomizedPercentage(BigDecimal amount, BigDecimal inTaxTotal);

  BigDecimal computeCustomizedPercentageUnscaled(BigDecimal amount, BigDecimal inTaxTotal);

  List<InvoiceTerm> getInvoiceTerms(List<Long> invoiceTermIds);
}
