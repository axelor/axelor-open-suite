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
package com.axelor.apps.intervention.events;

import com.axelor.apps.base.callable.ControllerCallableTool;
import com.axelor.apps.intervention.db.Intervention;
import com.axelor.apps.intervention.db.repo.InterventionRepository;
import com.axelor.apps.intervention.service.InterventionSurveyGenerator;
import com.axelor.apps.intervention.service.helper.InterventionHelper;
import com.axelor.apps.intervention.service.planning.PlanningDateTimeService;
import com.axelor.db.JpaRepository;
import com.axelor.event.Observes;
import com.axelor.events.PostRequest;
import com.axelor.events.PreRequest;
import com.axelor.events.RequestEvent;
import com.axelor.events.qualifiers.EntityType;
import com.axelor.inject.Beans;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import javax.inject.Named;
import org.apache.commons.collections.CollectionUtils;

public class InterventionObserver {

  private static final String END_DATE_TIME = "endDateTime";

  void onEquipmentsChange(
      @Observes @Named(RequestEvent.SAVE) @EntityType(Intervention.class) PostRequest event) {
    if (!CollectionUtils.isEmpty(event.getRequest().getRecords())) {
      for (Object data : event.getRequest().getRecords()) {
        processPostRequestData(data);
      }
    } else if (event.getRequest().getData() != null) {
      processPostRequestData(event.getRequest().getData());
    }
  }

  protected void processPostRequestData(Object data) {
    if (data instanceof Map) {
      @SuppressWarnings("unchecked")
      final Map<String, Object> map = (Map<String, Object>) data;
      Object id = map.getOrDefault("id", 0);
      if (id == null) {
        return;
      }
      Intervention intervention =
          JpaRepository.of(Intervention.class).find(Long.parseLong(String.valueOf(id)));
      if (intervention == null) {
        return;
      }

      if (map.containsKey("equipmentSet")
          && intervention.getStatusSelect().compareTo(InterventionRepository.INTER_STATUS_PLANNED)
              >= 0
          && InterventionHelper.isSurveyGenerated(intervention)) {
        ControllerCallableTool<Integer> controllerCallableTool = new ControllerCallableTool<>();
        InterventionSurveyGenerator interventionSurveyGenerator =
            Beans.get(InterventionSurveyGenerator.class);
        interventionSurveyGenerator.configure(intervention);
        controllerCallableTool.runInSeparateThread(interventionSurveyGenerator, null);
      }
    }
  }

  void onEndDateTimeChange(
      @Observes @Named(RequestEvent.SAVE) @EntityType(Intervention.class) PreRequest event) {
    if (event.getRequest().getData() == null) {
      if (!CollectionUtils.isEmpty(event.getRequest().getRecords())) {
        for (Object data : event.getRequest().getRecords()) {
          processPreRequestData(data);
        }
      }
    } else if (event.getRequest().getData() != null) {
      processPreRequestData(event.getRequest().getData());
    }
  }

  protected void processPreRequestData(Object data) {
    if (!(data instanceof Map)) {
      return;
    }
    @SuppressWarnings("unchecked")
    final Map<String, Object> map = (Map<String, Object>) data;
    Object id = map.getOrDefault("id", 0);
    if (id == null) {
      return;
    }
    Intervention intervention =
        JpaRepository.of(Intervention.class).find(Long.parseLong(String.valueOf(id)));
    if (intervention == null
        || intervention.getStatusSelect() < InterventionRepository.INTER_STATUS_FINISHED
        || !map.containsKey(END_DATE_TIME)) {
      return;
    }
    String totalDuration = "totalDuration";
    ZoneId zoneId = ZoneId.of(intervention.getCompany().getTimezone());
    ZonedDateTime endDateTime = ZonedDateTime.of(intervention.getEndDateTime(), zoneId);
    ZonedDateTime newEndDateTimeUtc =
        ZonedDateTime.of(
            LocalDateTime.parse(map.get(END_DATE_TIME).toString(), DateTimeFormatter.ISO_DATE_TIME),
            ZoneId.of("UTC"));
    ZonedDateTime newEndDateTime = newEndDateTimeUtc.withZoneSameInstant(zoneId);
    if (!newEndDateTime.equals(endDateTime)) {
      if (intervention.getTotalDuration() == null) {
        map.put(totalDuration, 0L);
      } else {
        map.put(totalDuration, intervention.getTotalDuration());
      }
      map.put(
          totalDuration,
          InterventionHelper.roundToNextHalfHour(
              Long.parseLong(map.get(totalDuration).toString())
                  + Beans.get(PlanningDateTimeService.class)
                      .diff(
                          intervention.getCompany(),
                          intervention.getCompany().getWeeklyPlanning(),
                          endDateTime.toLocalDateTime(),
                          newEndDateTime.toLocalDateTime())));
    }
  }
}
