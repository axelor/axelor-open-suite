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
package com.axelor.apps.crm.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.EventReminder;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.RecurrenceConfiguration;
import com.axelor.apps.crm.db.repo.EventReminderRepository;
import com.axelor.apps.crm.db.repo.EventRepository;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.apps.crm.db.repo.RecurrenceConfigurationRepository;
import com.axelor.apps.crm.exception.CrmExceptionMessage;
import com.axelor.apps.crm.service.CalendarService;
import com.axelor.apps.crm.service.EventService;
import com.axelor.apps.crm.service.LeadService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.base.service.ical.ICalendarEventService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.EmailAddress;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.date.DateTool;
import com.axelor.utils.date.DurationTool;
import com.google.common.base.Joiner;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class EventController {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void computeFromStartDateTime(ActionRequest request, ActionResponse response) {

    Event event = request.getContext().asType(Event.class);

    LOG.debug("event : {}", event);

    if (event.getStartDateTime() != null) {
      if (event.getDuration() != null && event.getDuration() != 0) {
        response.setValue(
            "endDateTime", DateTool.plusSeconds(event.getStartDateTime(), event.getDuration()));
      } else if (event.getEndDateTime() != null
          && event.getEndDateTime().isAfter(event.getStartDateTime())) {
        Duration duration =
            DurationTool.computeDuration(event.getStartDateTime(), event.getEndDateTime());
        response.setValue("duration", DurationTool.getSecondsDuration(duration));
      } else {
        Duration duration = Duration.ofHours(1);
        response.setValue("duration", DurationTool.getSecondsDuration(duration));
        response.setValue("endDateTime", event.getStartDateTime().plus(duration));
      }
    }
  }

  public void computeFromEndDateTime(ActionRequest request, ActionResponse response) {

    Event event = request.getContext().asType(Event.class);

    LOG.debug("event : {}", event);

    if (event.getEndDateTime() != null) {
      if (event.getStartDateTime() != null
          && event.getStartDateTime().isBefore(event.getEndDateTime())) {
        Duration duration =
            DurationTool.computeDuration(event.getStartDateTime(), event.getEndDateTime());
        response.setValue("duration", DurationTool.getSecondsDuration(duration));
      } else if (event.getDuration() != null) {
        response.setValue(
            "startDateTime", DateTool.minusSeconds(event.getEndDateTime(), event.getDuration()));
      }
    }
  }

  public void computeFromDuration(ActionRequest request, ActionResponse response) {

    Event event = request.getContext().asType(Event.class);

    LOG.debug("event : {}", event);

    if (event.getDuration() != null) {
      if (event.getStartDateTime() != null) {
        response.setValue(
            "endDateTime", DateTool.plusSeconds(event.getStartDateTime(), event.getDuration()));
      } else if (event.getEndDateTime() != null) {
        response.setValue(
            "startDateTime", DateTool.minusSeconds(event.getEndDateTime(), event.getDuration()));
      }
    }
  }

  public void computeFromCalendar(ActionRequest request, ActionResponse response) {

    Event event = request.getContext().asType(Event.class);

    LOG.debug("event : {}", event);

    if (event.getStartDateTime() != null && event.getEndDateTime() != null) {
      Duration duration =
          DurationTool.computeDuration(event.getStartDateTime(), event.getEndDateTime());
      response.setValue("duration", DurationTool.getSecondsDuration(duration));
    }
  }

  public void saveEventTaskStatusSelect(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Event event = request.getContext().asType(Event.class);
    Event persistEvent = Beans.get(EventRepository.class).find(event.getId());
    persistEvent.setStatusSelect(event.getStatusSelect());
    Beans.get(EventService.class).saveEvent(persistEvent);
  }

  public void saveEventTicketStatusSelect(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Event event = request.getContext().asType(Event.class);
    Event persistEvent = Beans.get(EventRepository.class).find(event.getId());
    persistEvent.setStatusSelect(event.getStatusSelect());
    Beans.get(EventService.class).saveEvent(persistEvent);
  }

  public void viewMap(ActionRequest request, ActionResponse response) {
    try {
      Event event = request.getContext().asType(Event.class);
      if (event.getLocation() != null) {
        Map<String, Object> result = Beans.get(MapService.class).getMap(event.getLocation());
        if (result != null) {
          Map<String, Object> mapView = new HashMap<>();
          mapView.put("title", "Map");
          mapView.put("resource", result.get("url"));
          mapView.put("viewType", "html");
          response.setView(mapView);
        } else
          response.setInfo(
              String.format(I18n.get(BaseExceptionMessage.ADDRESS_5), event.getLocation()));
      } else response.setInfo(I18n.get(CrmExceptionMessage.EVENT_1));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("rawtypes")
  public void assignToMeLead(ActionRequest request, ActionResponse response) {

    try {
      LeadService leadService = Beans.get(LeadService.class);
      LeadRepository leadRepo = Beans.get(LeadRepository.class);

      if (request.getContext().get("id") != null) {
        Lead lead = leadRepo.find((Long) request.getContext().get("id"));
        leadService.assignToMeLead(lead);
      } else if (((List) request.getContext().get("_ids")) != null) {
        for (Lead lead :
            leadRepo.all().filter("id in ?1", request.getContext().get("_ids")).fetch()) {
          lead.setUser(AuthUtils.getUser());
          leadService.assignToMeLead(lead);
        }
      }
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("rawtypes")
  public void assignToMeEvent(ActionRequest request, ActionResponse response) {

    EventRepository eventRepository = Beans.get(EventRepository.class);

    if (request.getContext().get("id") != null) {
      Event event = eventRepository.find((Long) request.getContext().get("id"));
      event.setUser(AuthUtils.getUser());
      Beans.get(EventService.class).saveEvent(event);
    } else if (!((List) request.getContext().get("_ids")).isEmpty()) {
      for (Event event :
          eventRepository.all().filter("id in ?1", request.getContext().get("_ids")).fetch()) {
        event.setUser(AuthUtils.getUser());
        Beans.get(EventService.class).saveEvent(event);
      }
    }
    response.setReload(true);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void generateRecurrentEvents(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {
      Long eventId = (Long) request.getContext().get("id");
      if (eventId == null)
        throw new AxelorException(
            Event.class,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(CrmExceptionMessage.EVENT_SAVED));
      Event event = Beans.get(EventRepository.class).find(eventId);

      RecurrenceConfigurationRepository confRepo =
          Beans.get(RecurrenceConfigurationRepository.class);
      RecurrenceConfiguration conf = event.getRecurrenceConfiguration();
      if (conf != null) {
        conf = confRepo.save(conf);
        Beans.get(EventService.class).generateRecurrentEvents(event, conf);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @Transactional
  public void deleteThis(ActionRequest request, ActionResponse response) {
    Long eventId = new Long(request.getContext().getParent().get("id").toString());
    EventRepository eventRepository = Beans.get(EventRepository.class);
    Event event = eventRepository.find(eventId);
    Event child =
        eventRepository.all().filter("self.parentEvent.id = ?1", event.getId()).fetchOne();
    if (child != null) {
      child.setParentEvent(event.getParentEvent());
    }
    eventRepository.remove(event);
    response.setCanClose(true);
    response.setReload(true);
  }

  @Transactional
  public void deleteNext(ActionRequest request, ActionResponse response) {
    Long eventId = new Long(request.getContext().getParent().get("id").toString());
    EventRepository eventRepository = Beans.get(EventRepository.class);
    Event event = eventRepository.find(eventId);
    Event child =
        eventRepository.all().filter("self.parentEvent.id = ?1", event.getId()).fetchOne();
    while (child != null) {
      child.setParentEvent(null);
      eventRepository.remove(event);
      event = child;
      child = eventRepository.all().filter("self.parentEvent.id = ?1", event.getId()).fetchOne();
    }
    response.setCanClose(true);
    response.setReload(true);
  }

  @Transactional
  public void deleteAll(ActionRequest request, ActionResponse response) {
    Long eventId = new Long(request.getContext().getParent().get("id").toString());
    EventRepository eventRepository = Beans.get(EventRepository.class);
    Event event = eventRepository.find(eventId);
    Event child =
        eventRepository.all().filter("self.parentEvent.id = ?1", event.getId()).fetchOne();
    Event parent = event.getParentEvent();
    while (child != null) {
      child.setParentEvent(null);
      eventRepository.remove(event);
      event = child;
      child = eventRepository.all().filter("self.parentEvent.id = ?1", event.getId()).fetchOne();
    }
    while (parent != null) {
      Event nextParent = parent.getParentEvent();
      eventRepository.remove(parent);
      parent = nextParent;
    }
    response.setCanClose(true);
    response.setReload(true);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void changeAll(ActionRequest request, ActionResponse response) throws AxelorException {
    Long eventId = new Long(request.getContext().getParent().get("id").toString());
    EventRepository eventRepository = Beans.get(EventRepository.class);
    Event event = eventRepository.find(eventId);

    Event child =
        eventRepository.all().filter("self.parentEvent.id = ?1", event.getId()).fetchOne();
    Event parent = event.getParentEvent();
    child.setParentEvent(null);
    Event eventDeleted = child;
    child =
        eventRepository.all().filter("self.parentEvent.id = ?1", eventDeleted.getId()).fetchOne();
    while (child != null) {
      child.setParentEvent(null);
      eventRepository.remove(eventDeleted);
      eventDeleted = child;
      child =
          eventRepository.all().filter("self.parentEvent.id = ?1", eventDeleted.getId()).fetchOne();
    }
    while (parent != null) {
      Event nextParent = parent.getParentEvent();
      eventRepository.remove(parent);
      parent = nextParent;
    }

    RecurrenceConfiguration conf = request.getContext().asType(RecurrenceConfiguration.class);
    RecurrenceConfigurationRepository confRepo = Beans.get(RecurrenceConfigurationRepository.class);
    conf = confRepo.save(conf);
    event.setRecurrenceConfiguration(conf);
    event = eventRepository.save(event);
    if (conf.getRecurrenceType() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(CrmExceptionMessage.RECURRENCE_RECURRENCE_TYPE));
    }

    int recurrenceType = conf.getRecurrenceType();

    if (conf.getPeriodicity() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(CrmExceptionMessage.RECURRENCE_PERIODICITY));
    }

    int periodicity = conf.getPeriodicity();

    if (periodicity < 1) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(CrmExceptionMessage.RECURRENCE_PERIODICITY));
    }

    boolean monday = conf.getMonday();
    boolean tuesday = conf.getTuesday();
    boolean wednesday = conf.getWednesday();
    boolean thursday = conf.getThursday();
    boolean friday = conf.getFriday();
    boolean saturday = conf.getSaturday();
    boolean sunday = conf.getSunday();
    Map<Integer, Boolean> daysMap = new HashMap<Integer, Boolean>();
    Map<Integer, Boolean> daysCheckedMap = new HashMap<Integer, Boolean>();
    if (recurrenceType == 2) {
      daysMap.put(DayOfWeek.MONDAY.getValue(), monday);
      daysMap.put(DayOfWeek.TUESDAY.getValue(), tuesday);
      daysMap.put(DayOfWeek.WEDNESDAY.getValue(), wednesday);
      daysMap.put(DayOfWeek.THURSDAY.getValue(), thursday);
      daysMap.put(DayOfWeek.FRIDAY.getValue(), friday);
      daysMap.put(DayOfWeek.SATURDAY.getValue(), saturday);
      daysMap.put(DayOfWeek.SUNDAY.getValue(), sunday);

      for (Integer day : daysMap.keySet()) {
        if (daysMap.get(day)) {
          daysCheckedMap.put(day, daysMap.get(day));
        }
      }
      if (daysMap.isEmpty()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(CrmExceptionMessage.RECURRENCE_DAYS_CHECKED));
      }
    }

    int monthRepeatType = conf.getMonthRepeatType();

    int endType = conf.getEndType();

    int repetitionsNumber = 0;

    if (endType == 1) {
      if (conf.getRepetitionsNumber() == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(CrmExceptionMessage.RECURRENCE_REPETITION_NUMBER));
      }

      repetitionsNumber = conf.getRepetitionsNumber();

      if (repetitionsNumber < 1) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            CrmExceptionMessage.RECURRENCE_REPETITION_NUMBER);
      }
    }
    LocalDate endDate =
        Beans.get(AppBaseService.class)
            .getTodayDate(
                event.getUser() != null
                    ? event.getUser().getActiveCompany()
                    : Optional.ofNullable(AuthUtils.getUser())
                        .map(User::getActiveCompany)
                        .orElse(null));
    if (endType == 2) {
      if (conf.getEndDate() == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(CrmExceptionMessage.RECURRENCE_END_DATE));
      }

      endDate = conf.getEndDate();

      if (endDate.isBefore(event.getStartDateTime().toLocalDate())
          && endDate.isEqual(event.getStartDateTime().toLocalDate())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(CrmExceptionMessage.RECURRENCE_END_DATE));
      }
    }
    switch (recurrenceType) {
      case 1:
        Beans.get(EventService.class)
            .addRecurrentEventsByDays(event, periodicity, endType, repetitionsNumber, endDate);
        break;

      case 2:
        Beans.get(EventService.class)
            .addRecurrentEventsByWeeks(
                event, periodicity, endType, repetitionsNumber, endDate, daysCheckedMap);
        break;

      case 3:
        Beans.get(EventService.class)
            .addRecurrentEventsByMonths(
                event, periodicity, endType, repetitionsNumber, endDate, monthRepeatType);
        break;

      case 4:
        Beans.get(EventService.class)
            .addRecurrentEventsByYears(event, periodicity, endType, repetitionsNumber, endDate);
        break;

      default:
        break;
    }

    response.setCanClose(true);
    response.setReload(true);
  }

  public void applyChangesToAll(ActionRequest request, ActionResponse response) {
    EventRepository eventRepository = Beans.get(EventRepository.class);
    Event thisEvent =
        eventRepository.find(new Long(request.getContext().get("_idEvent").toString()));
    Event event = eventRepository.find(thisEvent.getId());

    Beans.get(EventService.class).applyChangesToAll(event);
    response.setCanClose(true);
    response.setReload(true);
  }

  public void computeRecurrenceName(ActionRequest request, ActionResponse response) {
    try {
      RecurrenceConfiguration recurrConf =
          request.getContext().asType(RecurrenceConfiguration.class);
      response.setValue(
          "recurrenceName", Beans.get(EventService.class).computeRecurrenceName(recurrConf));
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setCalendarDomain(ActionRequest request, ActionResponse response) {
    User user = AuthUtils.getUser();
    List<Long> calendarIdlist = Beans.get(CalendarService.class).showSharedCalendars(user);
    if (calendarIdlist.isEmpty()) {
      response.setAttr("calendar", "domain", "self.id is null");
    } else {
      response.setAttr(
          "calendar", "domain", "self.id in (" + Joiner.on(",").join(calendarIdlist) + ")");
    }
  }

  public void checkRights(ActionRequest request, ActionResponse response) {
    Event event = request.getContext().asType(Event.class);
    User user = AuthUtils.getUser();
    List<Long> calendarIdlist = Beans.get(CalendarService.class).showSharedCalendars(user);
    if (calendarIdlist.isEmpty() || !calendarIdlist.contains(event.getCalendar().getId())) {
      response.setAttr("meetingGeneralPanel", "readonly", "true");
      response.setAttr("addGuestsPanel", "readonly", "true");
      response.setAttr("meetingAttributesPanel", "readonly", "true");
      response.setAttr("meetingLinkedPanel", "readonly", "true");
    }
  }

  public void changeCreator(ActionRequest request, ActionResponse response) {
    User user = AuthUtils.getUser();
    response.setValue("organizer", Beans.get(CalendarService.class).findOrCreateUser(user));
  }

  /**
   * This method is used to add attendees/guests from partner or contact partner or lead
   *
   * @param request
   * @param response
   */
  public void addGuest(ActionRequest request, ActionResponse response) {
    Event event = request.getContext().asType(Event.class);
    try {
      EmailAddress emailAddress = Beans.get(EventService.class).getEmailAddress(event);
      if (emailAddress != null) {
        response.setValue(
            "attendees", Beans.get(ICalendarEventService.class).addEmailGuest(emailAddress, event));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public void deleteReminder(ActionRequest request, ActionResponse response) {
    try {
      EventReminderRepository eventReminderRepository = Beans.get(EventReminderRepository.class);

      EventReminder eventReminder =
          eventReminderRepository.find((long) request.getContext().get("id"));
      eventReminderRepository.remove(eventReminder);
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void realizeEvent(ActionRequest request, ActionResponse response) {
    try {
      Event event =
          Beans.get(EventRepository.class).find(request.getContext().asType(Event.class).getId());

      Beans.get(EventService.class).realizeEvent(event);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void cancelEvent(ActionRequest request, ActionResponse response) {
    try {
      Event event =
          Beans.get(EventRepository.class).find(request.getContext().asType(Event.class).getId());

      Beans.get(EventService.class).cancelEvent(event);

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void fillEventDates(ActionRequest request, ActionResponse response) {
    try {
      Event event = request.getContext().asType(Event.class);
      Beans.get(EventService.class).fillEventDates(event);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
