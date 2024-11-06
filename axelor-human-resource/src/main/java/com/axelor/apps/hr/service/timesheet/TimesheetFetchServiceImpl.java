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
package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.auth.AuthUtils;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.Objects;

public class TimesheetFetchServiceImpl implements TimesheetFetchService {

  protected EmployeeService employeeService;
  protected AppHumanResourceService appHumanResourceService;
  protected TimesheetRepository timesheetRepository;
  protected TimesheetCreateService timesheetCreateService;

  @Inject
  public TimesheetFetchServiceImpl(
      EmployeeService employeeService,
      AppHumanResourceService appHumanResourceService,
      TimesheetRepository timesheetRepository,
      TimesheetCreateService timesheetCreateService) {
    this.employeeService = employeeService;
    this.appHumanResourceService = appHumanResourceService;
    this.timesheetRepository = timesheetRepository;
    this.timesheetCreateService = timesheetCreateService;
  }

  @Override
  public Timesheet getDraftTimesheet(Employee employee, LocalDate fromDate, LocalDate toDate) {

    Objects.requireNonNull(employee);
    Objects.requireNonNull(fromDate);

    String filter =
        "self.statusSelect = :timesheetStatus AND self.employee = :employee AND self.fromDate <= :fromDate AND self.isCompleted = false";

    if (toDate != null) {
      filter = filter.concat(" AND (self.toDate IS NULL OR :toDate <= self.toDate)");
    }

    Timesheet timesheet =
        timesheetRepository
            .all()
            .filter(filter)
            .bind("timesheetStatus", TimesheetRepository.STATUS_DRAFT)
            .bind("employee", employee)
            .bind("fromDate", fromDate)
            .bind("toDate", toDate)
            .order("-id")
            .fetchOne();
    if (timesheet != null) {
      return timesheet;
    } else {
      return null;
    }
  }

  @Override
  public Timesheet getCurrentOrCreateTimesheet() throws AxelorException {

    return getOrCreateOpenTimesheet(
        employeeService.getEmployee(AuthUtils.getUser()),
        appHumanResourceService.getTodayDateTime().toLocalDate(),
        null);
  }

  @Override
  public Timesheet getOrCreateOpenTimesheet(Employee employee, LocalDate fromDate, LocalDate toDate)
      throws AxelorException {
    Timesheet timesheet = getDraftTimesheet(employee, fromDate, toDate);
    if (timesheet == null) {

      timesheet = timesheetCreateService.createTimesheet(employee, fromDate, toDate);
    }
    return timesheet;
  }
}
