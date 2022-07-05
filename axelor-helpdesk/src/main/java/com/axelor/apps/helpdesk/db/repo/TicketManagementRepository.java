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
package com.axelor.apps.helpdesk.db.repo;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.helpdesk.db.Ticket;
import com.axelor.apps.helpdesk.service.TicketService;
import com.google.inject.Inject;

public class TicketManagementRepository extends TicketRepository {

  @Inject private TicketService ticketService;
  @Inject private AppBaseService appBaseService;

  @Override
  public Ticket save(Ticket ticket) {

    ticketService.computeSeq(ticket);
    ticketService.computeSLA(ticket);
    ticketService.checkSLAcompleted(ticket);
    return super.save(ticket);
  }

  @Override
  public Ticket copy(Ticket entity, boolean deep) {
    Ticket copy = super.copy(entity, deep);
    copy.setStatusSelect(null);
    copy.setProgressSelect(null);
    copy.setStartDateT(appBaseService.getTodayDateTime().toLocalDateTime());
    copy.setTicketSeq(null);
    return copy;
  }
}
