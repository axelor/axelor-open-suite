/**
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
package com.axelor.apps.crm.web;

import java.time.Duration;
import java.util.List;

import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.crm.db.Ticket;
import com.axelor.apps.crm.db.repo.TicketRepository;
import com.axelor.apps.crm.exception.IExceptionMessage;
import com.axelor.apps.crm.service.EventService;
import com.axelor.apps.crm.service.TicketService;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class TicketController {
	
	@Inject
	private TicketRepository ticketRepo;
	
	@Inject
	private TicketService ticketService;
	
	@Inject
	private EventService eventService;
	
	@SuppressWarnings("rawtypes")
	public void assignToMe(ActionRequest request, ActionResponse response)  {

		if(request.getContext().get("id") != null){
			Ticket ticket = ticketRepo.find((Long)request.getContext().get("id"));
			ticket.setUser(AuthUtils.getUser());
			ticketService.saveTicket(ticket);
		}
		else if(!((List)request.getContext().get("_ids")).isEmpty()){
			for(Ticket ticket : ticketRepo.all().filter("id in ?1",request.getContext().get("_ids")).fetch()){
				ticket.setUser(AuthUtils.getUser());
				ticketService.saveTicket(ticket);
			}
		}
		response.setReload(true);

	}
	
	public void setSequence(ActionRequest request, ActionResponse response) throws AxelorException {

		Ticket ticket = request.getContext().asType(Ticket.class);

		if(ticket.getTicketNumberSeq() ==  null){
			String seq = Beans.get(SequenceService.class).getSequenceNumber(IAdministration.TICKET);
			if (seq == null)
				throw new AxelorException(I18n.get(IExceptionMessage.TICKET_1),
								IException.CONFIGURATION_ERROR);
			else
				response.setValue("ticketNumberSeq", seq);
		}
	}
	
	public void computeFromStartDateTime(ActionRequest request, ActionResponse response) {

		Ticket ticket = request.getContext().asType(Ticket.class);


		if(ticket.getStartDateTime() != null) {
			if(ticket.getDuration() != null && ticket.getDuration() != 0) {
				response.setValue("endDateTime", eventService.computeEndDateTime(ticket.getStartDateTime(), ticket.getDuration().intValue()));
			}
			else if(ticket.getEndDateTime() != null && ticket.getEndDateTime().isAfter(ticket.getStartDateTime())) {
				Duration duration =  eventService.computeDuration(ticket.getStartDateTime(), ticket.getEndDateTime());
				response.setValue("duration", eventService.getDuration(duration));
			}
		}
	}

	public void computeFromEndDateTime(ActionRequest request, ActionResponse response) {

		Ticket ticket = request.getContext().asType(Ticket.class);

		if(ticket.getEndDateTime() != null) {
			if(ticket.getStartDateTime() != null && ticket.getStartDateTime().isBefore(ticket.getEndDateTime())) {
				Duration duration =  eventService.computeDuration(ticket.getStartDateTime(), ticket.getEndDateTime());
				response.setValue("duration", eventService.getDuration(duration));
			}
			else if(ticket.getDuration() != null)  {
				response.setValue("startDateTime", eventService.computeStartDateTime(ticket.getDuration().intValue(), ticket.getEndDateTime()));
			}
		}
	}

	public void computeFromDuration(ActionRequest request, ActionResponse response) {

		Ticket ticket = request.getContext().asType(Ticket.class);

		if(ticket.getDuration() != null)  {
			if(ticket.getStartDateTime() != null)  {
				response.setValue("endDateTime", eventService.computeEndDateTime(ticket.getStartDateTime(), ticket.getDuration().intValue()));
			}
			else if(ticket.getEndDateTime() != null)  {
				response.setValue("startDateTime", eventService.computeStartDateTime(ticket.getDuration().intValue(), ticket.getEndDateTime()));
			}
		}
	}

}
