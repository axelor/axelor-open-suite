/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
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
