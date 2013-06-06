package com.axelor.apps.organisation.web

import groovy.util.logging.Slf4j

import org.joda.time.Duration
import com.axelor.apps.organisation.service.LeaveRequestService
import com.axelor.apps.organisation.db.LeaveRequest
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject

@Slf4j
class LeaveRequestController {
	
@Inject
private LeaveRequestService leaveRequestService

	def void validate(ActionRequest request, ActionResponse response)  {
		
		LeaveRequest leaveRequest = request.context as LeaveRequest
		
		try {

				
			// TODO à placer dans un package intermédiaire
			leaveRequestService.createHolidayEvent(leaveRequest)
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
		
	}

	def computeStartDateTime(ActionRequest request, ActionResponse response) {
		
		LeaveRequest leaveRequest = request.context as LeaveRequest
		
		if(leaveRequest && leaveRequest?.startDateT)  {
			if(leaveRequest?.endDateT)  {
				Duration nbrOfDayOff =  leaveRequestService.computeDuration(leaveRequest.startDateT, leaveRequest.endDateT)
				response.values = [ "nbrOfDayOff" : leaveRequestService.getDaysDuration(leaveRequestService.getHoursDuration(nbrOfDayOff))]
			}
			else if(leaveRequest?.nbrOfDayOff)  {
				response.values = [ "endDateT" : leaveRequestService.computeEndDateTime(leaveRequest.startDateT, leaveRequest.nbrOfDayOff)]
			}
		}
	}
	
	def computeEndDateTime(ActionRequest request, ActionResponse response) {
		
		LeaveRequest leaveRequest = request.context as LeaveRequest
		
		if(leaveRequest && leaveRequest?.endDateT)  {
			if(leaveRequest?.startDateT)  {
				Duration nbrOfDayOff =  leaveRequestService.computeDuration(leaveRequest.startDateT, leaveRequest.endDateT)
				response.values = [ "nbrOfDayOff" : leaveRequestService.getDaysDuration(leaveRequestService.getHoursDuration(nbrOfDayOff))]
			}
			else if(leaveRequest?.nbrOfDayOff)  {
				response.values = [ "startDateT" : leaveRequestService.computeStartDateTime(leaveRequest.nbrOfDayOff, leaveRequest.endDateT)]
			}
		}
	}
	
	def computeDuration(ActionRequest request, ActionResponse response) {
		
		LeaveRequest leaveRequest = request.context as LeaveRequest
		
		if(leaveRequest && leaveRequest?.nbrOfDayOff)  {
			if(leaveRequest?.startDateT)  {
				response.values = [ "endDateT" : leaveRequestService.computeEndDateTime(leaveRequest.startDateT, leaveRequest.nbrOfDayOff)]
			}
			else if(leaveRequest?.endDateT)  {
				response.values = [ "startDateT" : leaveRequestService.computeStartDateTime(leaveRequest.nbrOfDayOff, leaveRequest.endDateT)]
			}
		}
	}
}
