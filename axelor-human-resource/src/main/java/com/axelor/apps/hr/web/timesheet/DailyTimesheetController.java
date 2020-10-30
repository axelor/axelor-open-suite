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
package com.axelor.apps.hr.web.timesheet;

import com.axelor.apps.hr.db.DailyTimesheet;
import com.axelor.apps.hr.db.repo.DailyTimesheetRepository;
import com.axelor.apps.hr.service.timesheet.DailyTimesheetService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class DailyTimesheetController {

  public void updateFromTimesheets(ActionRequest request, ActionResponse response) {

    DailyTimesheet dailyTimesheet = request.getContext().asType(DailyTimesheet.class);

    if (dailyTimesheet.getId() != null) {
      dailyTimesheet = Beans.get(DailyTimesheetRepository.class).find(dailyTimesheet.getId());
    }

    if (dailyTimesheet.getDailyTimesheetDate() != null
        && dailyTimesheet.getDailyTimesheetUser() != null) {
      Beans.get(DailyTimesheetService.class).updateFromTimesheets(dailyTimesheet);
      response.setValue("dailyTimesheetLineList", dailyTimesheet.getDailyTimesheetLineList());
    }
  }

  public void updateFromActivities(ActionRequest request, ActionResponse response) {

    DailyTimesheet dailyTimesheet = request.getContext().asType(DailyTimesheet.class);
    dailyTimesheet = Beans.get(DailyTimesheetRepository.class).find(dailyTimesheet.getId());

    if (dailyTimesheet.getDailyTimesheetDate() != null
        && dailyTimesheet.getDailyTimesheetUser() != null) {
      Beans.get(DailyTimesheetService.class).updateFromActivities(dailyTimesheet);
      response.setReload(true);
    }
  }

  public void updateFromEvents(ActionRequest request, ActionResponse response) {

    DailyTimesheet dailyTimesheet = request.getContext().asType(DailyTimesheet.class);
    dailyTimesheet = Beans.get(DailyTimesheetRepository.class).find(dailyTimesheet.getId());

    if (dailyTimesheet.getDailyTimesheetDate() != null
        && dailyTimesheet.getDailyTimesheetUser() != null) {
      Beans.get(DailyTimesheetService.class).updateFromEvents(dailyTimesheet);
      response.setReload(true);
    }
  }
}
