package com.axelor.apps.hr.service.expense;

import com.axelor.apps.hr.db.Expense;

public interface ExpenseWorkflowService {
  void backToDraft(Expense expense);
}
