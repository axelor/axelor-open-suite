/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.service.expense;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import java.util.List;

public interface ExpenseToolService {

  /**
   * Get the expense from employee, if no expense is found create one.
   *
   * @param employee
   * @return
   */
  public Expense getOrCreateExpense(Employee employee);

  public void setDraftSequence(Expense expense) throws AxelorException;

  public Expense updateMoveDateAndPeriod(Expense expense);

  void addExpenseLinesToExpense(Expense expense, List<ExpenseLine> expenseLineList)
      throws AxelorException;

  void addExpenseLinesToExpenseAndCompute(Expense expense, List<ExpenseLine> expenseLineList)
      throws AxelorException;

  boolean isKilometricExpenseLine(ExpenseLine expenseLine);
}
