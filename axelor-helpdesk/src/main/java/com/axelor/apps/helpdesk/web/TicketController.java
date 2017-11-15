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
package com.axelor.apps.helpdesk.web;

import java.util.List;

import javax.inject.Inject;

import com.axelor.apps.helpdesk.db.Ticket;
import com.axelor.apps.helpdesk.db.repo.TicketRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class TicketController {
	
	@Inject
	private TicketRepository ticketRepo;

	public void assignToMeTicket(ActionRequest request, ActionResponse response)  {
		
		if(request.getContext().get("id") != null){
			Ticket ticket = ticketRepo.find((Long)request.getContext().get("id"));
			ticket.setAssignedTo(AuthUtils.getUser());
			ticketRepo.save(ticket);
		}
		else if(!((List<?>)request.getContext().get("_ids")).isEmpty()){
			for(Ticket ticket : ticketRepo.all().filter("id in ?1",request.getContext().get("_ids")).fetch()){
				ticket.setAssignedTo(AuthUtils.getUser());
				ticketRepo.save(ticket);
			}
		}
		response.setReload(true);
	}
}
