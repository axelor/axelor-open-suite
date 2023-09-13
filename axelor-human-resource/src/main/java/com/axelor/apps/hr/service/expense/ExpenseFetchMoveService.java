package com.axelor.apps.hr.service.expense;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.hr.db.Expense;

public interface ExpenseFetchMoveService {
  Move getExpenseMove(Expense expense);

  Move getExpensePaymentMove(Expense expense);
}
