/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
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

  @Inject
  public DoubtfulCustomerInvoiceTermServiceImpl(
      InvoiceTermService invoiceTermService,
      MoveValidateService moveValidateService,
      ReconcileService reconcileService,
      InvoiceTermRepository invoiceTermRepo,
      MoveRepository moveRepo) {
    this.invoiceTermService = invoiceTermService;
    this.moveValidateService = moveValidateService;
    this.reconcileService = reconcileService;
    this.invoiceTermRepo = invoiceTermRepo;
    this.moveRepo = moveRepo;
  }

  public void createOrUpdateInvoiceTerms(
      Invoice invoice,
      Move newMove,
      List<MoveLine> invoicePartnerMoveLines,
      List<MoveLine> creditMoveLines,
      MoveLine debitMoveLine,
      LocalDate todayDate,
      BigDecimal amountRemaining)
      throws AxelorException {
    PaymentMode paymentMode = null;
    BankDetails bankDetails = null;
    User pfpUser = null;
    List<InvoiceTerm> invoiceTermToAdd = new ArrayList<>();
    List<InvoiceTerm> invoiceTermToRemove = new ArrayList<>();
    List<InvoiceTerm> invoiceTermToAddToDebitMoveLine = new ArrayList<>();

    for (MoveLine invoicePartnerMoveLine : invoicePartnerMoveLines) {

      List<InvoiceTerm> invoiceTermToAddToInvoicePartnerMoveLine = new ArrayList<>();
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
          invoiceTermToAdd.add(copyOnOldMoveLine);
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
    }

    if (creditMoveLines != null) {
      // Create invoice term on new credit move line
      int counter = 1;
      for (MoveLine creditMoveLine : creditMoveLines) {
        invoiceTermService.createInvoiceTerm(
            creditMoveLine,
            bankDetails,
            pfpUser,
            paymentMode,
            todayDate,
            creditMoveLine.getAmountRemaining(),
            counter++);

        newMove.getMoveLineList().add(creditMoveLine);
      }

      newMove.getMoveLineList().add(debitMoveLine);

      moveValidateService.accounting(newMove);
      moveRepo.save(newMove);

      for (MoveLine invoicePartnerMoveLine : invoicePartnerMoveLines) {
        MoveLine creditMoveLine =
            creditMoveLines.stream()
                .filter(
                    cml ->
                        cml.getCredit().equals(invoicePartnerMoveLine.getAmountRemaining())
                            && cml.getAccount().equals(invoicePartnerMoveLine.getAccount()))
                .findFirst()
                .orElse(null);
        if (creditMoveLine != null) {
          Reconcile reconcile =
              reconcileService.createReconcile(
                  invoicePartnerMoveLine,
                  creditMoveLine,
                  invoicePartnerMoveLine.getAmountRemaining(),
                  false);

          if (reconcile != null) {
            reconcileService.confirmReconcile(reconcile, true, true);
          }
        }
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
