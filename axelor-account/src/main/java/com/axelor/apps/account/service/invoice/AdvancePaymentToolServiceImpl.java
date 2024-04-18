package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AdvancePaymentToolServiceImpl implements AdvancePaymentToolService {

  protected AppAccountService appAccountService;
  protected MoveToolService moveToolService;

  @Inject
  public AdvancePaymentToolServiceImpl(
      AppAccountService appAccountService, MoveToolService moveToolService) {
    this.appAccountService = appAccountService;
    this.moveToolService = moveToolService;
  }

  @Override
  public List<MoveLine> getMoveLinesFromAdvancePayments(Invoice invoice) throws AxelorException {
    if (appAccountService.getAppAccount().getManageAdvancePaymentInvoice()) {
      return getMoveLinesFromInvoiceAdvancePayments(invoice);
    } else {
      return getMoveLinesFromSOAdvancePayments(invoice);
    }
  }

  @Override
  public List<MoveLine> getMoveLinesFromInvoiceAdvancePayments(Invoice invoice)
      throws AxelorException {
    List<MoveLine> advancePaymentMoveLines = new ArrayList<>();

    Set<Invoice> advancePayments = invoice.getAdvancePaymentInvoiceSet();
    List<InvoicePayment> invoicePayments;
    if (advancePayments == null || advancePayments.isEmpty()) {
      return advancePaymentMoveLines;
    }
    InvoicePaymentToolService invoicePaymentToolService =
        Beans.get(InvoicePaymentToolService.class);
    for (Invoice advancePayment : advancePayments) {
      invoicePayments = advancePayment.getInvoicePaymentList();
      // Since purchase order can have advance payment we check if it is a purchase or not
      // If it is a purchase, we must add debit lines from payment and not credit line.
      if (moveToolService.isDebitCustomer(invoice, true)) {
        List<MoveLine> creditMoveLines =
            invoicePaymentToolService.getMoveLinesFromPayments(invoicePayments, true);
        advancePaymentMoveLines.addAll(creditMoveLines);
      } else {
        List<MoveLine> debitMoveLines =
            invoicePaymentToolService.getMoveLinesFromPayments(invoicePayments, false);
        advancePaymentMoveLines.addAll(debitMoveLines);
      }
    }
    return advancePaymentMoveLines;
  }

  @Override
  public List<MoveLine> getMoveLinesFromSOAdvancePayments(Invoice invoice) {
    return new ArrayList<>();
  }
}
