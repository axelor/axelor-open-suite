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

import com.axelor.apps.base.db.DayPlanning;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

public enum Operation {
  ADD(
      Long::sum,
      LocalDateTime::plusSeconds,
      dateTime -> dateTime.plusDays(1).toLocalDate().atStartOfDay(),
      (dayPlanning, dateTime) -> {
        if (dayPlanning.getMorningFrom() != null
            && dateTime.toLocalTime().compareTo(dayPlanning.getMorningFrom()) < 0) {
          return new DayPlanningPeriod(dayPlanning.getMorningFrom(), dayPlanning.getMorningTo());
        } else if (dayPlanning.getAfternoonFrom() != null
            && dateTime.toLocalTime().compareTo(dayPlanning.getAfternoonFrom()) < 0) {
          return new DayPlanningPeriod(
              dayPlanning.getAfternoonFrom(), dayPlanning.getAfternoonTo());
        }
        if (dayPlanning.getMorningTo() != null
            && dayPlanning.getAfternoonFrom() != null
            && dateTime.toLocalTime().compareTo(dayPlanning.getMorningTo()) > 0
            && dateTime.toLocalTime().compareTo(dayPlanning.getAfternoonFrom()) < 0) {
          return new DayPlanningPeriod(
              dayPlanning.getAfternoonFrom(), dayPlanning.getAfternoonTo());
        }
        return null;
      },
      (period, dateTime) -> dateTime.with(period.getStart()),
      (period, dateTime) -> dateTime.with(period.getEnd()),
      (period, dateTime) -> dateTime.toLocalTime().isBefore(period.getStart())),
  SUB(
      (fromSeconds, seconds) -> fromSeconds - seconds,
      LocalDateTime::minusSeconds,
      dateTime -> dateTime.minusDays(1).toLocalDate().atTime(LocalTime.MAX),
      (dayPlanning, dateTime) -> {
        if (dayPlanning.getAfternoonTo() != null
            && dateTime.toLocalTime().compareTo(dayPlanning.getAfternoonTo()) > 0) {
          return new DayPlanningPeriod(
              dayPlanning.getAfternoonFrom(), dayPlanning.getAfternoonTo());
        } else if (dayPlanning.getMorningTo() != null
            && dateTime.toLocalTime().compareTo(dayPlanning.getMorningTo()) > 0) {
          return new DayPlanningPeriod(dayPlanning.getMorningFrom(), dayPlanning.getMorningTo());
        }
        if (dayPlanning.getMorningTo() != null
            && dayPlanning.getAfternoonFrom() != null
            && dateTime.toLocalTime().compareTo(dayPlanning.getMorningTo()) > 0
            && dateTime.toLocalTime().compareTo(dayPlanning.getAfternoonFrom()) < 0) {
          return new DayPlanningPeriod(dayPlanning.getMorningFrom(), dayPlanning.getMorningTo());
        }
        return null;
      },
      (period, dateTime) -> dateTime.with(period.getEnd()),
      (period, dateTime) -> dateTime.with(period.getStart()),
      (period, dateTime) -> dateTime.toLocalTime().isAfter(period.getEnd()));

  private final BinaryOperator<Long> secondsOp;
  private final BiFunction<LocalDateTime, Long, LocalDateTime> dateTimeOp;
  private final UnaryOperator<LocalDateTime> nextDayDateTimeOp;
  private final BiFunction<DayPlanning, LocalDateTime, DayPlanningPeriod> nearestPeriodOp;
  private final BiFunction<DayPlanningPeriod, LocalDateTime, LocalDateTime> goToPeriodStartOp;
  private final BiFunction<DayPlanningPeriod, LocalDateTime, LocalDateTime> goToPeriodEndOp;
  private final BiPredicate<DayPlanningPeriod, LocalDateTime> isBeforeOp;

  Operation(
      BinaryOperator<Long> secondsOp,
      BiFunction<LocalDateTime, Long, LocalDateTime> dateTimeOp,
      UnaryOperator<LocalDateTime> nextDayDateTimeOp,
      BiFunction<DayPlanning, LocalDateTime, DayPlanningPeriod> nearestPeriodOp,
      BiFunction<DayPlanningPeriod, LocalDateTime, LocalDateTime> goToPeriodStartOp,
      BiFunction<DayPlanningPeriod, LocalDateTime, LocalDateTime> goToPeriodEndOp,
      BiPredicate<DayPlanningPeriod, LocalDateTime> isBeforePeriodOp) {
    this.secondsOp = secondsOp;
    this.dateTimeOp = dateTimeOp;
    this.nextDayDateTimeOp = nextDayDateTimeOp;
    this.nearestPeriodOp = nearestPeriodOp;
    this.goToPeriodStartOp = goToPeriodStartOp;
    this.goToPeriodEndOp = goToPeriodEndOp;
    this.isBeforeOp = isBeforePeriodOp;
  }

  public Long compute(Long fromSeconds, Long seconds) {
    return secondsOp.apply(fromSeconds, seconds);
  }

  public LocalDateTime compute(LocalDateTime dateTime, Long seconds) {
    return dateTimeOp.apply(dateTime, seconds);
  }

  public LocalDateTime goToPeriodStart(DayPlanningPeriod period, LocalDateTime dateTime) {
    return goToPeriodStartOp.apply(period, dateTime);
  }

  public LocalDateTime goToPeriodEnd(DayPlanningPeriod period, LocalDateTime dateTime) {
    return goToPeriodEndOp.apply(period, dateTime);
  }

  public LocalDateTime getNextDayDateTime(LocalDateTime dateTime) {
    return nextDayDateTimeOp.apply(dateTime);
  }

  public DayPlanningPeriod getNearestPeriod(DayPlanning dayPlanning, LocalDateTime dateTime) {
    return nearestPeriodOp.apply(dayPlanning, dateTime);
  }

  public boolean isBefore(DayPlanningPeriod period, LocalDateTime dateTime) {
    return isBeforeOp.test(period, dateTime);
  }
}
