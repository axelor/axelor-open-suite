package com.axelor.apps.organisation.web

import groovy.util.logging.Slf4j

import com.axelor.apps.crm.service.EventService
import com.axelor.apps.organisation.db.LeaveRequest
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject

@Slf4j
class LeaveRequestController {
	
@Inject
private EventService eventService

	def void validate(ActionRequest request, ActionResponse response)  {
		
		LeaveRequest leaveRequest = request.context as LeaveRequest
		
		try {

			eventService.createHolidayEvent(leaveRequest)
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
		
	}

	
}
