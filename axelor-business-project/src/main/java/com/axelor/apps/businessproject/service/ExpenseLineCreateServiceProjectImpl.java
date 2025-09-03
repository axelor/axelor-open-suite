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
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.KilometricService;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.hr.service.employee.EmployeeFetchService;
import com.axelor.apps.hr.service.expense.ExpenseLineCreateServiceImpl;
import com.axelor.apps.hr.service.expense.ExpenseLineToolService;
import com.axelor.apps.hr.service.expense.ExpenseProofFileService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;

public class ExpenseLineCreateServiceProjectImpl extends ExpenseLineCreateServiceImpl {
  protected AppBusinessProjectService appBusinessProjectService;

  @Inject
  public ExpenseLineCreateServiceProjectImpl(
      ExpenseLineRepository expenseLineRepository,
      AppHumanResourceService appHumanResourceService,
      KilometricService kilometricService,
      HRConfigService hrConfigService,
      AppBaseService appBaseService,
      ExpenseProofFileService expenseProofFileService,
      ExpenseLineToolService expenseLineToolService,
      AppBusinessProjectService appBusinessProjectService,
      EmployeeFetchService employeeFetchService) {
    super(
        expenseLineRepository,
        appHumanResourceService,
        kilometricService,
        hrConfigService,
        appBaseService,
        expenseProofFileService,
        expenseLineToolService,
        employeeFetchService);
    this.appBusinessProjectService = appBusinessProjectService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  protected ExpenseLine createBasicExpenseLine(
      Project project,
      Employee employee,
      LocalDate expenseDate,
      String comments,
      Currency currency,
      Boolean toInvoice,
      ProjectTask projectTask)
      throws AxelorException {
    ExpenseLine expenseLine =
        super.createBasicExpenseLine(
            project, employee, expenseDate, comments, currency, toInvoice, projectTask);

    if (appBusinessProjectService.isApp("business-project")) {
      expenseLine.setToInvoice(getToInvoice(project, toInvoice));
    }
    return expenseLine;
  }

  protected boolean getToInvoice(Project project, Boolean toInvoice) throws AxelorException {
    if (toInvoice == null && project == null) {
      return false;
    }
    if (toInvoice != null && project == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_NO_PROJECT));
    }
    return toInvoice == null ? project.getIsInvoicingExpenses() : toInvoice;
  }
}
