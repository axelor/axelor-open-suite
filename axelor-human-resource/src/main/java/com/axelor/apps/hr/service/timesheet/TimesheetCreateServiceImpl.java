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
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.user.UserHrService;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;

public class TimesheetCreateServiceImpl implements TimesheetCreateService {

  protected UserHrService userHrService;
  protected ProjectRepository projectRepository;
  protected TimesheetLineService timesheetLineService;
  protected TimesheetRepository timesheetRepository;
  protected TimesheetLineCreateService timesheetLineCreateService;

  @Inject
  public TimesheetCreateServiceImpl(
      UserHrService userHrService,
      ProjectRepository projectRepository,
      TimesheetLineService timesheetLineService,
      TimesheetRepository timesheetRepository,
      TimesheetLineCreateService timesheetLineCreateService) {
    this.userHrService = userHrService;
    this.projectRepository = projectRepository;
    this.timesheetLineService = timesheetLineService;
    this.timesheetRepository = timesheetRepository;
    this.timesheetLineCreateService = timesheetLineCreateService;
  }

  @Transactional
  @Override
  public Timesheet createTimesheet(Employee employee, LocalDate fromDate, LocalDate toDate) {
    Timesheet timesheet = new Timesheet();
    timesheet.setEmployee(employee);

    Company company = null;
    if (employee != null) {
      if (employee.getMainEmploymentContract() != null) {
        company = employee.getMainEmploymentContract().getPayCompany();
      } else if (employee.getUser() != null) {
        company = employee.getUser().getActiveCompany();
      }
    }

    String timeLoggingPreferenceSelect =
        employee == null ? null : employee.getTimeLoggingPreferenceSelect();
    timesheet.setTimeLoggingPreferenceSelect(timeLoggingPreferenceSelect);
    timesheet.setCompany(company);
    timesheet.setFromDate(fromDate);
    timesheet.setToDate(toDate);
    timesheet.setStatusSelect(TimesheetRepository.STATUS_DRAFT);

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
}
