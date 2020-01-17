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
package com.axelor.apps.base.web.weeklyplanning;

import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class WeeklyPlanningController {

  @Inject private WeeklyPlanningService weeklyPlanningService;

  public void initPlanning(ActionRequest request, ActionResponse response) {
    WeeklyPlanning planning = request.getContext().asType(WeeklyPlanning.class);
    planning = weeklyPlanningService.initPlanning(planning);
    response.setValue("weekDays", planning.getWeekDays());
  }

  public void checkPlanning(ActionRequest request, ActionResponse response) {
    WeeklyPlanning planning = request.getContext().asType(WeeklyPlanning.class);
    try {
      planning = weeklyPlanningService.checkPlanning(planning);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }
}
