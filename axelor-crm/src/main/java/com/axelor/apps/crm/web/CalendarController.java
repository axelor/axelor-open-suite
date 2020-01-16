/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.crm.web;

import com.axelor.apps.crm.db.Event;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalendarController {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void showMyEvents(ActionRequest request, ActionResponse response) {
    User user = AuthUtils.getUser();

    response.setView(
        ActionView.define(I18n.get("My Calendar"))
            .model(Event.class.getName())
            .add("calendar", "event-calendar-color-by-calendar")
            .add("grid", "event-grid")
            .add("form", "event-form")
            .context("_typeSelect", 2)
            .domain(
                "self.user.id = :_userId or self.calendar.user.id = :_userId or :_userId IN (SELECT attendee.user FROM self.attendees attendee) or self.organizer.user.id = :_userId")
            .context("_userId", user.getId())
            .map());
  }
}
