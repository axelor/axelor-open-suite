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
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.service.ProjectService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.List;
import java.util.Map;

public interface ProjectBusinessService extends ProjectService {

  SaleOrder generateQuotation(Project project) throws AxelorException;

  Project generateProject(SaleOrder saleOrder) throws AxelorException;

  Project generatePhaseProject(SaleOrderLine saleOrderLine, Project parent) throws AxelorException;

  void computeProjectTotals(Project project) throws AxelorException;

  void backupToProjectHistory(Project project);

  Map<String, Object> processRequestToDisplayTimeReporting(Long id) throws AxelorException;

  Map<String, Object> processRequestToDisplayFinancialReporting(Long id) throws AxelorException;

  void transitionBetweenPaidStatus(Project project) throws AxelorException;

  List<String> checkPercentagesOver1000OnTasks(Project project);

  Project findProjectFromModel(String modelClassName, Long modelId);

  /**
   * Determines if a project is ready to invoice. A project is ready to invoice if it has been
   * confirmed for invoicing and all present items are fully validated. If the project has expenses,
   * they must all be validated. If the project has tasks, all tasks must have timesheet lines and
   * all timesheet lines must be validated. A project with neither expenses nor tasks only needs to
   * be confirmed.
   *
   * @param project the project to check
   * @return true if the project is ready to invoice, false otherwise
   */
  boolean readyToInvoice(Project project);

  boolean allTimesheetLinesValidated(Project project);

  boolean allExpensesValidated(Project project);

  boolean allTasksHaveTimesheetLines(Project project);

  void syncTaskReportToProject(Project project);

  long getProjectExpenseCount(Project project);

  /**
   * Determines if a project is ready for review. A project is ready for review if it has at least
   * one approval item (timesheet lines or expenses) and all present items are fully validated.
   *
   * @param project the project to check
   * @return true if the project is ready for review, false otherwise
   */
  Boolean isProjectReadyForReview(Project project);

  Partner getSubcontractor(Project project) throws AxelorException;

  Boolean allExpensesSent(Project project);

  Boolean hasExtraExpenses(Project project);

  boolean hasExpense(Project project);

  /**
   * Checks if all tasks for a project have already been reported. If a project does not have any
   * task, it considers that its task are yet to be reported.
   *
   * @param project project
   * @return false if all task for project have not been reported yet or if project has no task else
   *     returns true
   */
  boolean allTaskReported(Project project);

  boolean hasTask(Project project);

  boolean allExpensesSentOrValidated(Project project);

  /**
   * Ensures consistency for single-user projects. If not a single-user project, does nothing. If
   * single-user project, assignedTo must exist and membersUserSet must contain only that user.
   *
   * @param project
   * @throws AxelorException
   */
  void ensureSingleUserProjectConsistency(Project project) throws AxelorException;
}
