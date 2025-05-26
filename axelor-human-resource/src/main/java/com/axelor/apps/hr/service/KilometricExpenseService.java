package com.axelor.apps.hr.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import java.math.BigDecimal;

public interface KilometricExpenseService {

  BigDecimal computeKilometricExpense(ExpenseLine expenseLine, Employee employee)
      throws AxelorException;

  void updateExpenseLineKilometricLog(Expense expense) throws AxelorException;
}
