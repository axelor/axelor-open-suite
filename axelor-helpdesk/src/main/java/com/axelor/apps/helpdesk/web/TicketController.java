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
package com.axelor.apps.helpdesk.web;

import com.axelor.apps.helpdesk.db.Ticket;
import com.axelor.apps.helpdesk.exceptions.IExceptionMessage;
import com.axelor.apps.helpdesk.service.TicketServiceImpl;
import com.axelor.apps.tool.date.DateTool;
import com.axelor.apps.tool.date.DurationTool;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.time.Duration;
import java.util.List;

@Singleton
public class TicketController {

  @Inject private TicketServiceImpl ticketService;

  /**
   * Ticket assign to the current user.
   *
   * @param request
   * @param response
   */
  public void assignToMeTicket(ActionRequest request, ActionResponse response) {

    Long id = (Long) request.getContext().get("id");
    List<?> ids = (List<?>) request.getContext().get("_ids");

    if (id == null && ids == null) {
      response.setAlert(I18n.get(IExceptionMessage.SELECT_TICKETS));
    } else {
      ticketService.assignToMeTicket(id, ids);
      response.setReload(true);
    }
  }

  /**
   * Compute duration or endDateTime from startDateTime
   *
   * @param request
   * @param response
   */
  public void computeFromStartDateTime(ActionRequest request, ActionResponse response) {

    Ticket ticket = request.getContext().asType(Ticket.class);

    if (ticket.getStartDateT() != null) {
      if (ticket.getDuration() != null && ticket.getDuration() != 0) {
        response.setValue(
            "endDateT", DateTool.plusSeconds(ticket.getStartDateT(), ticket.getDuration()));

      } else if (ticket.getEndDateT() != null
          && ticket.getEndDateT().isAfter(ticket.getStartDateT())) {
        Duration duration =
            DurationTool.computeDuration(ticket.getStartDateT(), ticket.getEndDateT());
        response.setValue("duration", DurationTool.getSecondsDuration(duration));
      }
    }
  }

  /**
   * Compute startDateTime or endDateTime from duration
   *
   * @param request
   * @param response
   */
  public void computeFromDuration(ActionRequest request, ActionResponse response) {

    Ticket ticket = request.getContext().asType(Ticket.class);

    if (ticket.getDuration() != null) {
      if (ticket.getStartDateT() != null) {
        response.setValue(
            "endDateT", DateTool.plusSeconds(ticket.getStartDateT(), ticket.getDuration()));

      } else if (ticket.getEndDateT() != null) {
        response.setValue(
            "startDateT", DateTool.minusSeconds(ticket.getEndDateT(), ticket.getDuration()));
      }
    }
  }

  /**
   * Compute duration or startDateTime from endDateTime
   *
   * @param request
   * @param response
   */
  public void computeFromEndDateTime(ActionRequest request, ActionResponse response) {

    Ticket ticket = request.getContext().asType(Ticket.class);

    if (ticket.getEndDateT() != null) {

      if (ticket.getStartDateT() != null && ticket.getStartDateT().isBefore(ticket.getEndDateT())) {
        Duration duration =
            DurationTool.computeDuration(ticket.getStartDateT(), ticket.getEndDateT());
        response.setValue("duration", DurationTool.getSecondsDuration(duration));

      } else if (ticket.getDuration() != null) {
        response.setValue(
            "startDateT", DateTool.minusSeconds(ticket.getEndDateT(), ticket.getDuration()));
      }
    }
  }
}
