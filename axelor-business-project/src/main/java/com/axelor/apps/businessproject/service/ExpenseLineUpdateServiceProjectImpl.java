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
package com.axelor.apps.businessproject.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.employee.EmployeeFetchService;
import com.axelor.apps.hr.service.expense.ExpenseComputationService;
import com.axelor.apps.hr.service.expense.ExpenseLineToolService;
import com.axelor.apps.hr.service.expense.ExpenseLineUpdateServiceImpl;
import com.axelor.apps.hr.service.expense.ExpenseToolService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.List;

public class ExpenseLineUpdateServiceProjectImpl extends ExpenseLineUpdateServiceImpl {
  protected AppBusinessProjectService appBusinessProjectService;

  @Inject
  public ExpenseLineUpdateServiceProjectImpl(
      ExpenseToolService expenseToolService,
      ExpenseComputationService expenseComputationService,
      ExpenseLineToolService expenseLineToolService,
      ExpenseLineRepository expenseLineRepository,
      ExpenseRepository expenseRepository,
      AppBusinessProjectService appBusinessProjectService,
      EmployeeFetchService employeeFetchService) {
    super(
        expenseToolService,
        expenseComputationService,
        expenseLineToolService,
        expenseLineRepository,
        expenseRepository,
        employeeFetchService);
    this.appBusinessProjectService = appBusinessProjectService;
  }

  protected void updateBasicExpenseLine(
      ExpenseLine expenseLine,
      Project project,
      Employee employee,
      LocalDate expenseDate,
      String comments,
      Currency currency,
      Product expenseProduct,
      Boolean toInvoice,
      Expense newExpense,
      ProjectTask projectTask,
      List<Employee> employeeList)
      throws AxelorException {
    super.updateBasicExpenseLine(
        expenseLine,
        project,
        employee,
        expenseDate,
        comments,
        currency,
        expenseProduct,
        toInvoice,
        newExpense,
        projectTask,
        employeeList);
    if (appBusinessProjectService.isApp("business-project")) {
      expenseLine.setToInvoice(getToInvoice(expenseLine, project, toInvoice));
    }
  }

  protected boolean getToInvoice(ExpenseLine expenseLine, Project newProject, Boolean toInvoice)
      throws AxelorException {
    if (toInvoice != null) {
      if (newProject != null) {
        checkProjectCoherence(toInvoice, newProject);
      }
      Project oldProject = expenseLine.getProject();
      if (oldProject != null) {
        checkProjectCoherence(toInvoice, oldProject);
      }
      return toInvoice;
    }

    if (newProject != null) {
      return newProject.getIsInvoicingExpenses();
    }

    Project oldProject = expenseLine.getProject();
    if (oldProject != null) {
      oldProject.getIsInvoicingExpenses();
    }

    return false;
  }

  protected void checkProjectCoherence(Boolean toInvoice, Project project) throws AxelorException {
    if (Boolean.TRUE.equals(toInvoice) && !project.getIsInvoicingExpenses()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_UPDATE_BILLING_INCOMPATIBLE_PROJECT));
    }
  }
}
