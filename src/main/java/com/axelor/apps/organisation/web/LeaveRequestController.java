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
