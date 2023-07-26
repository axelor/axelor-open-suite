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
import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.db.repo.DayPlanningRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class WeeklyPlanningServiceImp implements WeeklyPlanningService {

  public static final int DEFAULT_SCALE = 2;

  public DayOfWeek getFirstDayOfWeek() {
    WeeklyPlanning planning =
        Beans.get(UserService.class).getUserActiveCompany().getWeeklyPlanning();

    if (planning != null && ObjectUtils.notEmpty(planning.getWeekDays())) {
      Optional<DayPlanning> min =
          planning.getWeekDays().stream().min(Comparator.comparing(DayPlanning::getSequence));

      if (min.isPresent()) {
        return getDayOfWeek(min.get());
      }
    }

    return DayOfWeek.MONDAY;
  }

  protected DayOfWeek getDayOfWeek(DayPlanning day) {
    switch (day.getNameSelect()) {
      case DayPlanningRepository.MONDAY:
        return DayOfWeek.MONDAY;
      case DayPlanningRepository.TUESDAY:
        return DayOfWeek.TUESDAY;
      case DayPlanningRepository.WEDNESDAY:
        return DayOfWeek.WEDNESDAY;
      case DayPlanningRepository.THURSDAY:
        return DayOfWeek.THURSDAY;
      case DayPlanningRepository.FRIDAY:
        return DayOfWeek.FRIDAY;
      case DayPlanningRepository.SATURDAY:
        return DayOfWeek.SATURDAY;
      case DayPlanningRepository.SUNDAY:
        return DayOfWeek.SUNDAY;
      default:
        return DayOfWeek.SUNDAY;
    }
  }

  @Override
  @Transactional
  public WeeklyPlanning initPlanning(WeeklyPlanning planning) {
    String[] dayTab =
        new String[] {
          DayPlanningRepository.MONDAY,
          DayPlanningRepository.TUESDAY,
          DayPlanningRepository.WEDNESDAY,
          DayPlanningRepository.THURSDAY,
          DayPlanningRepository.FRIDAY,
          DayPlanningRepository.SATURDAY,
          DayPlanningRepository.SUNDAY
        };
    for (int i = 0; i < dayTab.length; i++) {
      DayPlanning day = new DayPlanning();
      day.setNameSelect(dayTab[i]);
      planning.addWeekDay(day);
    }
    return planning;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public WeeklyPlanning checkPlanning(WeeklyPlanning planning) throws AxelorException {

    List<DayPlanning> listDay = planning.getWeekDays();
    for (DayPlanning dayPlanning : listDay) {

      if (dayPlanning.getMorningFrom() != null
          && dayPlanning.getMorningTo() != null
          && dayPlanning.getMorningFrom().isAfter(dayPlanning.getMorningTo())) {

        String message =
            messageInCheckPlanning(BaseExceptionMessage.WEEKLY_PLANNING_1, dayPlanning);
        throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, message);
      }

      if (dayPlanning.getMorningTo() != null
          && dayPlanning.getAfternoonFrom() != null
          && dayPlanning.getMorningTo().isAfter(dayPlanning.getAfternoonFrom())) {

        String message =
            messageInCheckPlanning(BaseExceptionMessage.WEEKLY_PLANNING_2, dayPlanning);
        throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, message);
      }

      if (dayPlanning.getAfternoonFrom() != null
          && dayPlanning.getAfternoonTo() != null
          && dayPlanning.getAfternoonFrom().isAfter(dayPlanning.getAfternoonTo())) {

        String message =
            messageInCheckPlanning(BaseExceptionMessage.WEEKLY_PLANNING_3, dayPlanning);
        throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, message);
      }

      if ((dayPlanning.getMorningFrom() == null && dayPlanning.getMorningTo() != null)
          || (dayPlanning.getMorningTo() == null && dayPlanning.getMorningFrom() != null)
          || (dayPlanning.getAfternoonFrom() == null && dayPlanning.getAfternoonTo() != null)
          || (dayPlanning.getAfternoonTo() == null && dayPlanning.getAfternoonFrom() != null)) {

        String message =
            messageInCheckPlanning(BaseExceptionMessage.WEEKLY_PLANNING_4, dayPlanning);
        throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, message);
      }
    }
    return planning;
  }

  @Override
  public double getWorkingDayValueInDays(WeeklyPlanning planning, LocalDate date) {
    double value = 0;
    DayPlanning dayPlanning = findDayPlanning(planning, date);
    if (dayPlanning == null) {
      return value;
    }
    if (dayPlanning.getMorningFrom() != null && dayPlanning.getMorningTo() != null) {
      value += 0.5;
    }
    if (dayPlanning.getAfternoonFrom() != null && dayPlanning.getAfternoonTo() != null) {
      value += 0.5;
    }
    return value;
  }

  @Override
  public double getWorkingDayValueInDaysWithSelect(
      WeeklyPlanning planning, LocalDate date, boolean morning, boolean afternoon) {
    double value = 0;
    DayPlanning dayPlanning = findDayPlanning(planning, date);
    if (dayPlanning == null) {
      return value;
    }
    if (morning && dayPlanning.getMorningFrom() != null && dayPlanning.getMorningTo() != null) {
      value += 0.5;
    }
    if (afternoon
        && dayPlanning.getAfternoonFrom() != null
        && dayPlanning.getAfternoonTo() != null) {
      value += 0.5;
    }
    return value;
  }

  @Override
  public BigDecimal getWorkingDayValueInHours(
      WeeklyPlanning weeklyPlanning, LocalDate date, LocalTime from, LocalTime to) {
    double value = 0;
    DayPlanning dayPlanning = this.findDayPlanning(weeklyPlanning, date);

    if (dayPlanning == null) {
      return BigDecimal.valueOf(value);
    }

    // Compute morning leave duration
    LocalTime morningFrom = dayPlanning.getMorningFrom();
    LocalTime morningTo = dayPlanning.getMorningTo();
    if (morningFrom != null && morningTo != null) {
      LocalTime morningBegin = from != null && from.isAfter(morningFrom) ? from : morningFrom;
      LocalTime morningEnd = to != null && to.isBefore(morningTo) ? to : morningTo;
      if (to != null && to.isBefore(morningBegin)) {
        return BigDecimal.ZERO;
      } else if (from == null || from.isBefore(morningEnd)) {
        value += ChronoUnit.MINUTES.between(morningBegin, morningEnd);
      }
    }

    // Compute afternoon leave duration
    LocalTime afternoonFrom = dayPlanning.getAfternoonFrom();
    LocalTime afternoonTo = dayPlanning.getAfternoonTo();
    if (afternoonFrom != null && afternoonTo != null) {
      LocalTime afternoonBegin = from != null && from.isAfter(afternoonFrom) ? from : afternoonFrom;
      LocalTime afternoonEnd = to != null && to.isBefore(afternoonTo) ? to : afternoonTo;
      if (from != null && from.isAfter(afternoonEnd)) {
        return BigDecimal.ZERO;
      } else if (to == null || to.isAfter(afternoonBegin)) {
        value += ChronoUnit.MINUTES.between(afternoonBegin, afternoonEnd);
      }
    }

    return BigDecimal.valueOf(value)
        .divide(BigDecimal.valueOf(60), DEFAULT_SCALE, BigDecimal.ROUND_HALF_UP);
  }

  public DayPlanning findDayPlanning(WeeklyPlanning planning, LocalDate date) {
    int dayOfWeek = date.getDayOfWeek().getValue();
    switch (dayOfWeek) {
      case 1:
        return findDayWithName(planning, DayPlanningRepository.MONDAY);

      case 2:
        return findDayWithName(planning, DayPlanningRepository.TUESDAY);

      case 3:
        return findDayWithName(planning, DayPlanningRepository.WEDNESDAY);

      case 4:
        return findDayWithName(planning, DayPlanningRepository.THURSDAY);

      case 5:
        return findDayWithName(planning, DayPlanningRepository.FRIDAY);

      case 6:
        return findDayWithName(planning, DayPlanningRepository.SATURDAY);

      case 7:
        return findDayWithName(planning, DayPlanningRepository.SUNDAY);

      default:
        return findDayWithName(planning, "null");
    }
  }

  public DayPlanning findDayWithName(WeeklyPlanning planning, String name) {
    List<DayPlanning> dayPlanningList = planning.getWeekDays();
    for (DayPlanning dayPlanning : dayPlanningList) {
      if (dayPlanning.getNameSelect().equals(name)) {
        return dayPlanning;
      }
    }
    return null;
  }

  public String messageInCheckPlanning(String message, DayPlanning dayPlanning) {
    String dayPlanningName = dayPlanning.getNameSelect();
    return String.format(
        I18n.get(message),
        I18n.get(Character.toUpperCase(dayPlanningName.charAt(0)) + dayPlanningName.substring(1))
            .toLowerCase()); // Because day of week are traduced with a upperCase at the first
    // letter
  }
}
