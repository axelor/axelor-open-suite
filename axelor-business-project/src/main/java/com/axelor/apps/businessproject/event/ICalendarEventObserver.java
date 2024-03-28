package com.axelor.apps.businessproject.event;

import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.businessproject.service.ProjectPlanningTimeBusinessProjectServiceImpl;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.project.db.ProjectPlanningTime;
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
    AppBusinessProjectService appBusinessProjectService =
        Beans.get(AppBusinessProjectService.class);
    if (!appBusinessProjectService.isApp("business-project")
        || !appBusinessProjectService.getAppBusinessProject().getEnableEventCreation()) {
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

    Beans.get(ProjectPlanningTimeBusinessProjectServiceImpl.class)
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

    Beans.get(ProjectPlanningTimeBusinessProjectServiceImpl.class)
        .deleteLinkedProjectPlanningTime(ids);
  }
}
