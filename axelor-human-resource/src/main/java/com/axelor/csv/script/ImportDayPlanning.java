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
package com.axelor.csv.script;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.repo.ICalendarEventRepository;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningServiceImp;
import com.axelor.base.service.ical.ICalendarEventServiceImpl;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

public class ImportDayPlanning {

  @Inject ICalendarEventRepository eventRepo;

  @Inject WeeklyPlanningServiceImp weekService;

  @Inject ICalendarEventServiceImpl iCalendarEventServiceImpl;

  @Transactional(rollbackOn = Exception.class)
  public Object importDates(Object bean, Map<String, Object> values) throws AxelorException {
    assert bean instanceof ICalendarEvent;
    ICalendarEvent event = (ICalendarEvent) bean;
    event.setVisibilitySelect(ICalendarEventRepository.VISIBILITY_PUBLIC);
    event.setDisponibilitySelect(ICalendarEventRepository.DISPONIBILITY_BUSY);
    LocalTime starTime = LocalTime.parse((String) values.get("starTime"));
    LocalTime endTime = LocalTime.parse((String) values.get("endTime"));
    LocalDate date =
        LocalDate.now().with(weekService.getDayOfWeek(event.getSubject().split("\\s+")[0]));
    event.setSubject(
        String.format("Week-%d %s", weekService.getWeekNo(date), values.get("subject")));
    event.setStartDateTime(LocalDateTime.of(date, starTime));
    event.setEndDateTime(LocalDateTime.of(date, endTime));
    event.setRecurrenceConfiguration(weekService.setConfigDayValue(event));
    eventRepo.save(event);
    iCalendarEventServiceImpl.generateRecurrentEvents(event, event.getRecurrenceConfiguration());
    return event;
  }
}
