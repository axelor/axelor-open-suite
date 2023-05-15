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
import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.RecurrenceConfiguration;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.db.repo.ICalendarEventRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.db.repo.WeeklyPlanningRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.db.EntityHelper;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class WeeklyPlanningServiceImp implements WeeklyPlanningService {

  @Inject WeeklyPlanningRepository weeklyPlanningRepo;

  public static final int END_TYPE = 2;
  public static final int PERIODICITY = 1;
  public static final int DEFAULT_SCALE = 2;
  public static final int RECURRENCE_TYPE = 2;
  public static final String MORNING = "morning";
  public static final String AFTERNOON = "afternoon";
  public static final int YEARS_TO_ADD = 1;

  public DayOfWeek getDayOfWeek(String day) {
    switch (day) {
      case ICalendarEventRepository.MONDAY:
        return DayOfWeek.MONDAY;
      case ICalendarEventRepository.TUESDAY:
        return DayOfWeek.TUESDAY;
      case ICalendarEventRepository.WEDNESDAY:
        return DayOfWeek.WEDNESDAY;
      case ICalendarEventRepository.THURSDAY:
        return DayOfWeek.THURSDAY;
      case ICalendarEventRepository.FRIDAY:
        return DayOfWeek.FRIDAY;
      case ICalendarEventRepository.SATURDAY:
        return DayOfWeek.SATURDAY;
      case ICalendarEventRepository.SUNDAY:
        return DayOfWeek.SUNDAY;
      default:
        return DayOfWeek.SUNDAY;
    }
  }

  @Override
  @Transactional
  public WeeklyPlanning initPlanning(WeeklyPlanning planning) {
    List<String> dayTab = getAllDays();
    LocalDateTime time = LocalDateTime.now().with(DayOfWeek.MONDAY).truncatedTo(ChronoUnit.DAYS);
    for (int i = 0; i < dayTab.size(); i++) {
      ICalendarEvent day = new ICalendarEvent();
      day.setSubject(dayTab.get(i));
      day.setStartDateTime(time);
      day.setEndDateTime(time);
      day.setVisibilitySelect(ICalendarEventRepository.VISIBILITY_PUBLIC);
      day.setDisponibilitySelect(ICalendarEventRepository.DISPONIBILITY_BUSY);
      day.setSequence(i);
      planning.addWeekDay(day);
      if (i % 2 != 0) time = time.plusDays(1);
    }
    return planning;
  }

  public List<String> getAllDays() {
    return Arrays.asList(
        ICalendarEventRepository.MONDAY_MORNING,
        ICalendarEventRepository.MONDAY_AFTERNOON,
        ICalendarEventRepository.TUESDAY_MORNING,
        ICalendarEventRepository.TUESDAY_AFTERNOON,
        ICalendarEventRepository.WEDNESDAY_MORNING,
        ICalendarEventRepository.WEDNESDAY_AFTERNOON,
        ICalendarEventRepository.THURSDAY_MORNING,
        ICalendarEventRepository.THURSDAY_AFTERNOON,
        ICalendarEventRepository.FRIDAY_MORNING,
        ICalendarEventRepository.FRIDAY_AFTERNOON,
        ICalendarEventRepository.SATURDAY_MORNING,
        ICalendarEventRepository.SATURDAY_AFTERNOON,
        ICalendarEventRepository.SUNDAY_MORNING,
        ICalendarEventRepository.SUNDAY_AFTERNOON);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void checkPlanning(WeeklyPlanning planning) throws AxelorException {

    List<ICalendarEvent> listDay = planning.getWeekDays();
    for (ICalendarEvent dayPlanning : listDay) {
      List<ICalendarEvent> dayList = getDayList(listDay, dayPlanning);
      ICalendarEvent morningEvent = new ICalendarEvent();
      ICalendarEvent afternoonEvent = new ICalendarEvent();
      for (ICalendarEvent icaleventPlanning : dayList) {
        LocalTime startTime = dayPlanning.getStartDateTime().toLocalTime();
        LocalTime endTime = dayPlanning.getEndDateTime().toLocalTime();
        if (icaleventPlanning.getSubject().endsWith(MORNING)) {
          morningEvent = icaleventPlanning;
          if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            String message =
                messageInCheckPlanning(
                    BaseExceptionMessage.WEEKLY_PLANNING_1, icaleventPlanning, null);
            throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, message);
          }
        }
        if (icaleventPlanning.getSubject().endsWith(AFTERNOON)) {
          afternoonEvent = icaleventPlanning;
          if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            String message =
                messageInCheckPlanning(
                    BaseExceptionMessage.WEEKLY_PLANNING_3, icaleventPlanning, null);
            throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, message);
          }
        }
      }

      if (validateTime(morningEvent.getEndDateTime())
          && validateTime(afternoonEvent.getStartDateTime())
          && morningEvent.getEndDateTime().isAfter(afternoonEvent.getStartDateTime())) {

        String message =
            messageInCheckPlanning(
                BaseExceptionMessage.WEEKLY_PLANNING_2, morningEvent, afternoonEvent);
        throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, message);
      }

      if (!validateTime(morningEvent.getStartDateTime())
              && validateTime(morningEvent.getEndDateTime())
          || !validateTime(morningEvent.getEndDateTime())
              && validateTime(morningEvent.getStartDateTime())
          || !validateTime(afternoonEvent.getStartDateTime())
              && validateTime(afternoonEvent.getEndDateTime())
          || !validateTime(afternoonEvent.getEndDateTime())
              && validateTime(afternoonEvent.getStartDateTime())) {

        String message =
            messageInCheckPlanning(
                BaseExceptionMessage.WEEKLY_PLANNING_4, morningEvent, afternoonEvent);
        throw new AxelorException(TraceBackRepository.CATEGORY_INCONSISTENCY, message);
      }
    }
  }

  @Override
  public boolean validateTime(LocalDateTime time) {
    return (time.toLocalTime() != null && time.toLocalTime() != LocalTime.of(0, 0));
  }

  public List<ICalendarEvent> getDayList(List<ICalendarEvent> listDay, ICalendarEvent dayPlanning) {
    return listDay.stream()
        .filter(t -> t.getSubject().contains(dayPlanning.getSubject().split("\\s")[0]))
        .collect(Collectors.toList());
  }

  public String messageInCheckPlanning(
      String message, ICalendarEvent morningPlanning, ICalendarEvent afternoonPlanning) {
    String dayPlanningName = "";
    dayPlanningName =
        (afternoonPlanning != null)
            ? String.format(
                "%s AND %s", morningPlanning.getSubject(), afternoonPlanning.getSubject())
            : String.format("%s", morningPlanning.getSubject());
    return String.format(
        I18n.get(message),
        I18n.get(Character.toUpperCase(dayPlanningName.charAt(0)) + dayPlanningName.substring(1))
            .toLowerCase()); // Because day of week are traduced with a upperCase at the firs letter
  }

  @Override
  public double getWorkingDayValueInDays(WeeklyPlanning planning, LocalDate date) {
    double value = 0;
    List<ICalendarEvent> dayPlanningList = findDayPlanning(planning, date);
    if (dayPlanningList == null || dayPlanningList.isEmpty()) {
      return value;
    }
    LocalDateTime morningStartDateTime = null;
    LocalDateTime morningEndDateTime = null;
    LocalDateTime afternoonStartDateTime = null;
    LocalDateTime afternoonEndDateTime = null;

    for (ICalendarEvent dayPlanning : dayPlanningList) {
      if (dayPlanning.getSubject().endsWith(MORNING)) {
        morningStartDateTime = dayPlanning.getStartDateTime();
        morningEndDateTime = dayPlanning.getEndDateTime();
      }
      if (dayPlanning.getSubject().endsWith(AFTERNOON)) {
        afternoonStartDateTime = dayPlanning.getStartDateTime();
        afternoonEndDateTime = dayPlanning.getEndDateTime();
      }
    }

    if (validateTime(morningStartDateTime) && validateTime(morningEndDateTime)) {
      value += 0.5;
    }
    if (validateTime(afternoonStartDateTime) && validateTime(afternoonEndDateTime)) {
      value += 0.5;
    }
    return value;
  }

  @Override
  public double getWorkingDayValueInDaysWithSelect(
      WeeklyPlanning planning, LocalDate date, boolean morning, boolean afternoon) {
    double value = 0;
    LocalDateTime morningStartDateTime = null;
    LocalDateTime morningEndDateTime = null;
    LocalDateTime afternoonStartDateTime = null;
    LocalDateTime afternoonEndDateTime = null;
    List<ICalendarEvent> weekPlanningList = findDayPlanning(planning, date);

    if (weekPlanningList == null || weekPlanningList.isEmpty()) {
      return value;
    }
    for (ICalendarEvent dayPlanning : weekPlanningList) {
      if (dayPlanning.getSubject().endsWith(MORNING)) {
        morningStartDateTime = dayPlanning.getStartDateTime();
        morningEndDateTime = dayPlanning.getEndDateTime();
      }
      if (dayPlanning.getSubject().endsWith(AFTERNOON)) {
        afternoonStartDateTime = dayPlanning.getStartDateTime();
        afternoonEndDateTime = dayPlanning.getEndDateTime();
      }
    }

    if (morning && morningStartDateTime != null && morningEndDateTime != null) {
      value += 0.5;
    }
    if (afternoon && afternoonStartDateTime != null && afternoonEndDateTime != null) {
      value += 0.5;
    }
    return value;
  }

  @Override
  public BigDecimal getWorkingDayValueInHours(
      WeeklyPlanning weeklyPlanning, LocalDate date, LocalTime from, LocalTime to) {
    double value = 0;
    List<ICalendarEvent> dayPlanningList = this.findDayPlanning(weeklyPlanning, date);

    if (dayPlanningList == null || dayPlanningList.isEmpty()) {
      return BigDecimal.valueOf(value);
    }

    for (ICalendarEvent dayplanning : dayPlanningList) {
      // Compute morning leave duration
      if (dayplanning.getSubject().endsWith(MORNING)) {
        LocalTime morningFrom = dayplanning.getStartDateTime().toLocalTime();
        LocalTime morningTo = dayplanning.getEndDateTime().toLocalTime();
        if (morningFrom != null && morningTo != null) {
          LocalTime morningBegin = from != null && from.isAfter(morningFrom) ? from : morningFrom;
          LocalTime morningEnd = to != null && to.isBefore(morningTo) ? to : morningTo;
          if (to != null && to.isBefore(morningBegin)) {
            return BigDecimal.ZERO;
          } else if (from == null || from.isBefore(morningEnd)) {
            value += ChronoUnit.MINUTES.between(morningBegin, morningEnd);
          }
        }
      }

      // Compute afternoon leave duration
      if (dayplanning.getSubject().endsWith(AFTERNOON)) {
        LocalTime afternoonFrom = dayplanning.getStartDateTime().toLocalTime();
        LocalTime afternoonTo = dayplanning.getEndDateTime().toLocalTime();
        if (afternoonFrom != null && afternoonTo != null) {
          LocalTime afternoonBegin =
              from != null && from.isAfter(afternoonFrom) ? from : afternoonFrom;
          LocalTime afternoonEnd = to != null && to.isBefore(afternoonTo) ? to : afternoonTo;
          if (from != null && from.isAfter(afternoonEnd)) {
            return BigDecimal.ZERO;
          } else if (to == null || to.isAfter(afternoonBegin)) {
            value += ChronoUnit.MINUTES.between(afternoonBegin, afternoonEnd);
          }
        }
      }
    }

    return BigDecimal.valueOf(value)
        .divide(BigDecimal.valueOf(60), DEFAULT_SCALE, RoundingMode.HALF_UP);
  }

  public List<ICalendarEvent> findDayPlanning(WeeklyPlanning planning, LocalDate date) {
    int dayOfWeek = date.getDayOfWeek().getValue();
    switch (dayOfWeek) {
      case 1:
        return findDayWithName(planning, ICalendarEventRepository.MONDAY);

      case 2:
        return findDayWithName(planning, ICalendarEventRepository.TUESDAY);

      case 3:
        return findDayWithName(planning, ICalendarEventRepository.WEDNESDAY);

      case 4:
        return findDayWithName(planning, ICalendarEventRepository.THURSDAY);

      case 5:
        return findDayWithName(planning, ICalendarEventRepository.FRIDAY);

      case 6:
        return findDayWithName(planning, ICalendarEventRepository.SATURDAY);

      case 7:
        return findDayWithName(planning, ICalendarEventRepository.SUNDAY);

      default:
        return findDayWithName(planning, "null");
    }
  }

  public List<ICalendarEvent> findDayWithName(WeeklyPlanning planning, String name) {
    List<ICalendarEvent> dayList = new ArrayList<>();
    for (ICalendarEvent dayPlanning : planning.getWeekDays()) {
      if (dayPlanning.getSubject().contains(name)) {
        dayList.add(dayPlanning);
      }
    }
    return dayList.stream()
        .filter(Objects::nonNull)
        .filter(plan -> plan.getParentEvent() == null)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public WeeklyPlanning setICalEventValues(WeeklyPlanning planning) throws AxelorException {
    planning = EntityHelper.getEntity(planning);
    List<ICalendarEvent> weekDays = planning.getWeekDays();
    for (ICalendarEvent iCalendarEvent : weekDays) {
      if (iCalendarEvent.getRecurrenceConfiguration() == null
          || iCalendarEvent.getStartDateTime().toLocalDate()
              != iCalendarEvent.getRecurrenceConfiguration().getStartDate()) {
        RecurrenceConfiguration config = setConfigDayValue(iCalendarEvent);
        iCalendarEvent.setRecurrenceConfiguration(config);
      }
    }
    return weeklyPlanningRepo.save(planning);
  }

  public RecurrenceConfiguration setConfigDayValue(ICalendarEvent iCalendarEvent) {
    RecurrenceConfiguration config = iCalendarEvent.getRecurrenceConfiguration();
    if (config == null) {
      config = new RecurrenceConfiguration();
      config.setRecurrenceType(RECURRENCE_TYPE);
      config.setPeriodicity(PERIODICITY);
      config.setEndType(END_TYPE);
      config.setEndDate(LocalDate.now().plusYears(YEARS_TO_ADD));
    }
    LocalDate startDate = iCalendarEvent.getStartDateTime().toLocalDate();
    config.setStartDate(startDate);
    config = getDayName(config, startDate.getDayOfWeek().name().toLowerCase());
    return config;
  }

  public RecurrenceConfiguration getDayName(RecurrenceConfiguration config, String dayName) {
    switch (dayName) {
      case ICalendarEventRepository.MONDAY:
        config.setMonday(true);
        break;
      case ICalendarEventRepository.TUESDAY:
        config.setTuesday(true);
        break;
      case ICalendarEventRepository.WEDNESDAY:
        config.setWednesday(true);
        break;
      case ICalendarEventRepository.THURSDAY:
        config.setThursday(true);
        break;
      case ICalendarEventRepository.FRIDAY:
        config.setFriday(true);
        break;
      case ICalendarEventRepository.SATURDAY:
        config.setSaturday(true);
        break;
      case ICalendarEventRepository.SUNDAY:
        config.setSunday(true);
        break;
    }
    return config;
  }

  @Override
  public ICalendarEvent setDateTimeValues(ICalendarEvent event, String start, String end) {
    if (start != null) {
      LocalTime startTime = LocalTime.parse(start);
      if (startTime != null)
        event.setStartDateTime(LocalDateTime.of(event.getStartDateTime().toLocalDate(), startTime));
    }
    if (end != null) {
      LocalTime endTime = LocalTime.parse(end);
      if (endTime != null)
        event.setEndDateTime(LocalDateTime.of(event.getEndDateTime().toLocalDate(), endTime));
    }
    return event;
  }

  @Override
  public int getWeekNo(LocalDate date) {
    WeekFields of = WeekFields.of(Locale.getDefault());
    TemporalField weekNum = of.weekOfWeekBasedYear();
    return Integer.parseInt(String.format("%02d", date.get(weekNum)));
  }
}
