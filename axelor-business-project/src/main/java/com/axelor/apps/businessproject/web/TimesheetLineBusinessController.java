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
package com.axelor.apps.businessproject.web;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.businessproject.db.TaskMemberReport;
import com.axelor.apps.businessproject.db.TaskReport;
import com.axelor.apps.businessproject.db.repo.TaskReportRepository;
import com.axelor.apps.businessproject.service.TimesheetLineBusinessService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimesheetLineBusinessController {
  private static final Logger log = LoggerFactory.getLogger(TimesheetLineBusinessController.class);

  @Inject protected TaskReportRepository taskReportRepository;

  @Inject protected EmployeeRepository employeeRepository;

  public void setDefaultToInvoice(ActionRequest request, ActionResponse response) {
    try {
      TimesheetLine timesheetLine = request.getContext().asType(TimesheetLine.class);
      timesheetLine =
          Beans.get(TimesheetLineBusinessService.class).getDefaultToInvoice(timesheetLine);
      response.setValue("toInvoice", timesheetLine.getToInvoice());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /** Prefill for full form */
  public void prefillFromTaskMemberReport(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      if (context == null) return;

      TimesheetLine timesheetLine = context.asType(TimesheetLine.class);
      if (timesheetLine == null) return;

      ProjectTask projectTask = timesheetLine.getProjectTask();
      Project project =
          (projectTask != null) ? projectTask.getProject() : timesheetLine.getProject();
      if (project == null) return;

      Employee employee = resolveEmployee(timesheetLine, context);
      if (employee == null) return;

      TaskMemberReport report = findLatestMatchingTaskMemberReport(project, employee, projectTask);
      if (report != null) {
        prefillResponseFromReport(response, report, projectTask.getProduct());
        log.debug("Prefilled from TaskMemberReport (full form)");
      }

    } catch (Exception e) {
      log.error("Error in prefillFromTaskMemberReport", e);
      TraceBackService.trace(response, e);
    }
  }

  /** Prefill for grid/modal context */
  public void prefillFromTaskMemberReportGrid(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      if (context == null) return;

      ProjectTask projectTask = getProjectTaskFromContext(context);
      if (projectTask == null || projectTask.getProject() == null) return;
      Project project = projectTask.getProject();

      Employee employee = getEmployeeFromContext(context);
      if (employee == null) return;

      TaskMemberReport report = findLatestMatchingTaskMemberReport(project, employee, projectTask);
      if (report != null) {
        prefillResponseFromReport(response, report, projectTask.getProduct());
        log.debug(
            "Prefilled from TaskMemberReport (modal/dialog) for employee {} on project {}",
            employee.getName(),
            project.getName());
      }

    } catch (Exception e) {
      log.error("Error in grid prefill", e);
      TraceBackService.trace(response, e);
    }
  }

  /** Resolve employee from TimesheetLine → context → parent → current user */
  private Employee resolveEmployee(TimesheetLine timesheetLine, Context context) {
    if (timesheetLine.getEmployee() != null) return timesheetLine.getEmployee();

    Object employeeObj = context.get("employee");
    Employee employee = extractEmployeeFromObject(employeeObj);

    if (employee == null && context.getParent() != null) {
      Object parentEmp = context.getParent().get("employee");
      employee = extractEmployeeFromObject(parentEmp);
    }

    if (employee == null) {
      User currentUser = AuthUtils.getUser();
      employee = (currentUser != null) ? currentUser.getEmployee() : null;
    }

    return employee;
  }

  private Employee extractEmployeeFromObject(Object obj) {
    if (obj instanceof Employee) return (Employee) obj;
    if (obj instanceof Map) {
      Object empId = ((Map<?, ?>) obj).get("id");
      if (empId != null) {
        Long id = (empId instanceof Integer) ? ((Integer) empId).longValue() : (Long) empId;
        return employeeRepository.find(id);
      }
    }
    return null;
  }

  /** Extract project task from context */
  private ProjectTask getProjectTaskFromContext(Context context) {
    Object obj = context.get("projectTask");
    if (obj instanceof ProjectTask) return (ProjectTask) obj;
    return null;
  }

  /** Extract employee from context */
  private Employee getEmployeeFromContext(Context context) {
    Object obj = context.get("employee");
    Employee employee = extractEmployeeFromObject(obj);
    if (employee != null) return employee;

    if (context.getParent() != null) {
      Object parentEmp = context.getParent().get("employee");
      employee = extractEmployeeFromObject(parentEmp);
    }

    if (employee == null) {
      User currentUser = AuthUtils.getUser();
      employee = (currentUser != null) ? currentUser.getEmployee() : null;
    }
    return employee;
  }

  /** Finds latest matching TaskMemberReport */
  private TaskMemberReport findLatestMatchingTaskMemberReport(
      Project project, Employee employee, ProjectTask projectTask) {
    if (project == null) return null;

    Long employeeId = (employee != null) ? employee.getId() : null;
    Long taskId = (projectTask != null) ? projectTask.getId() : null;

    List<TaskReport> taskReports =
        taskReportRepository
            .all()
            .filter("self.project.id = :projectId")
            .bind("projectId", project.getId())
            .order("-taskDate")
            .fetch();

    TaskMemberReport fallbackReport = null;

    for (TaskReport taskReport : taskReports) {
      if (taskReport.getTaskMemberReports() == null) continue;
      for (TaskMemberReport tmr : taskReport.getTaskMemberReports()) {
        if (tmr.getEmployee() == null) continue;
        boolean taskMatches =
            (taskId == null) || (tmr.getTask() != null && tmr.getTask().getId().equals(taskId));
        if (!taskMatches) continue;
        if (tmr.getEmployee().getId().equals(employeeId)) return tmr;
        if (fallbackReport == null) fallbackReport = tmr;
      }
    }

    return fallbackReport;
  }

  /** Prefill response from report (works for modal & full form) */
  private void prefillResponseFromReport(
      ActionResponse response, TaskMemberReport report, Product activity) {
    Map<String, Object> values = new HashMap<>();

    if (report.getStartTime() != null) {
      values.put("startTime", report.getStartTime());
      values.put("date", report.getStartTime().toLocalDate());
    }

    if (report.getEndTime() != null) values.put("endTime", report.getEndTime());

    if (report.getWorkHours() != null) {
      BigDecimal duration = report.getWorkHours();
      values.put("duration", duration);
      values.put("durationForCustomer", duration);
    }

    if (report.getBreakTimeHours() != null) values.put("breakTime", report.getBreakTimeHours());
    if (report.getBreakTimeMinutes() != null)
      values.put("breakTimeMinutes", report.getBreakTimeMinutes());

    if (activity != null) {
      values.put("product", activity);
    }

    response.setValues(values);
  }
}
