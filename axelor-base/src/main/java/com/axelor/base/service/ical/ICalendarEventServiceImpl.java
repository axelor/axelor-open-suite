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
package com.axelor.base.service.ical;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.ICalendarUser;
import com.axelor.apps.base.db.RecurrenceConfiguration;
import com.axelor.apps.base.db.repo.ICalendarEventRepository;
import com.axelor.apps.base.db.repo.RecurrenceConfigurationRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.ical.ICalendarException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningServiceImp;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.db.EntityHelper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.EmailAddress;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.mail.MessagingException;
import org.apache.commons.math3.exception.TooManyIterationsException;

public class ICalendarEventServiceImpl implements ICalendarEventService {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("dd/MM");

  private static final int ITERATION_LIMIT = 1000;

  @Inject private ICalendarEventRepository iCalEventRepo;

  @Inject protected UserRepository userRepository;

  @Inject RecurrenceConfigurationRepository confRepo;

  @Inject WeeklyPlanningServiceImp weeklyPlanningServiceImp;

  @Override
  public List<ICalendarUser> addEmailGuest(EmailAddress email, ICalendarEvent event)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          AxelorException, MessagingException, IOException, ICalendarException, ParseException {
    if (email != null) {
      if (event.getAttendees() == null
          || !event.getAttendees().stream()
              .anyMatch(x -> email.getAddress().equals(x.getEmail()))) {
        ICalendarUser calUser = new ICalendarUser();
        calUser.setEmail(email.getAddress());
        calUser.setName(email.getName());
        if (email.getPartner() != null) {
          calUser.setUser(
              userRepository
                  .all()
                  .filter("self.partner.id = ?1", email.getPartner().getId())
                  .fetchOne());
        }
        event.addAttendee(calUser);
      }
    }
    return event.getAttendees();
  }

  @Override
  public void generateRecurrentEvents(ICalendarEvent icevent, RecurrenceConfiguration conf)
      throws AxelorException {
    Map<Integer, Boolean> daysCheckedMap = checkConfig(conf);
    addRecurrentEvents("generateRecurrentEvents", conf, icevent, daysCheckedMap);
  }

  @Override
  @Transactional
  public void addRecurrentEventsByDays(
      ICalendarEvent icevent,
      int periodicity,
      int endType,
      int repetitionsNumber,
      LocalDate endDate) {
    ICalendarEvent lastEvent = icevent;
    if (endType == RecurrenceConfigurationRepository.END_TYPE_REPET) {
      int repeated = 0;
      while (repeated != repetitionsNumber) {
        ICalendarEvent copy = iCalEventRepo.copy(lastEvent, false);
        copy.setParentEvent(icevent);
        copy.setStartDateTime(copy.getStartDateTime().plusDays(periodicity));
        copy.setEndDateTime(copy.getEndDateTime().plusDays(periodicity));
        lastEvent = iCalEventRepo.save(copy);
        repeated++;
      }
    } else {
      while (lastEvent
          .getStartDateTime()
          .plusDays(periodicity)
          .isBefore(endDate.atStartOfDay().plusDays(1))) {
        ICalendarEvent copy = iCalEventRepo.copy(lastEvent, false);
        copy.setParentEvent(icevent);
        copy.setStartDateTime(copy.getStartDateTime().plusDays(periodicity));
        copy.setEndDateTime(copy.getEndDateTime().plusDays(periodicity));
        lastEvent = iCalEventRepo.save(copy);
      }
    }
  }

  @Override
  @Transactional
  public void addRecurrentEventsByWeeks(
      ICalendarEvent icevent,
      int periodicity,
      int endType,
      int repetitionsNumber,
      LocalDate endDate,
      Map<Integer, Boolean> daysCheckedMap) {

    List<DayOfWeek> dayOfWeekList =
        daysCheckedMap.keySet().stream().sorted().map(DayOfWeek::of).collect(Collectors.toList());
    Duration duration = Duration.between(icevent.getStartDateTime(), icevent.getEndDateTime());
    ICalendarEvent lastEvent = icevent;
    BiFunction<Integer, LocalDateTime, Boolean> breakCondition;

    if (endType == RecurrenceConfigurationRepository.END_TYPE_REPET) {
      breakCondition = (iteration, dateTime) -> iteration >= repetitionsNumber;
    } else {
      breakCondition = (iteration, dateTime) -> dateTime.toLocalDate().isAfter(endDate);
    }

    boolean loop = true;

    for (int iteration = 0; loop; ++iteration) {
      if (iteration > ITERATION_LIMIT) {
        throw new TooManyIterationsException(iteration);
      }

      LocalDateTime nextStartDateTime = lastEvent.getStartDateTime().plusWeeks(periodicity - 1L);

      for (DayOfWeek dayOfWeek : dayOfWeekList) {
        nextStartDateTime = nextStartDateTime.with(TemporalAdjusters.next(dayOfWeek));

        if (Boolean.TRUE.equals(breakCondition.apply(iteration, nextStartDateTime))) {
          loop = false;
          break;
        }

        ICalendarEvent event = iCalEventRepo.copy(lastEvent, false);
        event.setId(null);
        event.setParentEvent(icevent);
        event.setSubject(
            event
                .getSubject()
                .replaceFirst(
                    "(Week-[0-9]*)",
                    String.format(
                        "Week-%d",
                        weeklyPlanningServiceImp.getWeekNo(nextStartDateTime.toLocalDate()))));
        event.setStartDateTime(nextStartDateTime);
        event.setEndDateTime(nextStartDateTime.plus(duration));
        lastEvent = iCalEventRepo.save(event);
      }
    }
  }

  @Override
  @Transactional
  public void addRecurrentEventsByMonths(
      ICalendarEvent icevent,
      int periodicity,
      int endType,
      int repetitionsNumber,
      LocalDate endDate,
      int monthRepeatType) {

    int weekNo = 1 + (icevent.getStartDateTime().getDayOfMonth() - 1) / 7;
    Duration duration = Duration.between(icevent.getStartDateTime(), icevent.getEndDateTime());
    ICalendarEvent lastEvent = icevent;
    BiFunction<Integer, LocalDateTime, Boolean> breakConditionFunc;
    Function<LocalDateTime, LocalDateTime> nextStartDateTimeFunc;
    LocalDateTime nextStartDateTime;

    if (endType == RecurrenceConfigurationRepository.END_TYPE_REPET) {
      breakConditionFunc = (iteration, dateTime) -> iteration >= repetitionsNumber;
    } else {
      breakConditionFunc = (iteration, dateTime) -> dateTime.toLocalDate().isAfter(endDate);
    }

    if (monthRepeatType == RecurrenceConfigurationRepository.REPEAT_TYPE_MONTH) {
      nextStartDateTimeFunc =
          dateTime ->
              dateTime
                  .withDayOfMonth(1)
                  .plusMonths(periodicity)
                  .withDayOfMonth(icevent.getStartDateTime().getDayOfMonth());
    } else {
      nextStartDateTimeFunc =
          dateTime -> {
            LocalDateTime baseNextDateTime = dateTime.withDayOfMonth(1).plusMonths(periodicity);
            dateTime =
                baseNextDateTime.with(
                    TemporalAdjusters.dayOfWeekInMonth(
                        weekNo, icevent.getStartDateTime().getDayOfWeek()));

            if (!dateTime.getMonth().equals(baseNextDateTime.getMonth()) && weekNo > 1) {
              dateTime =
                  baseNextDateTime.with(
                      TemporalAdjusters.dayOfWeekInMonth(
                          weekNo - 1, icevent.getStartDateTime().getDayOfWeek()));
            }

            return dateTime;
          };
    }

    for (int iteration = 0; ; ++iteration) {
      if (iteration > ITERATION_LIMIT) {
        throw new TooManyIterationsException(iteration);
      }

      nextStartDateTime = nextStartDateTimeFunc.apply(lastEvent.getStartDateTime());

      if (Boolean.TRUE.equals(breakConditionFunc.apply(iteration, nextStartDateTime))) {
        break;
      }

      ICalendarEvent copy = iCalEventRepo.copy(lastEvent, false);
      copy.setParentEvent(icevent);
      copy.setStartDateTime(nextStartDateTime);
      copy.setEndDateTime(nextStartDateTime.plus(duration));
      lastEvent = iCalEventRepo.save(copy);
    }
  }

  @Override
  @Transactional
  public void addRecurrentEventsByYears(
      ICalendarEvent icevent,
      int periodicity,
      int endType,
      int repetitionsNumber,
      LocalDate endDate) {
    ICalendarEvent lastEvent = icevent;
    if (endType == RecurrenceConfigurationRepository.END_TYPE_REPET) {
      int repeated = 0;
      while (repeated != repetitionsNumber) {
        ICalendarEvent copy = iCalEventRepo.copy(lastEvent, false);
        copy.setParentEvent(icevent);
        copy.setStartDateTime(copy.getStartDateTime().plusYears(periodicity));
        copy.setEndDateTime(copy.getEndDateTime().plusYears(periodicity));
        lastEvent = iCalEventRepo.save(copy);
        repeated++;
      }
    } else {
      while (lastEvent
          .getStartDateTime()
          .plusYears(periodicity)
          .isBefore(endDate.atStartOfDay().plusYears(1))) {
        ICalendarEvent copy = iCalEventRepo.copy(lastEvent, false);
        copy.setParentEvent(icevent);
        copy.setStartDateTime(copy.getStartDateTime().plusYears(periodicity));
        copy.setEndDateTime(copy.getEndDateTime().plusYears(periodicity));
        lastEvent = iCalEventRepo.save(copy);
      }
    }
  }

  @Override
  public String computeRecurrenceName(RecurrenceConfiguration recurrConf) {
    String recurrName = "";
    switch (recurrConf.getRecurrenceType()) {
      case RecurrenceConfigurationRepository.TYPE_DAY:
        if (recurrConf.getPeriodicity() == 1) {
          recurrName += I18n.get("Every day");
        } else {
          recurrName += String.format(I18n.get("Every %d days"), recurrConf.getPeriodicity());
        }

        if (recurrConf.getEndType() == RecurrenceConfigurationRepository.END_TYPE_REPET) {
          recurrName +=
              String.format(", " + I18n.get("%d times"), recurrConf.getRepetitionsNumber());
        } else if (recurrConf.getEndDate() != null) {
          recurrName +=
              ", " + I18n.get("until the") + " " + recurrConf.getEndDate().format(DATE_FORMAT);
        }
        break;

      case RecurrenceConfigurationRepository.TYPE_WEEK:
        if (recurrConf.getPeriodicity() == 1) {
          recurrName += I18n.get("Every week") + " ";
        } else {
          recurrName +=
              String.format(I18n.get("Every %d weeks") + " ", recurrConf.getPeriodicity());
        }
        if (recurrConf.getMonday()
            && recurrConf.getTuesday()
            && recurrConf.getWednesday()
            && recurrConf.getThursday()
            && recurrConf.getFriday()
            && !recurrConf.getSaturday()
            && !recurrConf.getSunday()) {
          recurrName += I18n.get("every week's day");
        } else if (recurrConf.getMonday()
            && recurrConf.getTuesday()
            && recurrConf.getWednesday()
            && recurrConf.getThursday()
            && recurrConf.getFriday()
            && recurrConf.getSaturday()
            && recurrConf.getSunday()) {
          recurrName += I18n.get("everyday");
        } else {
          recurrName += I18n.get("on") + " ";
          if (Boolean.TRUE.equals(recurrConf.getMonday())) {
            recurrName += I18n.get("mon,");
          }
          if (Boolean.TRUE.equals(recurrConf.getTuesday())) {
            recurrName += I18n.get("tues,");
          }
          if (Boolean.TRUE.equals(recurrConf.getWednesday())) {
            recurrName += I18n.get("wed,");
          }
          if (Boolean.TRUE.equals(recurrConf.getThursday())) {
            recurrName += I18n.get("thur,");
          }
          if (Boolean.TRUE.equals(recurrConf.getFriday())) {
            recurrName += I18n.get("fri,");
          }
          if (Boolean.TRUE.equals(recurrConf.getSaturday())) {
            recurrName += I18n.get("sat,");
          }
          if (Boolean.TRUE.equals(recurrConf.getSunday())) {
            recurrName += I18n.get("sun,");
          }
        }

        if (recurrConf.getEndType() == RecurrenceConfigurationRepository.END_TYPE_REPET) {
          recurrName +=
              String.format(" " + I18n.get("%d times"), recurrConf.getRepetitionsNumber());
        } else if (recurrConf.getEndDate() != null) {
          recurrName +=
              " " + I18n.get("until the") + " " + recurrConf.getEndDate().format(DATE_FORMAT);
        }
        break;

      case RecurrenceConfigurationRepository.TYPE_MONTH:
        if (recurrConf.getPeriodicity() == 1) {
          recurrName +=
              I18n.get("Every month the") + " " + recurrConf.getStartDate().getDayOfMonth();
        } else {
          recurrName +=
              String.format(
                  I18n.get("Every %d months the %d"),
                  recurrConf.getPeriodicity(),
                  recurrConf.getStartDate().getDayOfMonth());
        }

        if (recurrConf.getEndType() == RecurrenceConfigurationRepository.END_TYPE_REPET) {
          recurrName +=
              String.format(", " + I18n.get("%d times"), recurrConf.getRepetitionsNumber());
        } else if (recurrConf.getEndDate() != null) {
          recurrName +=
              ", " + I18n.get("until the") + " " + recurrConf.getEndDate().format(DATE_FORMAT);
        }
        break;

      case RecurrenceConfigurationRepository.TYPE_YEAR:
        if (recurrConf.getPeriodicity() == 1) {
          recurrName += I18n.get("Every year the") + recurrConf.getStartDate().format(MONTH_FORMAT);
        } else {
          recurrName +=
              String.format(
                  I18n.get("Every %d years the %s"),
                  recurrConf.getPeriodicity(),
                  recurrConf.getStartDate().format(MONTH_FORMAT));
        }

        if (recurrConf.getEndType() == RecurrenceConfigurationRepository.END_TYPE_REPET) {
          recurrName +=
              String.format(", " + I18n.get("%d times"), recurrConf.getRepetitionsNumber());
        } else if (recurrConf.getEndDate() != null) {
          recurrName +=
              ", " + I18n.get("until the") + " " + recurrConf.getEndDate().format(DATE_FORMAT);
        }
        break;

      default:
        break;
    }
    return recurrName;
  }

  @Override
  @Transactional
  public void applyChangesToAll(ICalendarEvent event) {
    List<ICalendarEvent> childs =
        iCalEventRepo
            .all()
            .filter("self.parentEvent.id = :eventId")
            .bind("eventId", event.getId())
            .fetch();
    List<ICalendarEvent> parents = new ArrayList<>();
    for (ICalendarEvent child : childs) {
      parents.add(child.getParentEvent());
      child = setCalenderValues(event, child);
      iCalEventRepo.save(child);
    }
    for (ICalendarEvent parent : parents) {
      ICalendarEvent nextParent = parent.getParentEvent();
      parent = setCalenderValues(event, parent);
      iCalEventRepo.save(parent);
      parent = nextParent;
    }
  }

  public ICalendarEvent setCalenderValues(ICalendarEvent event, ICalendarEvent child) {
    child.setSubject(event.getSubject());
    child.setCalendar(event.getCalendar());
    child.setStartDateTime(child.getStartDateTime().withHour(event.getStartDateTime().getHour()));
    child.setStartDateTime(
        child.getStartDateTime().withMinute(event.getStartDateTime().getMinute()));
    child.setEndDateTime(child.getEndDateTime().withHour(event.getEndDateTime().getHour()));
    child.setEndDateTime(child.getEndDateTime().withMinute(event.getEndDateTime().getMinute()));
    child.setUser(event.getUser());
    child.setDisponibilitySelect(event.getDisponibilitySelect());
    child.setVisibilitySelect(event.getVisibilitySelect());
    child.setDescription(event.getDescription());
    child.setTypeSelect(event.getTypeSelect());
    child.setLocation(event.getLocation());
    return child;
  }

  @Override
  public void deleteAll(Long eventId) {
    ICalendarEvent event = iCalEventRepo.find(eventId);
    ICalendarEvent child =
        iCalEventRepo.all().filter("self.parentEvent.id = ?1", event.getId()).fetchOne();
    ICalendarEvent parent = event.getParentEvent();
    while (child != null) {
      child.setParentEvent(null);
      iCalEventRepo.remove(event);
      event = child;
      child = iCalEventRepo.all().filter("self.parentEvent.id = ?1", event.getId()).fetchOne();
    }
    while (parent != null) {
      ICalendarEvent nextParent = parent.getParentEvent();
      iCalEventRepo.remove(parent);
      parent = nextParent;
    }
  }

  @Override
  public void changeAll(Long eventId, RecurrenceConfiguration conf) throws AxelorException {
    ICalendarEvent event = iCalEventRepo.find(eventId);
    ICalendarEvent child =
        iCalEventRepo.all().filter("self.parentEvent.id = ?1", event.getId()).fetchOne();
    ICalendarEvent parent = event.getParentEvent();
    child.setParentEvent(null);
    ICalendarEvent eventDeleted = child;
    child = iCalEventRepo.all().filter("self.parentEvent.id = ?1", eventDeleted.getId()).fetchOne();
    while (child != null) {
      child.setParentEvent(null);
      iCalEventRepo.remove(eventDeleted);
      eventDeleted = child;
      child =
          iCalEventRepo.all().filter("self.parentEvent.id = ?1", eventDeleted.getId()).fetchOne();
    }
    while (parent != null) {
      ICalendarEvent nextParent = parent.getParentEvent();
      iCalEventRepo.remove(parent);
      parent = nextParent;
    }

    conf = confRepo.save(EntityHelper.getEntity(conf));
    event.setRecurrenceConfiguration(conf);
    event = iCalEventRepo.save(event);

    Map<Integer, Boolean> daysCheckedMap = checkConfig(conf);

    addRecurrentEvents("changeAll", conf, event, daysCheckedMap);
  }

  private Map<Integer, Boolean> checkConfig(RecurrenceConfiguration conf) throws AxelorException {
    if (conf.getRecurrenceType() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.RECURRENCE_RECURRENCE_TYPE));
    }
    if (conf.getPeriodicity() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.RECURRENCE_PERIODICITY));
    }
    int periodicity = conf.getPeriodicity();
    if (periodicity < 1) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.RECURRENCE_PERIODICITY));
    }

    boolean monday = conf.getMonday();
    boolean tuesday = conf.getTuesday();
    boolean wednesday = conf.getWednesday();
    boolean thursday = conf.getThursday();
    boolean friday = conf.getFriday();
    boolean saturday = conf.getSaturday();
    boolean sunday = conf.getSunday();
    Map<Integer, Boolean> daysMap = new HashMap<>();
    Map<Integer, Boolean> daysCheckedMap = new HashMap<>();
    if (conf.getRecurrenceType() == RecurrenceConfigurationRepository.TYPE_WEEK) {
      daysMap.put(DayOfWeek.MONDAY.getValue(), monday);
      daysMap.put(DayOfWeek.TUESDAY.getValue(), tuesday);
      daysMap.put(DayOfWeek.WEDNESDAY.getValue(), wednesday);
      daysMap.put(DayOfWeek.THURSDAY.getValue(), thursday);
      daysMap.put(DayOfWeek.FRIDAY.getValue(), friday);
      daysMap.put(DayOfWeek.SATURDAY.getValue(), saturday);
      daysMap.put(DayOfWeek.SUNDAY.getValue(), sunday);

      for (Integer day : daysMap.keySet()) {
        if (Boolean.TRUE.equals(daysMap.get(day))) {
          daysCheckedMap.put(day, daysMap.get(day));
        }
      }
      if (daysMap.isEmpty() || daysCheckedMap.isEmpty()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.RECURRENCE_DAYS_CHECKED));
      }
    }
    return daysCheckedMap;
  }

  public void addRecurrentEvents(
      String methodName,
      RecurrenceConfiguration conf,
      ICalendarEvent event,
      Map<Integer, Boolean> daysCheckedMap)
      throws AxelorException {

    int endType = Integer.parseInt(conf.getEndType().toString());
    int repetitionsNumber = 0;

    if (endType == RecurrenceConfigurationRepository.END_TYPE_REPET) {
      if (conf.getRepetitionsNumber() == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.RECURRENCE_REPETITION_NUMBER));
      }

      repetitionsNumber = Integer.valueOf(conf.getRepetitionsNumber().toString());

      if (repetitionsNumber < 1) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.RECURRENCE_REPETITION_NUMBER));
      }
    }
    LocalDate endDate = getEndDate(methodName, event);

    if (endType == RecurrenceConfigurationRepository.END_TYPE_DATE) {
      if (conf.getEndDate() == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.RECURRENCE_END_DATE));
      }

      endDate = LocalDate.parse(conf.getEndDate().toString(), DateTimeFormatter.ISO_DATE);

      if (endDate.isBefore(event.getStartDateTime().toLocalDate())
          && endDate.isEqual(event.getStartDateTime().toLocalDate())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.RECURRENCE_END_DATE));
      }
    }
    addRecurrence(conf, event, endType, repetitionsNumber, endDate, daysCheckedMap);
  }

  public LocalDate getEndDate(String methodName, ICalendarEvent event) {
    if (methodName.equals("changeAll")) {
      return Beans.get(AppBaseService.class)
          .getTodayDate(
              event.getUser() != null
                  ? event.getUser().getActiveCompany()
                  : Optional.ofNullable(AuthUtils.getUser())
                      .map(User::getActiveCompany)
                      .orElse(null));
    } else if (methodName.equals("generateRecurrentEvents")) {
      return event.getEndDateTime().toLocalDate();
    }
    return null;
  }

  public void addRecurrence(
      RecurrenceConfiguration conf,
      ICalendarEvent event,
      int endType,
      int repetitionsNumber,
      LocalDate endDate,
      Map<Integer, Boolean> daysCheckedMap) {
    int recurrenceType = conf.getRecurrenceType();
    int periodicity = conf.getPeriodicity();
    int monthRepeatType = Integer.parseInt(conf.getMonthRepeatType().toString());
    switch (recurrenceType) {
      case RecurrenceConfigurationRepository.TYPE_DAY:
        addRecurrentEventsByDays(event, periodicity, endType, repetitionsNumber, endDate);
        break;

      case RecurrenceConfigurationRepository.TYPE_WEEK:
        addRecurrentEventsByWeeks(
            event, periodicity, endType, repetitionsNumber, endDate, daysCheckedMap);
        break;

      case RecurrenceConfigurationRepository.TYPE_MONTH:
        addRecurrentEventsByMonths(
            event, periodicity, endType, repetitionsNumber, endDate, monthRepeatType);
        break;

      case RecurrenceConfigurationRepository.TYPE_YEAR:
        addRecurrentEventsByYears(event, periodicity, endType, repetitionsNumber, endDate);
        break;

      default:
        break;
    }
  }

  @Override
  public void deleteNext(Long eventId) {
    ICalendarEvent event = iCalEventRepo.find(eventId);
    ICalendarEvent child =
        iCalEventRepo.all().filter("self.parentEvent.id = ?1", event.getId()).fetchOne();
    while (child != null) {
      child.setParentEvent(null);
      iCalEventRepo.remove(event);
      event = child;
      child = iCalEventRepo.all().filter("self.parentEvent.id = ?1", event.getId()).fetchOne();
    }
  }

  @Override
  public void deleteThis(Long eventId) {
    ICalendarEvent event = iCalEventRepo.find(eventId);
    ICalendarEvent child =
        iCalEventRepo.all().filter("self.parentEvent.id = ?1", event.getId()).fetchOne();
    if (child != null) {
      child.setParentEvent(event.getParentEvent());
    }
    iCalEventRepo.remove(event);
  }

  @Override
  public void deleteAllByParentId(Long eventId) {
    iCalEventRepo.all().filter("self.parentEvent.id = :eventId").bind("eventId", eventId).remove();
  }
}
