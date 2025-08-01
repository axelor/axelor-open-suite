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
package com.axelor.apps.hr.service.timesheet.editor;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.rest.dto.TimesheetLineEditorResponse;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.ws.rs.core.Response;

public interface TimesheetLineTimesheetEditorService {

  /**
   * This service updates {@link TimesheetLine} by searching based on {@link Timesheet}, {@link
   * Project} and {@link ProjectTask} and creates new {@link TimesheetLine} or update existing with
   * difference of duration or hoursDuration
   */
  Response createOrUpdateTimesheetLine(
      Timesheet timesheet,
      Project project,
      ProjectTask projectTask,
      Product product,
      BigDecimal duration,
      BigDecimal hoursDuration,
      LocalDate date,
      String comments,
      Boolean toInvoice)
      throws AxelorException;

  void removeTimesheetLines(
      Timesheet timesheet, LocalDate date, Project project, ProjectTask projectTask);

  TimesheetLineEditorResponse buildEditorReponse(
      Timesheet timesheet, LocalDate date, Project project, ProjectTask projectTask);
}
