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
package com.axelor.apps.hr.event;

import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.hr.service.project.ProjectPlanningTimeServiceImpl;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.db.JPA;
import com.axelor.db.mapper.Adapter;
import com.axelor.event.Observes;
import com.axelor.events.PostRequest;
import com.axelor.events.PreRequest;
import com.axelor.events.RequestEvent;
import com.axelor.events.qualifiers.EntityType;
import com.axelor.inject.Beans;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Named;

public class ICalendarEventObserver {
  void onSave(
      @Observes @Named(RequestEvent.SAVE) @EntityType(ICalendarEvent.class) PostRequest event) {
    AppProjectService appProjectService = Beans.get(AppProjectService.class);
    if (!appProjectService.isApp("project")
        || !appProjectService.getAppProject().getEnableEventCreation()) {
      return;
    }
    Map<String, Object> data = event.getRequest().getData();

    Object id = data.getOrDefault("id", 0);

    ProjectPlanningTime projectPlanningTime =
        JPA.all(ProjectPlanningTime.class)
            .filter("self.icalendarEvent.id = :icalendarEvent")
            .bind("icalendarEvent", id)
            .fetchOne();
    if (projectPlanningTime == null) {
      return;
    }
    LocalDateTime startDateTime =
        (LocalDateTime)
            Adapter.adapt(
                data.get("startDateTime"), LocalDateTime.class, LocalDateTime.class, null);
    LocalDateTime endDateTime =
        (LocalDateTime)
            Adapter.adapt(data.get("endDateTime"), LocalDateTime.class, LocalDateTime.class, null);
    String description = String.valueOf(data.get("description"));

    Beans.get(ProjectPlanningTimeServiceImpl.class)
        .updateProjectPlanningTime(projectPlanningTime, startDateTime, endDateTime, description);
  }

  void onDelete(
      @Observes @Named(RequestEvent.REMOVE) @EntityType(ICalendarEvent.class) PreRequest event) {
    List<Object> records = event.getRequest().getRecords();
    List<Long> ids = new ArrayList<>();
    for (Object data : records) {
      Map<String, Object> map = (Map<String, Object>) data;
      ids.add(Long.parseLong(String.valueOf(map.getOrDefault("id", 0))));
    }

    Beans.get(ProjectPlanningTimeServiceImpl.class).deleteLinkedProjectPlanningTime(ids);
  }
}
