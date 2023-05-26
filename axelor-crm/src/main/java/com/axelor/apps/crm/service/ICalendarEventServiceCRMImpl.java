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
package com.axelor.apps.crm.service;

import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.crm.db.Event;
import com.axelor.base.service.ical.ICalendarEventServiceImpl;

public class ICalendarEventServiceCRMImpl extends ICalendarEventServiceImpl {

  @Override
  public ICalendarEvent setCalenderValues(ICalendarEvent event, ICalendarEvent child) {
    if (event != null && event.getClass().equals(ICalendarEvent.class)) {
      return child;
    }
    Event crmEvent = (Event) event;
    Event crmChild = (Event) child;
    crmChild.setTeam(crmEvent.getTeam());
    crmChild.setPartner(crmEvent.getPartner());
    crmChild.setContactPartner(crmEvent.getContactPartner());
    crmChild.setEventLead(crmEvent.getEventLead());
    return crmChild;
  }
}
