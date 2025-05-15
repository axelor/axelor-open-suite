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
package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.user.UserHrService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TimesheetCreateServiceImpl implements TimesheetCreateService {

  protected UserHrService userHrService;
  protected ProjectRepository projectRepository;
  protected TimesheetLineService timesheetLineService;
  protected TimesheetRepository timesheetRepository;
  protected TimesheetLineCreateService timesheetLineCreateService;
  protected TimesheetQueryService timesheetQueryService;
  protected EmployeeRepository employeeRepository;

  @Inject
  public TimesheetCreateServiceImpl(
      UserHrService userHrService,
      ProjectRepository projectRepository,
      TimesheetLineService timesheetLineService,
      TimesheetRepository timesheetRepository,
      TimesheetLineCreateService timesheetLineCreateService,
      TimesheetQueryService timesheetQueryService,
      EmployeeRepository employeeRepository) {
    this.userHrService = userHrService;
    this.projectRepository = projectRepository;
    this.timesheetLineService = timesheetLineService;
    this.timesheetRepository = timesheetRepository;
    this.timesheetLineCreateService = timesheetLineCreateService;
    this.timesheetQueryService = timesheetQueryService;
    this.employeeRepository = employeeRepository;
  }

  @Transactional
  @Override
  public Timesheet createTimesheet(Employee employee, LocalDate fromDate, LocalDate toDate) {
    Timesheet timesheet = new Timesheet();

    Company company = timesheetQueryService.getDefaultCompany(employee);

    String timeLoggingPreferenceSelect =
        employee == null ? null : employee.getTimeLoggingPreferenceSelect();
    timesheet.setTimeLoggingPreferenceSelect(timeLoggingPreferenceSelect);
    timesheet.setCompany(company);
    timesheet.setFromDate(fromDate);
    timesheet.setToDate(toDate);
    timesheet.setStatusSelect(TimesheetRepository.STATUS_DRAFT);
    timesheet.setEmployee(employee);

    return timesheetRepository.save(timesheet);
  }

  @Override
  public Timesheet createTimesheet(LocalDate fromDate, LocalDate toDate) throws AxelorException {
    Employee employee = AuthUtils.getUser().getEmployee();
    if (employee == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(HumanResourceExceptionMessage.LEAVE_USER_EMPLOYEE));
    }
    if (fromDate != null && toDate != null && toDate.isBefore(fromDate)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.TIMESHEET_INVALID_DATES));
    }
    return createTimesheet(employee, fromDate, toDate);
  }

  @Override
  public List<Map<String, Object>> createDefaultLines(Timesheet timesheet) {

    List<Map<String, Object>> lines = new ArrayList<>();
    User user = timesheet.getEmployee().getUser();
    if (user == null || timesheet.getFromDate() == null) {
      return lines;
    }

    Product product = userHrService.getTimesheetProduct(timesheet.getEmployee(), null);

    if (product == null) {
      return lines;
    }

    List<Project> projects =
        projectRepository
            .all()
            .filter(
                "self.membersUserSet.id = ?1 "
                    + "and self.projectStatus.isCompleted = false "
                    + "and self.manageTimeSpent = true",
                user.getId())
            .fetch();

    for (Project project : projects) {
      TimesheetLine line =
          timesheetLineCreateService.createTimesheetLine(
              project,
              product,
              timesheet.getEmployee(),
              timesheet.getFromDate(),
              timesheet,
              new BigDecimal(0),
              null);
      lines.add(Mapper.toMap(line));
    }

    return lines;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Timesheet getOrCreateTimesheet(TimesheetLine timesheetLine) {
    Employee employee = timesheetLine.getEmployee();
    if (employee == null) {
      return null;
    }
    return getOrCreateTimesheet(employee, timesheetLine.getProject(), timesheetLine.getDate());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Timesheet getOrCreateTimesheet(Employee employee, Project project, LocalDate date) {
    if (employee != null) {
      employee = employeeRepository.find(employee.getId());
    }
    Company company = null;
    if (project != null) {
      company = project.getCompany();
    }
    if (company == null) {
      company = timesheetQueryService.getDefaultCompany(employee);
    }
    Timesheet timesheet =
        timesheetQueryService.getTimesheetQuery(employee, company, date).order("id").fetchOne();
    if (timesheet == null) {
      Timesheet lastTimesheet =
          timesheetRepository
              .all()
              .filter(
                  "self.employee = ?1 AND self.statusSelect != ?2 AND self.toDate is not null",
                  employee,
                  TimesheetRepository.STATUS_CANCELED)
              .order("-toDate")
              .fetchOne();
      timesheet =
          createTimesheet(
              employee,
              lastTimesheet != null && lastTimesheet.getToDate() != null
                  ? lastTimesheet.getToDate().plusDays(1)
                  : date,
              null);
      timesheet = timesheetRepository.save(timesheet);
    }
    return timesheet;
  }
}
