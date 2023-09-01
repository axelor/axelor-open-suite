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

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.extract.ExtractContextMoveService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCancelService;
import com.axelor.apps.bankpayment.db.repo.BankReconciliationLineRepository;
import com.axelor.apps.bankpayment.service.bankreconciliation.BankReconciliationService;
import com.axelor.apps.bankpayment.service.move.MoveReverseServiceBankPaymentImpl;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Expense;
import com.axelor.studio.app.service.AppService;
import com.google.inject.Inject;
import java.time.LocalDate;

public class ExpenseMoveReverseServiceImpl extends MoveReverseServiceBankPaymentImpl {

  protected ExpensePaymentService expensePaymentService;
  protected AppService appService;

  @Inject
  public ExpenseMoveReverseServiceImpl(
      MoveCreateService moveCreateService,
      ReconcileService reconcileService,
      MoveValidateService moveValidateService,
      MoveRepository moveRepository,
      MoveLineCreateService moveLineCreateService,
      ExtractContextMoveService extractContextMoveService,
      InvoicePaymentRepository invoicePaymentRepository,
      InvoicePaymentCancelService invoicePaymentCancelService,
      MoveToolService moveToolService,
      BankReconciliationService bankReconciliationService,
      BankReconciliationLineRepository bankReconciliationLineRepository,
      ExpensePaymentService expensePaymentService,
      AppService appService) {
    super(
        moveCreateService,
        reconcileService,
        moveValidateService,
        moveRepository,
        moveLineCreateService,
        extractContextMoveService,
        invoicePaymentRepository,
        invoicePaymentCancelService,
        moveToolService,
        bankReconciliationService,
        bankReconciliationLineRepository);
    this.expensePaymentService = expensePaymentService;
    this.appService = appService;
  }

  @Override
  public Move generateReverse(
      Move move,
      boolean isAutomaticReconcile,
      boolean isAutomaticAccounting,
      boolean isUnreconcileOriginalMove,
      LocalDate dateOfReversion)
      throws AxelorException {
    Move reverseMove =
        super.generateReverse(
            move,
            isAutomaticReconcile,
            isAutomaticAccounting,
            isUnreconcileOriginalMove,
            dateOfReversion);
    if (!appService.isApp("expense")) {
      return reverseMove;
    }

    cancelVentilation(move);
    cancelPayment(move);
    return reverseMove;
  }

  protected void cancelPayment(Move move) {
    Expense expensePayment = move.getExpensePayment();
    if (expensePayment != null) {
      expensePaymentService.resetExpensePaymentAfterCancellation(expensePayment);
    }
  }

  protected void cancelVentilation(Move move) {
    Expense expense = move.getExpense();
    if (expense != null) {
      expense.setVentilated(false);
    }
  }
}
