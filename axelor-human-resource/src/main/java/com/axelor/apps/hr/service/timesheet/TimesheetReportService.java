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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.TimesheetReport;
import com.axelor.inject.Beans;
import com.axelor.message.db.Message;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TimesheetReportService {

  static TimesheetReportService getInstance() {
    return Beans.get(TimesheetReportService.class);
  }

  Set<Employee> getEmployeeToBeReminded(TimesheetReport timesheetReport);

  List<Message> sendReminders(TimesheetReport timesheetReport) throws AxelorException;

  List<Map<String, Object>> getTimesheetReportList(String TimesheetReportId);
}
