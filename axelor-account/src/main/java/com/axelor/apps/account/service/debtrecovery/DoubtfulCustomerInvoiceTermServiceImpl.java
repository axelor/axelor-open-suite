package com.axelor.apps.account.service.debtrecovery;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DoubtfulCustomerInvoiceTermServiceImpl implements DoubtfulCustomerInvoiceTermService {
  protected InvoiceTermService invoiceTermService;
  protected MoveValidateService moveValidateService;
  protected ReconcileService reconcileService;
  protected InvoiceTermRepository invoiceTermRepo;
  protected MoveRepository moveRepo;

  public void createOrUpdateInvoiceTerms(
      Invoice invoice,
      Move newMove,
      MoveLine invoicePartnerMoveLine,
      MoveLine creditMoveLine,
      MoveLine debitMoveLine,
      LocalDate todayDate,
      BigDecimal amountRemaining)
      throws AxelorException {
    PaymentMode paymentMode = null;
    BankDetails bankDetails = null;
    User pfpUser = null;
    List<InvoiceTerm> invoiceTermToAdd = new ArrayList<>();
    List<InvoiceTerm> invoiceTermToRemove = new ArrayList<>();
    List<InvoiceTerm> invoiceTermToAddToInvoicePartnerMoveLine = new ArrayList<>();
    List<InvoiceTerm> invoiceTermToAddToDebitMoveLine = new ArrayList<>();

    for (InvoiceTerm invoiceTerm : invoicePartnerMoveLine.getInvoiceTermList()) {
      paymentMode = invoiceTerm.getPaymentMode();
      bankDetails = invoiceTerm.getBankDetails();
      pfpUser = invoiceTerm.getPfpValidatorUser();

      if (invoiceTerm.getAmountRemaining().signum() > 0
          && invoiceTerm.getAmount().compareTo(invoiceTerm.getAmountRemaining()) == 0) {

        // Copy invoice term on put it on new move line
        InvoiceTerm copy = invoiceTermRepo.copy(invoiceTerm, false);
        invoiceTermToAddToDebitMoveLine.add(copy);
        invoiceTermToAdd.add(copy);

        // Remove invoice from old invoice term
        invoiceTermToRemove.add(invoiceTerm);
      }

      if (invoiceTerm.getAmountRemaining().signum() > 0
          && invoiceTerm.getAmount().compareTo(invoiceTerm.getAmountRemaining()) != 0) {
        BigDecimal amount = invoiceTerm.getAmount();
        BigDecimal remainingAmount = invoiceTerm.getAmountRemaining();
        BigDecimal percentage = invoiceTerm.getPercentage();
        // Create new invoice term on new move line with amount remaining
        InvoiceTerm copyOnNewMoveLine = invoiceTermRepo.copy(invoiceTerm, false);
        copyOnNewMoveLine.setAmount(remainingAmount);
        BigDecimal newPercentage =
            remainingAmount
                .multiply(percentage)
                .divide(amount, AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
        copyOnNewMoveLine.setPercentage(newPercentage);
        invoiceTermToAddToDebitMoveLine.add(copyOnNewMoveLine);
        invoiceTermToAdd.add(copyOnNewMoveLine);

        InvoiceTerm copyOnOldMoveLine = invoiceTermRepo.copy(invoiceTerm, false);
        copyOnOldMoveLine.setAmount(remainingAmount);
        copyOnOldMoveLine.setAmountRemaining(remainingAmount);
        copyOnOldMoveLine.setPercentage(newPercentage);
        invoiceTermToAddToInvoicePartnerMoveLine.add(copyOnOldMoveLine);
        invoice.addInvoiceTermListItem(copyOnOldMoveLine);
        invoiceTermToRemove.add(copyOnOldMoveLine);
        // Update current invoice term
        invoiceTerm.setAmount(amount.subtract(remainingAmount));
        invoiceTerm.setAmountRemaining(BigDecimal.ZERO);
        invoiceTerm.setIsPaid(true);
        invoiceTerm.setPercentage(percentage.subtract(newPercentage));
      }
    }

    for (InvoiceTerm it : invoiceTermToAddToInvoicePartnerMoveLine) {
      invoicePartnerMoveLine.addInvoiceTermListItem(it);
    }

    if (creditMoveLine != null) {
      // Create invoice term on new credit move line
      invoiceTermService.createInvoiceTerm(
          creditMoveLine, bankDetails, pfpUser, paymentMode, todayDate, amountRemaining);

      newMove.getMoveLineList().add(debitMoveLine);
      newMove.getMoveLineList().add(creditMoveLine);

      moveValidateService.accounting(newMove);
      moveRepo.save(newMove);

      Reconcile reconcile =
          reconcileService.createReconcile(
              invoicePartnerMoveLine, creditMoveLine, amountRemaining, false);

      if (reconcile != null) {
        reconcileService.confirmReconcile(reconcile, true);
      }
    }
    if (debitMoveLine.getInvoiceTermList() != null) {
      for (InvoiceTerm it : debitMoveLine.getInvoiceTermList()) {
        invoiceTermRepo.remove(it);
      }
      debitMoveLine.clearInvoiceTermList();
    }
    for (InvoiceTerm it : invoiceTermToAddToDebitMoveLine) {
      debitMoveLine.addInvoiceTermListItem(it);
    }
    // Recalculate percentage on debit moveline
    for (InvoiceTerm it : debitMoveLine.getInvoiceTermList()) {
      it.setPercentage(
          it.getAmount()
              .multiply(BigDecimal.valueOf(100))
              .divide(
                  amountRemaining, AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP));
    }

    if (invoice != null) {
      for (InvoiceTerm it : invoiceTermToAdd) {
        invoice.addInvoiceTermListItem(it);
      }

      for (InvoiceTerm it : invoiceTermToRemove) {
        invoice.removeInvoiceTermListItem(it);
        it.setInvoice(null);
      }
    }
  }
}
