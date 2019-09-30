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
package com.axelor.apps.crm.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.crm.db.Event;
import com.axelor.apps.crm.db.EventAttendee;
import com.axelor.apps.crm.db.Lead;

public class EventAttendeeService {

  public EventAttendee createEventAttendee(Event event, Lead lead, Partner contactPartner) {

    EventAttendee eventAttendee = new EventAttendee();
    eventAttendee.setEvent(event);
    eventAttendee.setLead(lead);
    eventAttendee.setContactPartner(contactPartner);

    eventAttendee.setName(this.getName(eventAttendee));

    return eventAttendee;
  }

  public String getName(EventAttendee eventAttendee) {

    if (eventAttendee.getContactPartner() != null) {
      return eventAttendee.getContactPartner().getFullName();
    }
    if (eventAttendee.getLead() != null) {
      return eventAttendee.getLead().getFullName();
    }

    return "";
  }
}
