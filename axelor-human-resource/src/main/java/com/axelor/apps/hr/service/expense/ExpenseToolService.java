/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.apps.project.db.Project;
import java.util.List;

public interface ExpenseToolService {

  /**
   * Get the expense from employee, if no expense is found create one.
   *
   * @param employee
   * @return
   */
  public Expense getOrCreateExpense(Employee employee);

  public Expense getOrCreateExpense(Employee employee, Project project);

  public void setDraftSequence(Expense expense) throws AxelorException;

  public Expense updateMoveDateAndPeriod(Expense expense);

  void addExpenseLinesToExpense(Expense expense, List<ExpenseLine> expenseLineList)
      throws AxelorException;

  void addExpenseLineToExpense(Expense expense, ExpenseLine expenseLine) throws AxelorException;

  void addExpenseLinesToExpenseAndCompute(Expense expense, List<ExpenseLine> expenseLineList)
      throws AxelorException;

  void addExpenseLineToExpenseAndCompute(Expense expense, ExpenseLine expenseLine)
      throws AxelorException;

  boolean hasSeveralCurrencies(List<ExpenseLine> expenseLineList);

  boolean hasSeveralEmployees(List<ExpenseLine> expenseLineList);

  void addOrUpdateProjectExpenseLines(Expense expense, List<ExpenseLine> expenseLineList)
      throws AxelorException;

  void deleteExpenses(List<Integer> ids) throws AxelorException;

  /**
   * Validates the employee attached to an expense is valid based on the expense category. That is
   * If the expense is one which does not require an employee it validates this does not happen. If
   * the expense is one which does require an employee it validates the employee is present. It
   * validates the employee set on an expense is part of the project which the expense is related
   * to.
   *
   * @param expense Expense
   * @throws AxelorException
   */
  void validateExpenseEmployee(Expense expense) throws AxelorException;
}
