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
package com.axelor.apps.hr.service.publicHoliday;

import com.axelor.apps.base.db.EventsPlanning;
import com.axelor.apps.base.db.EventsPlanningLine;
import com.axelor.apps.base.db.repo.EventsPlanningLineRepository;
import com.axelor.apps.base.service.publicHoliday.PublicHolidayService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.hr.db.Employee;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.List;

public class PublicHolidayHrService extends PublicHolidayService {

  @Inject
  public PublicHolidayHrService(
      WeeklyPlanningService weeklyPlanningService,
      EventsPlanningLineRepository eventsPlanningLineRepo) {
    super(weeklyPlanningService, eventsPlanningLineRepo);
  }

  public boolean checkPublicHolidayDay(LocalDate date, Employee employee) {
    return super.checkPublicHolidayDay(date, employee.getPublicHolidayEventsPlanning());
  }

  public int getImposedDayNumber(Employee employee, LocalDate startDate, LocalDate endDate) {

    EventsPlanning imposedDays = employee.getImposedDayEventsPlanning();

    if (imposedDays == null
        || imposedDays.getEventsPlanningLineList() == null
        || imposedDays.getEventsPlanningLineList().isEmpty()) {
      return 0;
    }

    List<EventsPlanningLine> imposedDayList =
        eventsPlanningLineRepo
            .all()
            .filter(
                "self.eventsPlanning = ?1 AND self.date BETWEEN ?2 AND ?3",
                imposedDays,
                startDate,
                endDate)
            .fetch();

    return imposedDayList.size();
  }
}
