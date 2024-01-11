package com.axelor.apps.hr.service.expense;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import java.util.List;

public interface ExpenseCreateWizardService {
  boolean checkExpenseLinesToMerge(List<Integer> idList) throws AxelorException;

  Expense createExpense(List<ExpenseLine> expenseLineList) throws AxelorException;

  String getExpenseDomain(List<ExpenseLine> expenseLineList);
}
