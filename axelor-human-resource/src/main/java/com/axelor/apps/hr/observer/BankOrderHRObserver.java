package com.axelor.apps.hr.observer;

import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.event.BankOrderEvent;
import com.axelor.apps.base.service.app.AppService;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.service.expense.ExpenseService;
import com.axelor.event.Observes;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import javax.inject.Named;

public class BankOrderHRObserver {

  protected ExpenseService expenseService;

  @Inject
  public BankOrderHRObserver(ExpenseService expenseService) {
    this.expenseService = expenseService;
  }

  @Transactional(rollbackOn = Exception.class)
  void onValidatePayment(@Observes @Named(BankOrderEvent.VALIDATE_PAYMENT) BankOrderEvent event)
      throws AxelorException {

    BankOrder bankOrder = event.getBankOrder();
    if (!Beans.get(AppService.class).isApp("employee")) {
      return;
    }
    List<Expense> expenseList =
        Beans.get(ExpenseRepository.class)
            .all()
            .filter("self.bankOrder.id = :bankOrderId AND self.statusSelect != :statusReimbursed")
            .bind("bankOrderId", bankOrder.getId())
            .bind("statusReimbursed", ExpenseRepository.STATUS_REIMBURSED)
            .fetch();
    for (Expense expense : expenseList) {
      expense.setStatusSelect(ExpenseRepository.STATUS_REIMBURSED);
      expense.setPaymentStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);
      expenseService.createMoveForExpensePayment(expense);
    }
  }

  @Transactional(rollbackOn = Exception.class)
  void onCancelPayment(@Observes @Named(BankOrderEvent.CANCEL_PAYMENT) BankOrderEvent event)
      throws AxelorException {

    BankOrder bankOrder = event.getBankOrder();
    if (!Beans.get(AppService.class).isApp("employee")) {
      return;
    }
    List<Expense> expenseList =
        Beans.get(ExpenseRepository.class)
            .all()
            .filter(
                "self.bankOrder.id = :bankOrderId AND self.paymentStatusSelect != :paymentStatusSelect ")
            .bind("bankOrderId", bankOrder.getId())
            .bind("paymentStatusSelect", InvoicePaymentRepository.STATUS_CANCELED)
            .fetch();
    for (Expense expense : expenseList) {
      expenseService.cancelPayment(expense);
    }
  }
}
