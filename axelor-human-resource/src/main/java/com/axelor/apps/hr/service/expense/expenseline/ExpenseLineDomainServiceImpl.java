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
package com.axelor.apps.hr.service.expense.expenseline;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.service.employee.EmployeeFetchService;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import java.util.List;

public class ExpenseLineDomainServiceImpl implements ExpenseLineDomainService {
  protected EmployeeFetchService employeeFetchService;

  @Inject
  public ExpenseLineDomainServiceImpl(EmployeeFetchService employeeFetchService) {
    this.employeeFetchService = employeeFetchService;
  }

  @Override
  public String getInvitedCollaborators(ExpenseLine expenseLine, Expense expense) {
    List<Employee> employeeList =
        employeeFetchService.getInvitedCollaborators(
            expenseLine.getExpenseDate(), expense != null ? expense.getEmployee() : null);
    return "self.id IN (" + StringHelper.getIdListString(employeeList) + ")";
  }
}
