/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.organisation.web;

import org.joda.time.Duration;

import com.axelor.apps.organisation.db.LeaveRequest;
import com.axelor.apps.organisation.service.LeaveRequestService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class LeaveRequestController {

	@Inject
	private LeaveRequestService leaveRequestService;
	
	public void validate(ActionRequest request, ActionResponse response)  {
		
		LeaveRequest leaveRequest = request.getContext().asType(LeaveRequest.class);
		
		try {
			// TODO à placer dans un package intermédiaire
			leaveRequestService.createHolidayEvent(leaveRequest);
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	public void computeStartDateTime(ActionRequest request, ActionResponse response) {
		
		LeaveRequest leaveRequest = request.getContext().asType(LeaveRequest.class);
		
		if(leaveRequest != null && leaveRequest.getStartDateT() != null)  {
			if(leaveRequest.getEndDateT() != null)  {
				Duration nbrOfDayOff =  leaveRequestService.computeDuration(leaveRequest.getStartDateT(), leaveRequest.getEndDateT());
				response.setValue("nbrOfDayOff", leaveRequestService.getDaysDuration(leaveRequestService.getHoursDuration(nbrOfDayOff)));
			}
			else if(leaveRequest.getNbrOfDayOff() != null)  {
				response.setValue("endDateT", leaveRequestService.computeEndDateTime(leaveRequest.getStartDateT(), leaveRequest.getNbrOfDayOff().doubleValue()));
			}
		}
	}
	
	public void computeEndDateTime(ActionRequest request, ActionResponse response) {
		
		LeaveRequest leaveRequest = request.getContext().asType(LeaveRequest.class);
		
		if(leaveRequest != null && leaveRequest.getEndDateT() != null)  {
			if(leaveRequest.getStartDateT() != null)  {
				Duration nbrOfDayOff =  leaveRequestService.computeDuration(leaveRequest.getStartDateT(), leaveRequest.getEndDateT());
				response.setValue("nbrOfDayOff", leaveRequestService.getDaysDuration(leaveRequestService.getHoursDuration(nbrOfDayOff)));
			}
			else if(leaveRequest.getNbrOfDayOff() != null) {
				response.setValue("startDateT", leaveRequestService.computeStartDateTime(leaveRequest.getNbrOfDayOff().doubleValue(), leaveRequest.getEndDateT()));
			}
		}
	}
	
	public void computeDuration(ActionRequest request, ActionResponse response) {
		
		LeaveRequest leaveRequest = request.getContext().asType(LeaveRequest.class);
		
		if(leaveRequest != null && leaveRequest.getNbrOfDayOff() != null)  {
			if(leaveRequest.getStartDateT() != null)  {
				response.setValue("endDateT", leaveRequestService.computeEndDateTime(leaveRequest.getStartDateT(), leaveRequest.getNbrOfDayOff().doubleValue()));
			}
			else if(leaveRequest.getEndDateT() != null)  {
				response.setValue("startDateT", leaveRequestService.computeStartDateTime(leaveRequest.getNbrOfDayOff().doubleValue(), leaveRequest.getEndDateT()));
			}
		}
	}
}
