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
package com.axelor.apps.helpdesk.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.helpdesk.db.Ticket;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class TicketSequenceServiceImpl implements TicketSequenceService {

  protected SequenceService sequenceService;

  @Inject
  public TicketSequenceServiceImpl(SequenceService sequenceService) {
    this.sequenceService = sequenceService;
  }

  @Override
  public void computeSeq(Ticket ticket) throws AxelorException {
    if (Strings.isNullOrEmpty(ticket.getTicketSeq())) {
      String ticketSeq =
          sequenceService.getSequenceNumber(
              SequenceRepository.TICKET, null, Ticket.class, "ticketSeq", ticket);
      ticket.setTicketSeq(ticketSeq);
    }
  }
}
