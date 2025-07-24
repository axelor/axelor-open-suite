package com.axelor.apps.hr.service.expense.expenseline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;

public interface ExpenseLineComputeService {
  void computeUntaxedAndCompanyAmounts(ExpenseLine expenseLine, Expense expense)
      throws AxelorException;

  void setCompanyAmounts(ExpenseLine expenseLine, Expense expense) throws AxelorException;
}
