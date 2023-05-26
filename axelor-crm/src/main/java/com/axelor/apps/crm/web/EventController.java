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
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.EventReminder;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.repo.EventReminderRepository;
import com.axelor.apps.crm.db.repo.EventRepository;
import com.axelor.apps.crm.db.repo.LeadRepository;
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
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
