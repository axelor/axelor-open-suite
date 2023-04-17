/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TimesheetComputeNameServiceImpl implements TimesheetComputeNameService {

  @Override
  public String computeTimesheetFullname(Timesheet timesheet) {
    return computeTimesheetFullname(
        timesheet.getEmployee(), timesheet.getFromDate(), timesheet.getToDate());
  }

  public static String computeTimesheetFullname(
      Employee employee, LocalDate fromDate, LocalDate toDate) {
    DateTimeFormatter pattern = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    if (employee != null && employee.getName() != null && fromDate != null && toDate != null) {
      return employee.getName() + " " + fromDate.format(pattern) + "-" + toDate.format(pattern);
    } else if (employee != null && employee.getName() != null && fromDate != null) {
      return employee.getName() + " " + fromDate.format(pattern);
    } else if (employee != null && employee.getName() != null) {
      return employee.getName();
    } else {
      return "";
    }
  }
}
