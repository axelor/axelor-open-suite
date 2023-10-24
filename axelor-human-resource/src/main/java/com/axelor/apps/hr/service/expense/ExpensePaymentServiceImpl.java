/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.expense;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveCancelService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.bankorder.BankOrderCreateServiceHr;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;

@Singleton
public class ExpensePaymentServiceImpl implements ExpensePaymentService {

  protected BankOrderCreateServiceHr bankOrderCreateServiceHr;
  protected AccountConfigService accountConfigService;
  protected PaymentModeService paymentModeService;
  protected MoveCreateService moveCreateService;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveValidateService moveValidateService;
  protected ReconcileService reconcileService;
  protected BankOrderService bankOrderService;
  protected MoveCancelService moveCancelService;
  protected BankOrderRepository bankOrderRepository;
  protected ExpenseRepository expenseRepository;

  @Inject
  public ExpensePaymentServiceImpl(
      BankOrderCreateServiceHr bankOrderCreateServiceHr,
      AccountConfigService accountConfigService,
      PaymentModeService paymentModeService,
      MoveCreateService moveCreateService,
      MoveLineCreateService moveLineCreateService,
      MoveValidateService moveValidateService,
      ReconcileService reconcileService,
      BankOrderService bankOrderService,
      MoveCancelService moveCancelService,
      BankOrderRepository bankOrderRepository,
      ExpenseRepository expenseRepository) {
    this.bankOrderCreateServiceHr = bankOrderCreateServiceHr;
    this.accountConfigService = accountConfigService;
    this.paymentModeService = paymentModeService;
    this.moveCreateService = moveCreateService;
    this.moveLineCreateService = moveLineCreateService;
    this.moveValidateService = moveValidateService;
    this.reconcileService = reconcileService;
    this.bankOrderService = bankOrderService;
    this.moveCancelService = moveCancelService;
    this.bankOrderRepository = bankOrderRepository;
    this.expenseRepository = expenseRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void addPayment(Expense expense, BankDetails bankDetails) throws AxelorException {

    PaymentMode paymentMode = expense.getPaymentMode();

    if (paymentMode == null) {
      paymentMode = expense.getEmployee().getContactPartner().getOutPaymentMode();

      if (paymentMode == null) {
        throw new AxelorException(
            expense,
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(HumanResourceExceptionMessage.EXPENSE_MISSING_PAYMENT_MODE));
      }
      expense.setPaymentMode(paymentMode);
    }

    if (paymentMode.getGenerateBankOrder()) {
      BankOrder bankOrder = bankOrderCreateServiceHr.createBankOrder(expense, bankDetails);
      expense.setBankOrder(bankOrder);
      bankOrderRepository.save(bankOrder);
      expense.setPaymentStatusSelect(InvoicePaymentRepository.STATUS_PENDING);
    } else {
      if (accountConfigService
          .getAccountConfig(expense.getCompany())
          .getGenerateMoveForInvoicePayment()) {
        this.createMoveForExpensePayment(expense);
      }
      if (paymentMode.getAutomaticTransmission()) {
        expense.setPaymentStatusSelect(InvoicePaymentRepository.STATUS_PENDING);
      } else {
        expense.setPaymentStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);
        expense.setStatusSelect(ExpenseRepository.STATUS_REIMBURSED);
      }
    }
    expense.setPaymentAmount(
        expense
            .getInTaxTotal()
            .subtract(expense.getAdvanceAmount())
            .subtract(expense.getWithdrawnCash())
            .subtract(expense.getPersonalExpenseAmount()));
  }

  public Move createMoveForExpensePayment(Expense expense) throws AxelorException {
    Company company = expense.getCompany();
    PaymentMode paymentMode = expense.getPaymentMode();
    Partner partner = expense.getEmployee().getContactPartner();
    LocalDate paymentDate = expense.getPaymentDate();
    BigDecimal paymentAmount = expense.getInTaxTotal();
    BankDetails bankDetails = expense.getBankDetails();
    String origin = expense.getExpenseSeq();

    Account employeeAccount;

    Journal journal = paymentModeService.getPaymentModeJournal(paymentMode, company, bankDetails);

    MoveLine expenseMoveLine = this.getExpenseEmployeeMoveLineByLoop(expense);

    if (expenseMoveLine == null) {
      return null;
    }
    employeeAccount = expenseMoveLine.getAccount();

    Move move =
        moveCreateService.createMove(
            journal,
            company,
            expense.getMove().getCurrency(),
            partner,
            paymentDate,
            paymentDate,
            paymentMode,
            partner != null ? partner.getFiscalPosition() : null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT,
            origin,
            null,
            bankDetails);

    move.addMoveLineListItem(
        moveLineCreateService.createMoveLine(
            move,
            partner,
            paymentModeService.getPaymentModeAccount(paymentMode, company, bankDetails),
            paymentAmount,
            false,
            paymentDate,
            null,
            1,
            origin,
            null));

    MoveLine employeeMoveLine =
        moveLineCreateService.createMoveLine(
            move,
            partner,
            employeeAccount,
            paymentAmount,
            true,
            paymentDate,
            null,
            2,
            origin,
            null);
    employeeMoveLine.setTaxAmount(expense.getTaxTotal());

    move.addMoveLineListItem(employeeMoveLine);

    moveValidateService.accounting(move);
    expense.setPaymentMove(move);

    reconcileService.reconcile(expenseMoveLine, employeeMoveLine, true, false);

    expenseRepository.save(expense);

    return move;
  }

  protected MoveLine getExpenseEmployeeMoveLineByLoop(Expense expense) {
    MoveLine expenseEmployeeMoveLine = null;
    for (MoveLine moveline : expense.getMove().getMoveLineList()) {
      if (moveline.getCredit().compareTo(BigDecimal.ZERO) > 0) {
        expenseEmployeeMoveLine = moveline;
      }
    }
    return expenseEmployeeMoveLine;
  }

  @Override
  public void addPayment(Expense expense) throws AxelorException {
    BankDetails bankDetails = expense.getBankDetails();
    if (ObjectUtils.isEmpty(bankDetails)) {
      bankDetails = expense.getCompany().getDefaultBankDetails();
    }

    if (ObjectUtils.isEmpty(bankDetails)) {
      throw new AxelorException(
          expense,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_NO_COMPANY_BANK_DETAILS));
    }
    addPayment(expense, bankDetails);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void cancelPayment(Expense expense) throws AxelorException {
    BankOrder bankOrder = expense.getBankOrder();

    if (bankOrder != null) {
      if (bankOrder.getStatusSelect() == BankOrderRepository.STATUS_CARRIED_OUT
          || bankOrder.getStatusSelect() == BankOrderRepository.STATUS_REJECTED) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(HumanResourceExceptionMessage.EXPENSE_PAYMENT_CANCEL));
      } else if (bankOrder.getStatusSelect() != BankOrderRepository.STATUS_CANCELED) {
        bankOrderService.cancelBankOrder(bankOrder);
      }
    }

    Move paymentMove = expense.getPaymentMove();
    if (paymentMove != null) {
      if (paymentMove.getStatusSelect() == MoveRepository.STATUS_NEW) {
        expense.setPaymentMove(null);
      }
      moveCancelService.cancel(paymentMove);
    }
    resetExpensePaymentAfterCancellation(expense);
  }

  @Override
  @Transactional
  public void resetExpensePaymentAfterCancellation(Expense expense) {
    expense.setPaymentStatusSelect(InvoicePaymentRepository.STATUS_CANCELED);
    expense.setStatusSelect(ExpenseRepository.STATUS_VALIDATED);
    expense.setPaymentDate(null);
    expense.setPaymentAmount(BigDecimal.ZERO);
  }
}
