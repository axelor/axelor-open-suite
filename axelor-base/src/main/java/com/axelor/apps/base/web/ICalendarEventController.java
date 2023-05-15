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
package com.axelor.apps.base.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.callable.ControllerCallableTool;
import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.RecurrenceConfiguration;
import com.axelor.apps.base.db.repo.ICalendarEventRepository;
import com.axelor.apps.base.db.repo.RecurrenceConfigurationRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.ical.ICalendarException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.base.service.ical.ICalendarEventService;
import com.axelor.base.service.ical.ICalendarGenerateEventService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.EmailAddress;
import com.axelor.message.db.repo.EmailAddressRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import javax.mail.MessagingException;

public class ICalendarEventController {

  @SuppressWarnings("unchecked")
  public void addEmailGuest(ActionRequest request, ActionResponse response)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          AxelorException, MessagingException, IOException, ICalendarException, ParseException {
    ICalendarEvent event = request.getContext().asType(ICalendarEvent.class);
    try {
      Map<String, Object> guestEmail = (Map<String, Object>) request.getContext().get("guestEmail");
      if (guestEmail != null) {
        EmailAddress emailAddress =
            Beans.get(EmailAddressRepository.class)
                .find(new Long((guestEmail.get("id").toString())));
        if (emailAddress != null) {
          response.setValue(
              "attendees",
              Beans.get(ICalendarEventService.class).addEmailGuest(emailAddress, event));
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public void generateRecurrentEvents(ActionRequest request, ActionResponse response)
      throws AxelorException {
    try {
      Long eventId = (Long) request.getContext().get("id");
      if (eventId == null)
        throw new AxelorException(
            ICalendarEvent.class,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(BaseExceptionMessage.EVENT_SAVED));
      ICalendarEvent event = Beans.get(ICalendarEventRepository.class).find(eventId);

      RecurrenceConfigurationRepository confRepo =
          Beans.get(RecurrenceConfigurationRepository.class);
      RecurrenceConfiguration conf = event.getRecurrenceConfiguration();
      if (conf != null) {
        conf = confRepo.save(conf);
        ICalendarGenerateEventService eventService = Beans.get(ICalendarGenerateEventService.class);
        eventService.setICalendarEvent(event, conf);
        ControllerCallableTool<ICalendarEvent> eventControllerCallableTool =
            new ControllerCallableTool<>();
        eventControllerCallableTool.runInSeparateThread(eventService, response);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeRecurrenceName(ActionRequest request, ActionResponse response) {
    RecurrenceConfiguration recurrConf = request.getContext().asType(RecurrenceConfiguration.class);
    response.setValue(
        "recurrenceName", Beans.get(ICalendarEventService.class).computeRecurrenceName(recurrConf));
  }

  @Transactional
  public void deleteThis(ActionRequest request, ActionResponse response) {
    Beans.get(ICalendarEventService.class)
        .deleteThis(Long.valueOf(request.getContext().getParent().get("id").toString()));
    response.setCanClose(true);
    response.setReload(true);
  }

  @Transactional
  public void deleteNext(ActionRequest request, ActionResponse response) {
    Beans.get(ICalendarEventService.class)
        .deleteNext(Long.valueOf(request.getContext().getParent().get("id").toString()));
    response.setCanClose(true);
    response.setReload(true);
  }

  @Transactional
  public void deleteAll(ActionRequest request, ActionResponse response) {
    Beans.get(ICalendarEventService.class)
        .deleteAll(Long.valueOf(request.getContext().getParent().get("id").toString()));
    response.setCanClose(true);
    response.setReload(true);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void changeAll(ActionRequest request, ActionResponse response) throws AxelorException {
    RecurrenceConfiguration config = request.getContext().asType(RecurrenceConfiguration.class);
    Beans.get(ICalendarEventService.class)
        .changeAll(Long.valueOf(request.getContext().getParent().get("id").toString()), config);
    response.setCanClose(true);
    response.setReload(true);
  }

  public void applyChangesToAll(ActionRequest request, ActionResponse response) {
    ICalendarEventRepository eventRepository = Beans.get(ICalendarEventRepository.class);
    ICalendarEvent thisEvent =
        eventRepository.find(Long.valueOf(request.getContext().get("_idEvent").toString()));
    ICalendarEvent event = eventRepository.find(thisEvent.getId());
    Beans.get(ICalendarEventService.class).applyChangesToAll(event);
    response.setCanClose(true);
    response.setReload(true);
  }
}
