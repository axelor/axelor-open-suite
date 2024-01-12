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
package com.axelor.apps.helpdesk.rest.dto;

import com.axelor.apps.helpdesk.db.Ticket;
import com.axelor.apps.helpdesk.db.TicketStatus;
import com.axelor.utils.api.ResponseStructure;
import java.time.LocalDateTime;

public class TicketResponse extends ResponseStructure {

  private final Long id;
  private final String ticketSeq;
  private final TicketStatus ticketStatus;
  private final LocalDateTime startDateT;
  private final LocalDateTime endDateT;
  private final Long duration;

  public TicketResponse(Ticket ticket) {
    super(ticket.getVersion());
    this.id = ticket.getId();
    this.ticketSeq = ticket.getTicketSeq();
    this.ticketStatus = ticket.getTicketStatus();
    this.startDateT = ticket.getStartDateT();
    this.endDateT = ticket.getEndDateT();
    this.duration = ticket.getDuration();
  }

  public Long getId() {
    return id;
  }

  public String getTicketSeq() {
    return ticketSeq;
  }

  public TicketStatus getTicketStatus() {
    return ticketStatus;
  }

  public LocalDateTime getStartDateT() {
    return startDateT;
  }

  public LocalDateTime getEndDateT() {
    return endDateT;
  }

  public Long getDuration() {
    return duration;
  }
}
