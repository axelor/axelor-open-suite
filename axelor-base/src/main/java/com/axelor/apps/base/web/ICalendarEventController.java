/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.ical.ICalendarException;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.repo.EmailAddressRepository;
import com.axelor.base.service.ical.ICalendarEventService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import javax.mail.MessagingException;

public class ICalendarEventController {

  @Inject ICalendarEventService iCalendarEventService;

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
          response.setValue("attendees", iCalendarEventService.addEmailGuest(emailAddress, event));
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
