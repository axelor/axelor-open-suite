/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.service.bankorder;

import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.PaymentScheduleLineRepository;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderMergeServiceImpl;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderService;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.repo.ExpenseHRRepository;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class BankOrderMergeHRServiceImpl extends BankOrderMergeServiceImpl {

  protected ExpenseHRRepository expenseHRRepository;

  @Inject
  public BankOrderMergeHRServiceImpl(
      BankOrderRepository bankOrderRepo,
      InvoicePaymentRepository invoicePaymentRepo,
      BankOrderService bankOrderService,
      InvoiceRepository invoiceRepository,
      PaymentScheduleLineRepository paymentScheduleLineRepository,
      ExpenseHRRepository expenseHRRepository) {
    super(
        bankOrderRepo,
        invoicePaymentRepo,
        bankOrderService,
        invoiceRepository,
        paymentScheduleLineRepository);
    this.expenseHRRepository = expenseHRRepository;
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  @Override
  public BankOrder mergeBankOrders(Collection<BankOrder> bankOrders) throws AxelorException {
    List<Expense> expenseList =
        expenseHRRepository
            .all()
            .filter(
                "self.bankOrder.id IN (?)",
                bankOrders.stream().map(BankOrder::getId).collect(Collectors.toList()))
            .fetch();

    for (Expense expense : expenseList) {
      expense.setBankOrder(null);
      expenseHRRepository.save(expense);
    }

    BankOrder bankOrder = super.mergeBankOrders(bankOrders);

    for (Expense expense : expenseList) {
      expense.setBankOrder(bankOrder);
      expenseHRRepository.save(expense);
    }

    return bankOrder;
  }
}
