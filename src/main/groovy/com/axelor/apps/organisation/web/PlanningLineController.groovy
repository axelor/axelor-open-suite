package com.axelor.apps.organisation.web

import groovy.util.logging.Slf4j
import org.joda.time.Duration

import com.axelor.apps.base.db.Partner
import com.axelor.apps.organisation.db.PlanningLine
import com.axelor.apps.organisation.service.PlanningLineService
import com.axelor.apps.crm.db.Event
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject


@Slf4j
public class PlanningLineController {

	@Inject
	private PlanningLineService planningLineService
	
	def computeStartDateTime(ActionRequest request, ActionResponse response) {
		
		PlanningLine planningline = request.context as PlanningLine
		
		if(planningline.fromDateTime != null)  {
			if(planningline.toDateTime != null)  {
				Duration duration =  planningLineService.computeDuration(planningline.fromDateTime, planningline.toDateTime)
				response.values = [ "duration" : planningLineService.getDaysDuration(planningLineService.getHoursDuration(duration))]
			}
			else if(planningline.duration != null)  {
				response.values = [ "toDateTime" : planningLineService.computeEndDateTime(planningline.fromDateTime, planningline.duration)]
			}
		}
	}
	
	def computeEndDateTime(ActionRequest request, ActionResponse response) {
		
		PlanningLine planningline = request.context as PlanningLine
		
		if(planningline.toDateTime != null)  {
			if(planningline.fromDateTime != null)  {
				Duration duration =  planningLineService.computeDuration(planningline.fromDateTime, planningline.toDateTime)
				response.values = [ "duration" : planningLineService.getDaysDuration(planningLineService.getHoursDuration(duration))]
			}
			else if(planningline.duration != null)  {
				response.values = [ "fromDateTime" : planningLineService.computeStartDateTime(planningline.duration, planningline.toDateTime)]
			}
		}
	}
	
	def computeDuration(ActionRequest request, ActionResponse response) {
		
		PlanningLine planningline = request.context as PlanningLine
		
		if(planningline.duration != null)  {
			if(planningline.fromDateTime != null)  {
				response.values = [ "toDateTime" : planningLineService.computeEndDateTime(planningline.fromDateTime, planningline.duration)]
			}
			else if(planningline.toDateTime != null)  {
				response.values = [ "fromDateTime" : planningLineService.computeStartDateTime(planningline.duration, planningline.toDateTime)]
			}
		}
	}
  
}
