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
import com.axelor.apps.base.db.Product;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.TSTimer;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.ProjectTask;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public interface TimesheetLineCreateService {

  TimesheetLine createTimesheetLine(
      Project project,
      ProjectTask projectTask,
      Product product,
      LocalDate date,
      Timesheet timesheet,
      BigDecimal duration,
      BigDecimal hoursDuration,
      String comments,
      boolean toInvoice)
      throws AxelorException;

  TimesheetLine createTimesheetLine(
      Project project,
      Product product,
      Employee employee,
      LocalDate date,
      Timesheet timesheet,
      BigDecimal hours,
      String comments);

  TimesheetLine createTimesheetLine(
      Project project,
      ProjectTask projectTask,
      Product product,
      Employee employee,
      LocalDate date,
      Timesheet timesheet,
      BigDecimal hours,
      String comments,
      TSTimer timer);

  TimesheetLine createTimesheetLine(
      Employee employee, LocalDate date, Timesheet timesheet, BigDecimal hours, String comments)
      throws AxelorException;

  void createTimesheetLinesUsingPlanning(
      List<Pair<ProjectPlanningTime, BigDecimal>> projectPlanningTimeListWithDuration,
      LocalDate date,
      Timesheet timesheet)
      throws AxelorException;
}
