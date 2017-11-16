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
package com.axelor.apps.helpdesk.service;

import java.time.LocalDateTime;
import java.util.List;

import javax.inject.Inject;

import com.axelor.apps.base.db.AppHelpdesk;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.repo.AppHelpdeskRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.helpdesk.db.SLA;
import com.axelor.apps.helpdesk.db.Ticket;
import com.axelor.apps.helpdesk.db.repo.SLARepository;
import com.axelor.apps.helpdesk.db.repo.TicketRepository;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.service.publicHoliday.PublicHolidayService;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.google.common.base.Strings;
import com.google.inject.persist.Transactional;

public class TicketService {
	
	@Inject
	private SequenceService sequenceService;
	
	@Inject
	private AppHelpdeskRepository appHelpdeskRepo;

	@Inject
	private TicketRepository ticketRepo;
	
	@Inject
	private SLARepository slaRepo;

	@Inject
	private PublicHolidayService publicHolidayService;

	@Inject
	private WeeklyPlanningService weeklyPlanningService;
	
	private LocalDateTime toDate;
	
	public void computeSeq(Ticket ticket) {
		
		if (Strings.isNullOrEmpty(ticket.getTicketSeq())) {
			String ticketSeq = sequenceService.getSequenceNumber(IAdministration.TICKET, null);
			ticket.setTicketSeq(ticketSeq);
		}
	}

	public void computeSLA(Ticket ticket) {
		
		AppHelpdesk helpdesk = appHelpdeskRepo.all().fetchOne();

		if (helpdesk.getIsSLApolicies()) {

			if (ticket.getAssignedTo() != null) {
				
				SLA sla = slaRepo.all().filter("self.team = ?1 and self.ticketType = ?2 and (self.priority = ?3 or self.priority = 4)",
						ticket.getAssignedTo().getActiveTeam(), ticket.getTicketType(), ticket.getPriority()).fetchOne();
				
				if (sla != null) {
					ticket.setSlaPolicy(sla);
					try {
						this.computeDuration(ticket, sla);
					} catch (AxelorException e) {
						e.printStackTrace();
					}
				} else {
					this.setEmptySLA(ticket);
				}
			} else {
				this.setEmptySLA(ticket);
			}
		}
	}
	
	public void computeDuration(Ticket ticket, SLA sla) throws AxelorException {

		if (sla.getIsWorkingDays()) {

			if (ticket.getAssignedTo().getEmployee() != null) {
				
				if (sla.getDays() > 0) {
					LocalDateTime fromDate = ticket.getStartDate().plusDays(1);
					this.checkWorkingDays(fromDate, ticket.getAssignedTo().getEmployee(), sla.getDays());
					ticket.setDeadline(toDate.plusHours(sla.getHours()));
					
				} else {
					ticket.setDeadline(ticket.getStartDate().plusHours(sla.getHours()));
				}
			} else {
				LocalDateTime localDateTime = ticket.getStartDate().plusDays(sla.getDays());
				localDateTime = localDateTime.plusHours(sla.getHours());
				ticket.setDeadline(localDateTime);
			}
		} else {
			LocalDateTime localDateTime = ticket.getStartDate().plusDays(sla.getDays());
			localDateTime = localDateTime.plusHours(sla.getHours());
			ticket.setDeadline(localDateTime);
		}
	}
	
	public void checkWorkingDays(LocalDateTime fromDate, Employee employee, int days) throws AxelorException {

		if (weeklyPlanningService.workingDayValue(employee.getPlanning(), fromDate.toLocalDate()) == 0
				|| publicHolidayService.checkPublicHolidayDay(fromDate.toLocalDate(), employee)) {

			fromDate = fromDate.plusDays(1);
			this.checkWorkingDays(fromDate, employee, days);

		} else {
			toDate = fromDate;
			days--;
			if (days != 0) {
				fromDate = fromDate.plusDays(1);
				this.checkWorkingDays(fromDate, employee, days);
			}
		}
	}
	
	public void setEmptySLA(Ticket ticket) {

		ticket.setSlaPolicy(null);
	}
	
	public void checkSLAcompleted(Ticket ticket) {

		if (ticket.getSlaPolicy() != null) {

			LocalDateTime currentDate = LocalDateTime.now();

			if (ticket.getStatusSelect() >= ticket.getSlaPolicy().getReachStage()
					&& (currentDate.isBefore(ticket.getDeadline()) || currentDate.isEqual(ticket.getDeadline()))) {

				ticket.setSlaCompleted(true);
			} else {
				ticket.setSlaCompleted(false);
			}
		}
	}
	
	@Transactional
	public void assignToMeTicket(Long id, List<?> ids) {

		if (id != null) {
			Ticket ticket = ticketRepo.find(id);
			ticket.setAssignedTo(AuthUtils.getUser());
			ticketRepo.save(ticket);

		} else if (!ids.isEmpty()) {
			
			for (Ticket ticket : ticketRepo.all().filter("id in ?1", ids).fetch()) {
				ticket.setAssignedTo(AuthUtils.getUser());
				ticketRepo.save(ticket);
			}
		}
	}

}