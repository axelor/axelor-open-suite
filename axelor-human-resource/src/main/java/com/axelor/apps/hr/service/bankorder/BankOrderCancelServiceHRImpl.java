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
package com.axelor.apps.hr.service.bankorder;

import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.service.payment.paymentsession.PaymentSessionCancelService;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderCancelServiceImpl;
import com.axelor.apps.bankpayment.service.invoice.payment.InvoicePaymentBankPaymentCancelService;
import com.axelor.apps.bankpayment.service.move.MoveCancelBankPaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.service.expense.ExpensePaymentService;
import com.google.inject.Inject;
import java.util.List;

public class BankOrderCancelServiceHRImpl extends BankOrderCancelServiceImpl {

  protected ExpenseRepository expenseRepository;
  protected ExpensePaymentService expensePaymentService;
  protected AppBaseService appBaseService;

  @Inject
  public BankOrderCancelServiceHRImpl(
      InvoicePaymentRepository invoicePaymentRepository,
      BankOrderRepository bankOrderRepository,
      PaymentSessionRepository paymentSessionRepository,
      MoveRepository moveRepository,
      ExpenseRepository expenseRepository,
      InvoicePaymentBankPaymentCancelService invoicePaymentBankPaymentCancelService,
      MoveCancelBankPaymentService moveCancelBankPaymentService,
      PaymentSessionCancelService paymentSessionCancelService,
      ExpensePaymentService expensePaymentService,
      AppBaseService appBaseService) {
    super(
        invoicePaymentRepository,
        bankOrderRepository,
        paymentSessionRepository,
        moveRepository,
        invoicePaymentBankPaymentCancelService,
        moveCancelBankPaymentService,
        paymentSessionCancelService);
    this.expenseRepository = expenseRepository;
    this.expensePaymentService = expensePaymentService;
    this.appBaseService = appBaseService;
  }

  @Override
  public BankOrder cancelPayment(BankOrder bankOrder) throws AxelorException {
    bankOrder = super.cancelPayment(bankOrder);

    if (!appBaseService.isApp("employee")) {
      return bankOrder;
    }
    List<Expense> expenseList =
        expenseRepository.all().filter("self.bankOrder.id = ?", bankOrder.getId()).fetch();
    for (Expense expense : expenseList) {
      if (expense != null
          && expense.getPaymentStatusSelect() != InvoicePaymentRepository.STATUS_CANCELED) {
        expensePaymentService.cancelPayment(expense);
      }
    }

    return bankOrder;
  }
}
