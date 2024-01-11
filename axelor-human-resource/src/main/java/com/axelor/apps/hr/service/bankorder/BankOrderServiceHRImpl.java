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
package com.axelor.apps.hr.service.bankorder;

import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.service.payment.paymentsession.PaymentSessionCancelService;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.ebics.service.EbicsService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderLineOriginService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderLineService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderMoveService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderServiceImpl;
import com.axelor.apps.bankpayment.service.config.BankPaymentConfigService;
import com.axelor.apps.bankpayment.service.invoice.payment.InvoicePaymentBankPaymentCancelService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.service.expense.ExpenseService;
import com.axelor.inject.Beans;
import com.axelor.studio.app.service.AppService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class BankOrderServiceHRImpl extends BankOrderServiceImpl {

  protected ExpenseService expenseService;

  @Inject
  public BankOrderServiceHRImpl(
      BankOrderRepository bankOrderRepo,
      InvoicePaymentRepository invoicePaymentRepo,
      BankOrderLineService bankOrderLineService,
      EbicsService ebicsService,
      InvoicePaymentBankPaymentCancelService invoicePaymentBankPaymentCancelService,
      BankPaymentConfigService bankPaymentConfigService,
      SequenceService sequenceService,
      BankOrderLineOriginService bankOrderLineOriginService,
      BankOrderMoveService bankOrderMoveService,
      AppBaseService appBaseService,
      PaymentSessionCancelService paymentSessionCancelService,
      PaymentSessionRepository paymentSessionRepo,
      CurrencyService currencyService,
      ExpenseService expenseService) {
    super(
        bankOrderRepo,
        invoicePaymentRepo,
        bankOrderLineService,
        ebicsService,
        invoicePaymentBankPaymentCancelService,
        bankPaymentConfigService,
        sequenceService,
        bankOrderLineOriginService,
        bankOrderMoveService,
        appBaseService,
        paymentSessionCancelService,
        paymentSessionRepo,
        currencyService);
    this.expenseService = expenseService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validatePayment(BankOrder bankOrder) throws AxelorException {
    super.validatePayment(bankOrder);
    if (!Beans.get(AppService.class).isApp("employee")) {
      return;
    }
    List<Expense> expenseList =
        Beans.get(ExpenseRepository.class)
            .all()
            .filter("self.bankOrder.id = ?", bankOrder.getId())
            .fetch();
    for (Expense expense : expenseList) {
      if (expense != null && expense.getStatusSelect() != ExpenseRepository.STATUS_REIMBURSED) {
        expense.setStatusSelect(ExpenseRepository.STATUS_REIMBURSED);
        expense.setPaymentStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);
        expenseService.createMoveForExpensePayment(expense);
      }
    }
  }

  @Override
  public BankOrder cancelPayment(BankOrder bankOrder) throws AxelorException {
    bankOrder = super.cancelPayment(bankOrder);

    if (!Beans.get(AppService.class).isApp("employee")) {
      return bankOrder;
    }
    List<Expense> expenseList =
        Beans.get(ExpenseRepository.class)
            .all()
            .filter("self.bankOrder.id = ?", bankOrder.getId())
            .fetch();
    for (Expense expense : expenseList) {
      if (expense != null
          && expense.getPaymentStatusSelect() != InvoicePaymentRepository.STATUS_CANCELED) {
        expenseService.cancelPayment(expense);
      }
    }

    return bankOrder;
  }
}
