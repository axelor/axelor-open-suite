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
package com.axelor.apps.base.web.weeklyplanning;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class WeeklyPlanningController {

  public void initPlanning(ActionRequest request, ActionResponse response) {
    WeeklyPlanning planning = request.getContext().asType(WeeklyPlanning.class);
    planning = Beans.get(WeeklyPlanningService.class).initPlanning(planning);
    Integer typeSelect = getTypeSelectFromContext(request.getContext());
    if (typeSelect != null) {
      response.setValue("typeSelect", typeSelect);
    }
    response.setValue("weekDays", planning.getWeekDays());
  }

  public void checkPlanning(ActionRequest request, ActionResponse response) {
    WeeklyPlanning planning = request.getContext().asType(WeeklyPlanning.class);
    try {
      planning = Beans.get(WeeklyPlanningService.class).checkPlanning(planning);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }

  protected Integer getTypeSelectFromContext(Context context) {
    final String typeSelectKey = "_typeSelect";
    while (context != null) {
      if (context.containsKey(typeSelectKey)) {
        return (Integer) context.get(typeSelectKey);
      }
      context = context.getParent();
    }
    return null;
  }
}
