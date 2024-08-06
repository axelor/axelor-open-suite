/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.ical.ICalendarException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.base.service.ical.ICalendarEventService;
import com.axelor.inject.Beans;
import com.axelor.message.db.EmailAddress;
import com.axelor.message.db.repo.EmailAddressRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
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
}
