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
package com.axelor.apps.helpdesk.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.helpdesk.db.Ticket;
import com.axelor.apps.helpdesk.db.TicketStatus;
import com.axelor.apps.helpdesk.service.TicketService;
import com.axelor.apps.helpdesk.service.TicketStatusService;
import com.axelor.apps.helpdesk.service.app.AppHelpdeskService;
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppHelpdesk;
import com.google.inject.Inject;
import java.util.Map;
import java.util.Optional;

public class TicketManagementRepository extends TicketRepository {
  @Inject private AppBaseService appBaseService;

  @Override
  public Ticket save(Ticket ticket) {

    TicketService ticketService = Beans.get(TicketService.class);
    try {
      ticketService.computeSeq(ticket);
    } catch (AxelorException e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
    }
    ticketService.computeSLA(ticket);
    ticketService.checkSLAcompleted(ticket);
    return super.save(ticket);
  }

  @Override
  public Ticket copy(Ticket entity, boolean deep) {
    Ticket copy = super.copy(entity, deep);
    copy.setTicketStatus(Beans.get(TicketStatusService.class).findDefaultStatus());
    copy.setProgressSelect(null);
    copy.setStartDateT(Beans.get(AppBaseService.class).getTodayDateTime().toLocalDateTime());
    copy.setTicketSeq(null);
    return copy;
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {

    AppHelpdesk appHelpdesk = Beans.get(AppHelpdeskService.class).getHelpdeskApp();

    if (context.get("_model") != null
        && context.get("_model").toString().equals(Ticket.class.getName())
        && context.get("id") != null) {

      Long id = (Long) json.get("id");
      if (id != null) {
        TicketStatus ticketStatus =
            Optional.ofNullable(find(id)).map(Ticket::getTicketStatus).orElse(null);
        json.put(
            "$isClosed",
            ticketStatus != null && ticketStatus.equals(appHelpdesk.getClosedTicketStatus()));
        json.put(
            "$isInProgress",
            ticketStatus != null && ticketStatus.equals(appHelpdesk.getInProgressTicketStatus()));
        json.put(
            "$isResolved",
            ticketStatus != null && ticketStatus.equals(appHelpdesk.getResolvedTicketStatus()));
      } else {
        json.put("$isClosed", false);
        json.put("$isInProgress", false);
        json.put("$isResolved", false);
      }
    }
    return super.populate(json, context);
  }
}
