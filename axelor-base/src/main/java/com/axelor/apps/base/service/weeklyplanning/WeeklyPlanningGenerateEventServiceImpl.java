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
package com.axelor.apps.base.service.weeklyplanning;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.db.repo.ICalendarEventRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.base.service.ical.ICalendarEventServiceImpl;
import com.google.inject.Inject;

public class WeeklyPlanningGenerateEventServiceImpl implements WeeklyPlanningGenerateEventService {

  @Inject ICalendarEventServiceImpl iCalendarEventServiceImpl;

  @Inject ICalendarEventRepository eventRepository;

  protected WeeklyPlanning weeks;

  @Override
  public WeeklyPlanning call() throws Exception {
    weeks.getWeekDays().stream()
        .forEach(
            calEvent -> {
              try {
                iCalendarEventServiceImpl.deleteAllByParentId(calEvent.getId());
                calEvent = eventRepository.find(calEvent.getId());
                iCalendarEventServiceImpl.generateRecurrentEvents(
                    calEvent, calEvent.getRecurrenceConfiguration());
              } catch (AxelorException e) {
                TraceBackService.trace(e);
              }
            });
    return null;
  }

  @Override
  public void setWeeksPlanning(WeeklyPlanning weeks) {
    this.weeks = weeks;
  }
}
