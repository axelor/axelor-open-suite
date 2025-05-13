package com.axelor.apps.hr.service.expense.expenseline;

import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;

public interface ExpenseLineDomainService {
  String getInvitedCollaborators(ExpenseLine expenseLine, Expense expense);
}
