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

import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.project.db.Project;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class TimesheetLineRemoveServiceImpl implements TimesheetLineRemoveService {

  protected TimesheetLineRepository timeSheetLineRepository;

  @Inject
  public TimesheetLineRemoveServiceImpl(TimesheetLineRepository timeSheetLineRepository) {
    this.timeSheetLineRepository = timeSheetLineRepository;
  }

  @Override
  public void removeTimesheetLines(List<Integer> projectTimesheetLineIds) {
    for (Integer id : projectTimesheetLineIds) {
      removeTimesheetLine(timeSheetLineRepository.find(Long.valueOf(id)));
    }
  }

  @Transactional
  protected void removeTimesheetLine(TimesheetLine timesheetLine) {
    if (timesheetLine == null) {
      return;
    }

    if (timesheetLine.getTimesheet() != null) {
      Timesheet timesheet = timesheetLine.getTimesheet();
      timesheetLine.setTimesheet(null);
      timesheet.removeTimesheetLineListItem(timesheetLine);
    }
    if (timesheetLine.getProject() != null) {
      Project project = timesheetLine.getProject();
      timesheetLine.setProject(null);
      project.removeTimesheetLineListItem(timesheetLine);
    }

    timeSheetLineRepository.remove(timesheetLine);
  }
}
