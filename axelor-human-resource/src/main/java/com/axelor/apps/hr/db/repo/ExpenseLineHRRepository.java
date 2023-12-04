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
package com.axelor.apps.hr.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.service.KilometricService;
import com.axelor.inject.Beans;
import java.math.BigDecimal;

public class ExpenseLineHRRepository extends ExpenseLineRepository {

  @Override
  public ExpenseLine save(ExpenseLine expenseLine) {
    if (expenseLine.getKilometricAllowParam() != null
        && expenseLine.getDistance().compareTo(BigDecimal.ZERO) != 0
        && expenseLine.getExpenseDate() != null
        && expenseLine.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
      Long empId;
      Expense expense = expenseLine.getExpense();

      if (expense != null && expense.getEmployee() != null) {
        empId = expense.getEmployee().getId();
      } else {
        empId = expenseLine.getEmployee().getId();
      }
      Employee employee = Beans.get(EmployeeRepository.class).find(empId);
      try {
        expenseLine.setTotalAmount(
            Beans.get(KilometricService.class).computeKilometricExpense(expenseLine, employee));
      } catch (AxelorException e) {
        TraceBackService.trace(e);
      }
    }
    return super.save(expenseLine);
  }
}
