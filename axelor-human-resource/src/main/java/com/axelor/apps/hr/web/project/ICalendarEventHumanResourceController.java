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
package com.axelor.apps.hr.web.project;

import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.hr.service.project.ProjectPlanningTimeService;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ICalendarEventHumanResourceController {

  public void loadLinkedPlanningTime(ActionRequest request, ActionResponse response) {
    ICalendarEvent event = request.getContext().asType(ICalendarEvent.class);

    ProjectPlanningTime projectPlanningTime =
        Beans.get(ProjectPlanningTimeService.class).loadLinkedPlanningTime(event);

    if (projectPlanningTime != null) {
      response.setAttr("$_linkedProjectPlanningTime", "hidden", false);
      response.setValue("$_linkedProjectPlanningTime", projectPlanningTime);
    }
  }
}
