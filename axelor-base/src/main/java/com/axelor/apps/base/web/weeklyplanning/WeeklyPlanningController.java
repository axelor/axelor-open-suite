/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.callable.ControllerCallableTool;
import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningGenerateEventService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.db.mapper.Mapper;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WeeklyPlanningController {

  public void initPlanning(ActionRequest request, ActionResponse response) {
    WeeklyPlanning planning = request.getContext().asType(WeeklyPlanning.class);
    planning = Beans.get(WeeklyPlanningService.class).initPlanning(planning);
    if (request.getContext().containsKey("_typeSelect")) {
      response.setValue("typeSelect", request.getContext().get("_typeSelect"));
    }
    response.setValue("weekDays", planning.getWeekDays());
  }

  public void checkPlanning(ActionRequest request, ActionResponse response) {
    WeeklyPlanning planning = request.getContext().asType(WeeklyPlanning.class);
    try {
      Beans.get(WeeklyPlanningService.class).checkPlanning(planning);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setICalEventValues(ActionRequest request, ActionResponse response)
      throws AxelorException {
    WeeklyPlanning planning = request.getContext().asType(WeeklyPlanning.class);
    WeeklyPlanning weeks = Beans.get(WeeklyPlanningService.class).setICalEventValues(planning);
    WeeklyPlanningGenerateEventService eventService =
        Beans.get(WeeklyPlanningGenerateEventService.class);
    eventService.setWeeksPlanning(weeks);
    ControllerCallableTool<WeeklyPlanning> weekPlanControllerCallableTool =
        new ControllerCallableTool<>();
    weekPlanControllerCallableTool.runInSeparateThread(eventService, response);
    response.setReload(true);
  }

  public void setDateTime(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    ICalendarEvent event = context.asType(ICalendarEvent.class);
    response.setValues(
        Beans.get(WeeklyPlanningService.class)
            .setDateTimeValues(
                event, (String) context.get("startTime"), (String) context.get("endTime")));
  }

  public void setTimeValues(ActionRequest request, ActionResponse response) {
    WeeklyPlanning planning = request.getContext().asType(WeeklyPlanning.class);
    List<Map<String, Object>> weekDays = new ArrayList<>();
    for (ICalendarEvent weekDay : planning.getWeekDays()) {
      Map<String, Object> weekDayMap = Mapper.toMap(weekDay);
      weekDayMap.put("$startTime", weekDay.getStartDateTime().toLocalTime());
      weekDayMap.put("$endTime", weekDay.getEndDateTime().toLocalTime());
      weekDays.add(weekDayMap);
    }
    response.setValue("weekDays", weekDays);
  }
}
