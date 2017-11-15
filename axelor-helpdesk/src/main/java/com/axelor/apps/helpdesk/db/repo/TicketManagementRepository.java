/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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

import javax.inject.Inject;

import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.helpdesk.db.Ticket;
import com.axelor.apps.helpdesk.service.TicketService;
import com.google.common.base.Strings;

public class TicketManagementRepository extends TicketRepository {
	
	@Inject
	private TicketService ticketService;
	
	@Inject
	private SequenceService sequenceService;

	@Override
	public Ticket save(Ticket ticket) {
		
		computeSeq(ticket);
		ticketService.computeSLA(ticket);
		ticketService.checkSLAcompleted(ticket);
		return super.save(ticket);
	}

	public void computeSeq(Ticket ticket) {
		
		if (Strings.isNullOrEmpty(ticket.getTicketSeq())) {
			String ticketSeq = sequenceService.getSequenceNumber(IAdministration.TICKET, null);
			ticket.setTicketSeq(ticketSeq);
		}
	}

}
