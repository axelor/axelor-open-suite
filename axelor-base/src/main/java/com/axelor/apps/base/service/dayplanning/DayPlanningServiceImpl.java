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
package com.axelor.apps.base.service.dayplanning;

import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.utils.date.DurationTool;
import com.google.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

public class DayPlanningServiceImpl implements DayPlanningService {

  protected WeeklyPlanningService weeklyPlanningService;

  @Inject
  public DayPlanningServiceImpl(WeeklyPlanningService weeklyPlanningService) {
    this.weeklyPlanningService = weeklyPlanningService;
  }

  @Override
  public Optional<LocalDateTime> getAllowedStartDateTPeriodAt(
      DayPlanning dayPlanning, LocalDateTime dateT) {

    return getAllowedStartDateTPeriodAt(dayPlanning, dateT, 0);
  }

  protected Optional<LocalDateTime> getAllowedStartDateTPeriodAt(
      DayPlanning dayPlanning, LocalDateTime dateT, int nbDaysJump) {

    if (nbDaysJump == 8) {
      return Optional.empty();
    }

    if (dayPlanning == null) {
      return Optional.of(dateT);
    }

    LocalTime morningFromTime = dayPlanning.getMorningFrom();
    LocalTime morningToTime = dayPlanning.getMorningTo();
    LocalTime afternoonFromTime = dayPlanning.getAfternoonFrom();
    LocalTime afternoonToTime = dayPlanning.getAfternoonTo();

    LocalTime localTime = dateT.toLocalTime();

    if (morningFromTime != null && morningToTime != null) {

      if (localTime.compareTo(morningFromTime) >= 0 && localTime.compareTo(morningToTime) < 0) {
        return Optional.of(dateT);
      }

      if (localTime.isBefore(morningFromTime)) {
        return Optional.of(getDateTAtLocalTime(dateT, morningFromTime));
      }
    }

    if (afternoonFromTime != null && afternoonToTime != null) {

      if (localTime.compareTo(afternoonFromTime) >= 0 && localTime.compareTo(afternoonToTime) < 0) {
        return Optional.of(dateT);
      }

      if (localTime.isBefore(afternoonFromTime)) {
        return Optional.of(getDateTAtLocalTime(dateT, afternoonFromTime));
      }
    }

    LocalDateTime nextDay = dateT.plusDays(1).with(LocalTime.MIN);
    return getAllowedStartDateTPeriodAt(
        weeklyPlanningService.findDayPlanning(
            dayPlanning.getWeeklyPlanning(), nextDay.toLocalDate()),
        nextDay,
        nbDaysJump + 1);
  }

  protected LocalDateTime getDateTAtLocalTime(LocalDateTime dateT, LocalTime localTime) {

    return dateT
        .withHour(localTime.getHour())
        .withMinute(localTime.getMinute())
        .withSecond(localTime.getSecond());
  }

  @Override
  public long computeVoidDurationBetween(
      DayPlanning dayPlanning, LocalDateTime startDateT, LocalDateTime endDateT) {

    if (startDateT.isAfter(endDateT)) {
      return 0;
    }

    // If it is not the same day
    if (startDateT.toLocalDate().compareTo(endDateT.toLocalDate()) < 0) {
      return computeVoidDurationBetween(
          dayPlanning, startDateT, startDateT.with(LocalTime.MAX), endDateT);
    }

    // Same day
    return computeVoidDurationBetween(
        dayPlanning, startDateT.toLocalTime(), endDateT.toLocalTime());
  }

  protected long computeVoidDurationBetween(
      DayPlanning dayPlanning, LocalTime startT, LocalTime endT) {

    LocalTime morningFromTime = dayPlanning.getMorningFrom();
    LocalTime morningToTime = dayPlanning.getMorningTo();
    LocalTime afternoonFromTime = dayPlanning.getAfternoonFrom();
    LocalTime afternoonToTime = dayPlanning.getAfternoonTo();

    long duration = 0;

    if (afternoonFromTime != null && afternoonToTime != null) {
      duration += computeVoidSecondPeriod(dayPlanning, startT, endT);
    }

    if (morningFromTime != null && morningToTime != null) {
      duration += computeVoidFirstPeriod(dayPlanning, startT, endT);
    }

    return duration;
  }

  /**
   * This method has two behaviours: if first period and second period exist within dayPlanning it
   * will only compute the duration between startT and morningFromTime as the in between void
   * duration should be compute in computeVoidSecondPeriod method. If only the first period exist,
   * then this method will compute duration between morningToTime and endT + duration between startT
   * and morningFromTime.
   *
   * @param dayPlanning
   * @param startT
   * @param endT
   * @return duration in seconds
   */
  protected long computeVoidFirstPeriod(DayPlanning dayPlanning, LocalTime startT, LocalTime endT) {
    LocalTime cursorTime = null;

    LocalTime morningFromTime = dayPlanning.getMorningFrom();
    LocalTime morningToTime = dayPlanning.getMorningTo();
    LocalTime afternoonFromTime = dayPlanning.getAfternoonFrom();
    LocalTime afternoonToTime = dayPlanning.getAfternoonTo();

    long duration = 0;

    // This checks if we have a second period.
    if ((afternoonFromTime == null || afternoonToTime == null) && endT.isAfter(morningToTime)) {
      // If second period does not exist, we need to compute the duration
      // between morningToTime and endT
      cursorTime = endT;
      LocalTime maxTime = max(morningToTime, startT);

      duration += DurationTool.getSecondsDuration(Duration.between(maxTime, cursorTime));

      cursorTime = maxTime;

      if (cursorTime.compareTo(startT) == 0) {
        return duration;
      }
    }

    cursorTime = morningFromTime;
    if (cursorTime.isAfter(startT)) {
      duration += DurationTool.getSecondsDuration(Duration.between(startT, cursorTime));
    }

    return duration;
  }

  protected long computeVoidSecondPeriod(
      DayPlanning dayPlanning, LocalTime startT, LocalTime endT) {
    LocalTime cursorTime = endT;

    LocalTime morningToTime = dayPlanning.getMorningTo();
    LocalTime afternoonFromTime = dayPlanning.getAfternoonFrom();
    LocalTime afternoonToTime = dayPlanning.getAfternoonTo();

    long duration = 0;

    if (cursorTime.isAfter(afternoonToTime)) {
      LocalTime maxTime = max(afternoonToTime, startT);
      duration += DurationTool.getSecondsDuration(Duration.between(maxTime, cursorTime));

      cursorTime = maxTime;
    }

    if (cursorTime.compareTo(startT) == 0) {
      return duration;
    }

    cursorTime = afternoonFromTime;

    if (cursorTime.isAfter(startT) && morningToTime == null) {
      duration += DurationTool.getSecondsDuration(Duration.between(startT, cursorTime));
    } else if (cursorTime.isAfter(startT) && endT.isAfter(morningToTime)) {
      LocalTime maxTime = max(morningToTime, startT);
      duration += DurationTool.getSecondsDuration(Duration.between(maxTime, cursorTime));
    }

    return duration;
  }

  protected LocalTime max(LocalTime t1, LocalTime t2) {

    if (t1 == null && t2 == null) return null;
    if (t1 == null) return t2;
    if (t2 == null) return t1;
    return (t1.isAfter(t2)) ? t1 : t2;
  }

  protected LocalTime min(LocalTime t1, LocalTime t2) {

    if (t1 == null && t2 == null) return null;
    if (t1 == null) return t2;
    if (t2 == null) return t1;
    return (t1.isBefore(t2)) ? t1 : t2;
  }

  protected long computeVoidDurationBetween(
      DayPlanning dayPlanning,
      LocalDateTime startDateT,
      LocalDateTime endDateT,
      LocalDateTime initialEndDateT) {

    long duration = 0;

    if (dayPlanning.getWeeklyPlanning() == null || startDateT.compareTo(endDateT) >= 0) {
      return 0;
    }

    if (endDateT.toLocalDate().isAfter(initialEndDateT.toLocalDate())) {
      return 0;
    }

    if (endDateT.toLocalDate().isEqual(initialEndDateT.toLocalDate())) {
      endDateT = initialEndDateT;
    }

    dayPlanning =
        weeklyPlanningService.findDayPlanning(
            dayPlanning.getWeeklyPlanning(), startDateT.toLocalDate());
    duration +=
        computeVoidDurationBetween(dayPlanning, startDateT.toLocalTime(), endDateT.toLocalTime());

    LocalDateTime nextDay = startDateT.plusDays(1).with(LocalTime.MIN);
    return duration
        + computeVoidDurationBetween(
            dayPlanning, nextDay, nextDay.with(LocalTime.MAX), initialEndDateT);
  }
}
