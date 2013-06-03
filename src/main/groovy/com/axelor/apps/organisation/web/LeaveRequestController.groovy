package com.axelor.apps.organisation.web

import groovy.util.logging.Slf4j

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

	
}
