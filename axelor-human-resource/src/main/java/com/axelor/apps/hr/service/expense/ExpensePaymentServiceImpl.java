/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveCancelService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.account.service.reconcile.ReconcileService;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.db.repo.BankOrderLineOriginRepository;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderCancelService;
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
import java.util.Objects;

@Singleton
public class ExpensePaymentServiceImpl implements ExpensePaymentService {

  protected BankOrderCreateServiceHr bankOrderCreateServiceHr;
  protected AccountConfigService accountConfigService;
  protected PaymentModeService paymentModeService;
  protected MoveCreateService moveCreateService;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveValidateService moveValidateService;
  protected ReconcileService reconcileService;
  protected BankOrderCancelService bankOrderCancelService;
  protected MoveCancelService moveCancelService;
  protected BankOrderRepository bankOrderRepository;
  protected ExpenseRepository expenseRepository;
  protected ExpenseFetchMoveService expenseFetchMoveService;

  @Inject
  public ExpensePaymentServiceImpl(
      BankOrderCreateServiceHr bankOrderCreateServiceHr,
      AccountConfigService accountConfigService,
      PaymentModeService paymentModeService,
      MoveCreateService moveCreateService,
      MoveLineCreateService moveLineCreateService,
      MoveValidateService moveValidateService,
      ReconcileService reconcileService,
      BankOrderCancelService bankOrderCancelService,
      MoveCancelService moveCancelService,
      BankOrderRepository bankOrderRepository,
      ExpenseRepository expenseRepository,
      ExpenseFetchMoveService expenseFetchMoveService) {
    this.bankOrderCreateServiceHr = bankOrderCreateServiceHr;
    this.accountConfigService = accountConfigService;
    this.paymentModeService = paymentModeService;
    this.moveCreateService = moveCreateService;
    this.moveLineCreateService = moveLineCreateService;
    this.moveValidateService = moveValidateService;
    this.reconcileService = reconcileService;
    this.bankOrderCancelService = bankOrderCancelService;
    this.moveCancelService = moveCancelService;
    this.bankOrderRepository = bankOrderRepository;
    this.expenseRepository = expenseRepository;
    this.expenseFetchMoveService = expenseFetchMoveService;
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
      expense.setPaymentStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);
      expense.setStatusSelect(ExpenseRepository.STATUS_REIMBURSED);
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
    Move move = expenseFetchMoveService.getExpenseMove(expense);

    Account employeeAccount;

    Journal journal = paymentModeService.getPaymentModeJournal(paymentMode, company, bankDetails);

    MoveLine expenseMoveLine = this.getExpenseEmployeeMoveLineByLoop(expense);

    if (expenseMoveLine == null) {
      return null;
    }
    employeeAccount = expenseMoveLine.getAccount();

    Move movePayment =
        moveCreateService.createMove(
            journal,
            company,
            move.getCurrency(),
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

    movePayment.addMoveLineListItem(
        moveLineCreateService.createMoveLine(
            movePayment,
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
            movePayment,
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

    movePayment.addMoveLineListItem(employeeMoveLine);

    moveValidateService.accounting(movePayment);
    expense.setPaymentMove(movePayment);
    movePayment.setExpensePayment(expense);

    reconcileService.reconcile(expenseMoveLine, employeeMoveLine, true, false);

    expenseRepository.save(expense);

    return movePayment;
  }

  protected MoveLine getExpenseEmployeeMoveLineByLoop(Expense expense) {
    MoveLine expenseEmployeeMoveLine = null;
    Move move = expenseFetchMoveService.getExpenseMove(expense);
    if (move == null) {
      return null;
    }
    for (MoveLine moveline : move.getMoveLineList()) {
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
    resetExpensePaymentAfterCancellation(expense);

    BankOrder bankOrder = expense.getBankOrder();
    if (bankOrder != null) {
      if (bankOrder.getStatusSelect() == BankOrderRepository.STATUS_CARRIED_OUT) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(HumanResourceExceptionMessage.EXPENSE_PAYMENT_CANCEL));
      } else if (bankOrder.getStatusSelect() != BankOrderRepository.STATUS_CANCELED) {
        cancelBankOrderPayment(bankOrder, expense);
      }
    }

    Move paymentMove = expense.getPaymentMove();
    if (paymentMove != null) {
      if (paymentMove.getStatusSelect() == MoveRepository.STATUS_NEW) {
        expense.setPaymentMove(null);
      }
      moveCancelService.cancel(paymentMove);
    }
  }

  @Override
  @Transactional
  public void resetExpensePaymentAfterCancellation(Expense expense) {
    expense.setPaymentStatusSelect(InvoicePaymentRepository.STATUS_CANCELED);
    expense.setStatusSelect(ExpenseRepository.STATUS_VALIDATED);
    expense.setPaymentDate(null);
    expense.setPaymentAmount(BigDecimal.ZERO);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void cancelBankOrderPayment(BankOrder bankOrder, Expense expense)
      throws AxelorException {
    if (ObjectUtils.isEmpty(bankOrder.getBankOrderLineList())
        || bankOrder.getBankOrderLineList().size() == 1
        || bankOrder.getAreMovesGenerated()
        || bankOrder.getStatusSelect() != BankOrderRepository.STATUS_DRAFT) {
      bankOrderCancelService.cancelBankOrder(bankOrder);
      return;
    }

    BankOrderLine bankOrderLine = findBankOrderLineWithThisOrigin(bankOrder, expense);
    if (bankOrderLine == null) {
      bankOrderCancelService.cancelBankOrder(bankOrder);
      return;
    }

    expense.setBankOrder(null);
    bankOrder.removeBankOrderLineListItem(bankOrderLine);
    bankOrderRepository.save(bankOrder);
  }

  protected BankOrderLine findBankOrderLineWithThisOrigin(BankOrder bankOrder, Expense expense) {
    if (bankOrder == null
        || ObjectUtils.isEmpty(bankOrder.getBankOrderLineList())
        || expense == null) {
      return null;
    }

    for (BankOrderLine bankOrderLine : bankOrder.getBankOrderLineList()) {
      if (ObjectUtils.isEmpty(bankOrderLine.getBankOrderLineOriginList())) {
        continue;
      }

      if (bankOrderLine.getBankOrderLineOriginList().stream()
          .anyMatch(
              origin ->
                  BankOrderLineOriginRepository.RELATED_TO_EXPENSE.equals(
                          origin.getRelatedToSelect())
                      && Objects.equals(expense.getId(), origin.getRelatedToSelectId()))) {
        return bankOrderLine;
      }
    }

    return null;
  }
}
