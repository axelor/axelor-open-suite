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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.apps.project.db.Project;
import com.axelor.db.Query;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.Optional;

public class TimesheetQueryServiceImpl implements TimesheetQueryService {

  protected TimesheetRepository timesheetRepository;
  protected EmployeeRepository employeeRepository;
  protected EmployeeService employeeService;

  @Inject
  public TimesheetQueryServiceImpl(
      TimesheetRepository timesheetRepository,
      EmployeeRepository employeeRepository,
      EmployeeService employeeService) {
    this.timesheetRepository = timesheetRepository;
    this.employeeRepository = employeeRepository;
    this.employeeService = employeeService;
  }

  @Override
  public Query<Timesheet> getTimesheetQuery(TimesheetLine timesheetLine) {
    Company defaultCompany = employeeService.getDefaultCompany(timesheetLine.getEmployee());
    return getTimesheetQuery(
        timesheetLine.getEmployee(),
        Optional.of(timesheetLine)
            .map(TimesheetLine::getProject)
            .map(Project::getCompany)
            .orElse(defaultCompany),
        timesheetLine.getDate());
  }

  @Override
  public Query<Timesheet> getTimesheetQuery(Employee employee, Company company, LocalDate date) {
    return timesheetRepository
        .all()
        .filter(
            "self.employee = ?1 AND self.company = ?2 AND (self.statusSelect = 1 OR self.statusSelect = 2) AND ((?3 BETWEEN self.fromDate AND self.toDate) OR (self.toDate = null))",
            employee,
            company,
            date);
  }
}
