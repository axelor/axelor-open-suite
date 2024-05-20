/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.db.repo;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.service.KilometricService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import javax.persistence.PersistenceException;

public class ExpenseLineHRRepository extends ExpenseLineRepository {

  protected EmployeeRepository employeeRepository;
  protected KilometricService kilometricService;

  @Inject
  public ExpenseLineHRRepository(
      KilometricService kilometricService, EmployeeRepository employeeRepository) {
    this.kilometricService = kilometricService;
    this.employeeRepository = employeeRepository;
  }

  @Override
  public ExpenseLine save(ExpenseLine expenseLine) {
    try {
      if (expenseLine.getKilometricAllowParam() != null
          && expenseLine.getDistance().compareTo(BigDecimal.ZERO) != 0
          && expenseLine.getExpenseDate() != null) {
        Employee employee = null;
        Expense expense = expenseLine.getExpense();

        if (expense != null && expense.getEmployee() != null) {
          employee = expense.getEmployee();
        } else if (expenseLine.getEmployee() != null) {
          employee = expenseLine.getEmployee();
        }
        if (employee != null) {
          employee = employeeRepository.find(employee.getId());
          expenseLine.setTotalAmount(
              kilometricService.computeKilometricExpense(expenseLine, employee));
        } else {
          expenseLine.setTotalAmount(BigDecimal.ZERO);
        }
      }
      return super.save(expenseLine);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }
}
