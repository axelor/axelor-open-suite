/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.base.service.ical;

import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.ICalendarUser;
import com.axelor.apps.base.ical.ICalendarException;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import javax.mail.MessagingException;

public class ICalendarEventServiceImpl implements ICalendarEventService {

  @Inject protected UserRepository userRepository;

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
}
