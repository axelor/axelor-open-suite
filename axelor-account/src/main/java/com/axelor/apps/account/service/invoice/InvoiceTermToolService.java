package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.AxelorException;
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

  InvoiceTerm getInvoiceTermToPay(
      InvoicePayment invoicePayment,
      List<InvoiceTerm> invoiceTermsToPay,
      BigDecimal amount,
      int counter,
      int size);

  List<InvoiceTerm> getPaymentVoucherInvoiceTerms(InvoicePayment invoicePayment, Invoice invoice)
      throws AxelorException;
}
