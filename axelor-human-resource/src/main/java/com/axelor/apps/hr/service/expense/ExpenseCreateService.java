package com.axelor.apps.hr.service.expense;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import java.util.List;

public interface ExpenseCreateService {
  Expense createExpense(
      Company company,
      Employee employee,
      Currency currency,
      BankDetails bankDetails,
      Period period,
      Integer companyCbSelect,
      List<ExpenseLine> expenseLineList)
      throws AxelorException;
}
