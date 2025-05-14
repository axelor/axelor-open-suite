/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.helpdesk.service;

import com.axelor.apps.helpdesk.db.Ticket;
import com.axelor.apps.helpdesk.db.repo.TicketRepository;
import com.axelor.auth.AuthUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class TicketAssignmentServiceImpl implements TicketAssignmentService {

  protected TicketRepository ticketRepository;

  @Inject
  public TicketAssignmentServiceImpl(TicketRepository ticketRepository) {
    this.ticketRepository = ticketRepository;
  }

  /** Ticket assign to the current user. */
  @Override
  @Transactional
  public void assignToMeTicket(Long id, List<?> ids) {

    if (id != null) {
      Ticket ticket = ticketRepository.find(id);
      ticket.setAssignedToUser(AuthUtils.getUser());
      ticketRepository.save(ticket);

    } else if (!ids.isEmpty()) {

      for (Ticket ticket : ticketRepository.all().filter("id in ?1", ids).fetch()) {
        ticket.setAssignedToUser(AuthUtils.getUser());
        ticketRepository.save(ticket);
      }
    }
  }
}
