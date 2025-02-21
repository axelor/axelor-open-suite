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
package com.axelor.apps.intervention.service.planning;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.service.publicHoliday.PublicHolidayService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlanningDateTimeProcessorImpl implements PlanningDateTimeProcessor {

  private final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected final WeeklyPlanningService weeklyPlanningService;
  protected final PublicHolidayService publicHolidayService;
  protected WeeklyPlanning planning = null;
  protected Company company = null;
  protected Operation operation = null;
  protected LocalDateTime fromDateTime = null;
  protected LocalDateTime toDateTime = null;
  protected Long seconds = null;
  protected DayPlanning currentDay = null;
  protected DayPlanningPeriod currentPeriod = null;
  protected DayPlanningPeriod nearestPeriod = null;

  @Inject
  public PlanningDateTimeProcessorImpl(
      WeeklyPlanningService weeklyPlanningService, PublicHolidayService publicHolidayService) {
    this.weeklyPlanningService = weeklyPlanningService;
    this.publicHolidayService = publicHolidayService;
  }

  @Override
  public PlanningDateTimeProcessor from(LocalDateTime from) {
    this.fromDateTime = from;
    if (toDateTime != null) {
      if (fromDateTime.compareTo(toDateTime) <= 0) {
        this.operation = Operation.ADD;
      } else {
        this.operation = Operation.SUB;
      }
    }
    return this;
  }

  @Override
  public PlanningDateTimeProcessor to(LocalDateTime to) {
    this.toDateTime = to;
    if (fromDateTime != null) {
      if (fromDateTime.compareTo(toDateTime) <= 0) {
        this.operation = Operation.ADD;
      } else {
        this.operation = Operation.SUB;
      }
    }
    this.seconds = 0L;
    return this;
  }

  @Override
  public PlanningDateTimeProcessor with(WeeklyPlanning planning, Company company) {
    this.planning = planning;
    this.company = company;
    return this;
  }

  @Override
  public PlanningDateTimeProcessor processing(Operation operation, Long seconds) {
    this.operation = operation;
    this.seconds = seconds;
    return this;
  }

  protected DayPlanningPeriod findPeriod(DayPlanning dayPlanning, LocalDateTime dateTime) {
    if (dayPlanning != null
        && dayPlanning.getMorningFrom() != null
        && dayPlanning.getMorningTo() != null
        && isBetween(
            dateTime.with(dayPlanning.getMorningFrom()),
            dateTime.with(dayPlanning.getMorningTo()),
            dateTime)) {
      return new DayPlanningPeriod(dayPlanning.getMorningFrom(), dayPlanning.getMorningTo());
    }
    if (dayPlanning != null
        && dayPlanning.getAfternoonFrom() != null
        && dayPlanning.getAfternoonTo() != null
        && isBetween(
            dateTime.with(dayPlanning.getAfternoonFrom()),
            dateTime.with(dayPlanning.getAfternoonTo()),
            dateTime)) {
      return new DayPlanningPeriod(dayPlanning.getAfternoonFrom(), dayPlanning.getAfternoonTo());
    }
    return null;
  }

  protected boolean isBetween(
      LocalDateTime dateFrame1, LocalDateTime dateFrame2, LocalDateTime date) {
    return (dateFrame2 == null && (date.isAfter(dateFrame1) || date.isEqual(dateFrame1)))
        || (dateFrame2 != null
            && (date.isAfter(dateFrame1) || date.isEqual(dateFrame1))
            && (date.isBefore(dateFrame2) || date.isEqual(dateFrame2)));
  }

  protected boolean isPublicHoliday(LocalDate date) {
    if (company == null) {
      return false;
    }
    return publicHolidayService.checkPublicHolidayDay(
        date, company.getPublicHolidayEventsPlanning());
  }

  protected <T> T postProcess(T t) {
    LOG.debug("Computation result : {}", t);
    return t;
  }

  @Override
  public LocalDateTime compute() {
    Objects.requireNonNull(planning);
    Objects.requireNonNull(fromDateTime);
    Objects.requireNonNull(seconds);
    Objects.requireNonNull(operation);
    currentDay = weeklyPlanningService.findDayPlanning(planning, fromDateTime.toLocalDate());
    if (currentDay == null) {
      throw new IllegalStateException("Invalid planning.");
    }

    currentPeriod = findPeriod(currentDay, fromDateTime);
    nearestPeriod = operation.getNearestPeriod(currentDay, fromDateTime);

    if (currentPeriod == null && nearestPeriod == null
        || isPublicHoliday(fromDateTime.toLocalDate())) {
      fromDateTime = operation.getNextDayDateTime(fromDateTime);
      return compute();
    }
    if (currentPeriod == null) {
      fromDateTime = operation.goToPeriodStart(nearestPeriod, fromDateTime);
      return compute();
    }
    LocalDateTime nextDateTime = operation.compute(fromDateTime, seconds);
    if (fromDateTime.toLocalDate().equals(nextDateTime.toLocalDate())
        && currentPeriod.include(nextDateTime)) {
      return postProcess(nextDateTime);
    } else {
      nextDateTime = operation.goToPeriodEnd(currentPeriod, fromDateTime);
      seconds -= Math.abs(ChronoUnit.SECONDS.between(fromDateTime, nextDateTime));
      fromDateTime = operation.compute(nextDateTime, 1L);
      return compute();
    }
  }

  @Override
  public Long diff() {
    Objects.requireNonNull(planning);
    Objects.requireNonNull(fromDateTime);
    Objects.requireNonNull(toDateTime);
    Objects.requireNonNull(operation);
    currentDay = weeklyPlanningService.findDayPlanning(planning, fromDateTime.toLocalDate());
    if (currentDay == null) {
      throw new IllegalStateException("Invalid planning.");
    }

    currentPeriod = findPeriod(currentDay, fromDateTime);
    nearestPeriod = operation.getNearestPeriod(currentDay, fromDateTime);

    if (currentPeriod == null && nearestPeriod == null
        || isPublicHoliday(fromDateTime.toLocalDate())) {
      if (fromDateTime.toLocalDate().equals(toDateTime.toLocalDate())) {
        return postProcess(seconds);
      }
      fromDateTime = operation.getNextDayDateTime(fromDateTime);
      return diff();
    }
    if (currentPeriod == null) {
      if (fromDateTime.toLocalDate().equals(toDateTime.toLocalDate())
          && Boolean.TRUE.equals(operation.isBefore(nearestPeriod, toDateTime))) {
        return postProcess(seconds);
      }
      fromDateTime = operation.goToPeriodStart(nearestPeriod, fromDateTime);
      return diff();
    }
    if (fromDateTime.toLocalDate().equals(toDateTime.toLocalDate())
        && currentPeriod.include(fromDateTime)
        && toDateTime != null
        && currentPeriod.include(toDateTime)) {
      return postProcess(seconds + ChronoUnit.SECONDS.between(fromDateTime, toDateTime));
    } else {
      LocalDateTime nextDateTime = operation.goToPeriodEnd(currentPeriod, fromDateTime);
      seconds += ChronoUnit.SECONDS.between(fromDateTime, nextDateTime);
      fromDateTime = operation.compute(nextDateTime, 1L);
      return diff();
    }
  }
}
