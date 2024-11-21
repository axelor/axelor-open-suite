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

import com.axelor.apps.base.service.DateService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimesheetComputeNameServiceImpl implements TimesheetComputeNameService {

  protected DateService dateService;

  @Inject
  public TimesheetComputeNameServiceImpl(DateService dateService) {
    this.dateService = dateService;
  }

  @Override
  public String computeTimesheetFullname(Timesheet timesheet) {
    return computeTimesheetFullname(
        timesheet.getEmployee(), timesheet.getFromDate(), timesheet.getToDate());
  }

  @Override
  public String computeTimesheetFullname(Employee employee, LocalDate fromDate, LocalDate toDate) {
    try {
      DateTimeFormatter pattern = dateService.getDateFormat();

      if (employee != null && employee.getName() != null && fromDate != null && toDate != null) {
        return employee.getName() + " " + fromDate.format(pattern) + "-" + toDate.format(pattern);
      } else if (employee != null && employee.getName() != null && fromDate != null) {
        return employee.getName() + " " + fromDate.format(pattern);
      } else if (employee != null && employee.getName() != null) {
        return employee.getName();
      } else {
        return "";
      }
    } catch (Exception e) {
      Logger logger = LoggerFactory.getLogger(getClass());
      logger.error(e.getMessage());
    }
    return "";
  }
}
