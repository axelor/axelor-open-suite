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

import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.ListUtils;

public class TimesheetRemoveServiceImpl implements TimesheetRemoveService {

  protected TimesheetLineRepository timesheetLineRepository;

  @Inject
  public TimesheetRemoveServiceImpl(TimesheetLineRepository timesheetLineRepository) {
    this.timesheetLineRepository = timesheetLineRepository;
  }

  @Override
  @Transactional
  public void removeAfterToDateTimesheetLines(Timesheet timesheet) {

    List<TimesheetLine> removedTimesheetLines = new ArrayList<>();

    for (TimesheetLine timesheetLine : ListUtils.emptyIfNull(timesheet.getTimesheetLineList())) {
      if (timesheetLine.getDate().isAfter(timesheet.getToDate())) {
        removedTimesheetLines.add(timesheetLine);
        if (timesheetLine.getId() != null) {
          timesheetLineRepository.remove(timesheetLine);
        }
      }
    }
    timesheet.getTimesheetLineList().removeAll(removedTimesheetLines);
  }
}
