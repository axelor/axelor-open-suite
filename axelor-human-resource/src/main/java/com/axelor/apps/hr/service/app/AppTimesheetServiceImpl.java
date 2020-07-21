/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.app;

import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class AppTimesheetServiceImpl implements AppTimesheetService {
  protected TimesheetRepository timesheetRepo;

  @Inject
  public AppTimesheetServiceImpl(TimesheetRepository timesheetRepo) {
    this.timesheetRepo = timesheetRepo;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void switchTimesheetEditors(Boolean state) {
    List<Timesheet> timesheets;
    Query<Timesheet> query = timesheetRepo.all().order("id");
    int offset = 0;
    while (!(timesheets = query.fetch(AbstractBatch.FETCH_LIMIT, offset)).isEmpty()) {
      for (Timesheet timesheet : timesheets) {
        offset++;
        if (timesheet.getShowEditor() != state) {
          timesheet.setShowEditor(state);
          timesheetRepo.save(timesheet);
        }
      }
      JPA.clear();
    }
  }
}
