/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.publicHoliday;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.EventsPlanning;
import com.axelor.apps.base.db.EventsPlanningLine;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.db.repo.EventsPlanningLineRepository;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class PublicHolidayServiceImpl implements PublicHolidayService {

  protected WeeklyPlanningService weeklyPlanningService;
  protected EventsPlanningLineRepository eventsPlanningLineRepo;

  @Inject
  public PublicHolidayServiceImpl(
      WeeklyPlanningService weeklyPlanningService,
      EventsPlanningLineRepository eventsPlanningLineRepo) {

    this.weeklyPlanningService = weeklyPlanningService;
    this.eventsPlanningLineRepo = eventsPlanningLineRepo;
  }

  @Override
  public BigDecimal computePublicHolidayDays(
      LocalDate fromDate,
      LocalDate toDate,
      WeeklyPlanning weeklyPlanning,
      EventsPlanning publicHolidayPlanning) {
    BigDecimal publicHolidayDays = BigDecimal.ZERO;

    List<EventsPlanningLine> publicHolidayDayList =
        eventsPlanningLineRepo
            .all()
            .filter(
                "self.eventsPlanning = ?1 AND self.date BETWEEN ?2 AND ?3",
                publicHolidayPlanning,
                fromDate,
                toDate)
            .fetch();
    for (EventsPlanningLine publicHolidayDay : publicHolidayDayList) {
      publicHolidayDays =
          publicHolidayDays.add(
              BigDecimal.valueOf(
                  weeklyPlanningService.getWorkingDayValueInDays(
                      weeklyPlanning, publicHolidayDay.getDate())));
    }
    return publicHolidayDays;
  }

  /**
   * Returns true if the given date is a public holiday in the given public holiday events planning.
   *
   * @param date
   * @param publicHolidayEventsPlanning
   * @return
   */
  @Override
  public boolean checkPublicHolidayDay(LocalDate date, EventsPlanning publicHolidayEventsPlanning) {

    if (publicHolidayEventsPlanning == null) {
      return false;
    }

    List<EventsPlanningLine> publicHolidayDayList =
        eventsPlanningLineRepo
            .all()
            .filter(
                "self.eventsPlanning = ?1 AND self.date = ?2", publicHolidayEventsPlanning, date)
            .fetch();
    return ObjectUtils.notEmpty(publicHolidayDayList);
  }

  @Transactional(rollbackOn = Exception.class)
  @Override
  public void generateEventsPlanningLines(
      EventsPlanning eventsPlanning, LocalDate startDate, LocalDate endDate, String description) {
    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
      EventsPlanningLine eventsPlanningLine = new EventsPlanningLine();
      eventsPlanningLine.setYear(date.getYear());
      eventsPlanningLine.setDate(date);
      eventsPlanningLine.setDescription(description);
      eventsPlanningLine.setEventsPlanning(eventsPlanning);
      eventsPlanningLineRepo.save(eventsPlanningLine);
    }
  }

  @Override
  public LocalDate getFreeDay(LocalDate date, Company company) {
    Objects.requireNonNull(date);
    Objects.requireNonNull(company);

    var publicHolidayEventsPlanning = company.getPublicHolidayEventsPlanning();
    var weeklyPlanning = company.getWeeklyPlanning();

    if (publicHolidayEventsPlanning == null && weeklyPlanning == null) {
      return date;
    }

    if (!checkPublicHolidayDay(date, publicHolidayEventsPlanning)
        && weeklyPlanningService.isWorkingDay(weeklyPlanning, date)) {
      return date;
    }

    return getFreeDay(date.plusDays(1), company);
  }
}
