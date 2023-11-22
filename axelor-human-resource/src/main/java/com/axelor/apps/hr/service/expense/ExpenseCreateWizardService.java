package com.axelor.apps.hr.service.expense;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.Context;
import java.util.List;

public interface ExpenseCreateWizardService {
  ActionView.ActionViewBuilder getCreateExpenseWizard(Context context) throws AxelorException;

  Expense createExpense(List<ExpenseLine> expenseLineList) throws AxelorException;

  String getExpenseDomain(List<ExpenseLine> expenseLineList);
}
