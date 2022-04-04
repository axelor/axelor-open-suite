package com.axelor.apps.hr.observer;

import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.event.BankOrderValidated;
import com.axelor.apps.base.service.app.AppService;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.service.expense.ExpenseService;
import com.axelor.event.Observes;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import java.util.List;

public class BankOrderHRObserver {

  protected ExpenseService expenseService;

  @Inject
  public BankOrderHRObserver(ExpenseService expenseService) {
    this.expenseService = expenseService;
  }

  @Transactional(rollbackOn = {Exception.class})
  void onValidatePayment(@Observes @Named("validatePayment") BankOrderValidated event)
      throws AxelorException {

    BankOrder bankOrder = event.getBankOrder();
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

  @Transactional(rollbackOn = {Exception.class})
  void onCancelPayment(@Observes @Named("cancelPayment") BankOrderValidated event)
      throws AxelorException {

    BankOrder bankOrder = event.getBankOrder();
    if (!Beans.get(AppService.class).isApp("employee")) {
      return;
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
  }
}
