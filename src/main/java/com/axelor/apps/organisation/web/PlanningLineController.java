package com.axelor.apps.organisation.web;

import org.joda.time.Duration;

import com.axelor.apps.organisation.db.PlanningLine;
import com.axelor.apps.organisation.service.PlanningLineService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class PlanningLineController {

	@Inject
	private PlanningLineService planningLineService;
	
	public void computeStartDateTime(ActionRequest request, ActionResponse response) {
		
		PlanningLine planningline = request.getContext().asType(PlanningLine.class);
		
		if(planningline != null && planningline.getFromDateTime() != null)  {
			if(planningline.getToDateTime() != null) {
				Duration duration =  planningLineService.computeDuration(planningline.getFromDateTime(), planningline.getToDateTime());
				response.setValue("duration", planningLineService.getDaysDuration(planningLineService.getHoursDuration(duration)));
			}
			else if(planningline.getDuration() != null) {
				response.setValue("toDateTime", planningLineService.computeEndDateTime(planningline.getFromDateTime(), planningline.getDuration().doubleValue()));
			}
		}
	}
	
	public void computeEndDateTime(ActionRequest request, ActionResponse response) {
		
		PlanningLine planningline = request.getContext().asType(PlanningLine.class);
		
		if(planningline != null && planningline.getToDateTime() != null)  {
			if(planningline.getFromDateTime() != null)  {
				Duration duration =  planningLineService.computeDuration(planningline.getFromDateTime(), planningline.getToDateTime());
				response.setValue("duration", planningLineService.getDaysDuration(planningLineService.getHoursDuration(duration)));
			}
			else if(planningline.getDuration() != null)  {
				response.setValue("fromDateTime", planningLineService.computeStartDateTime(planningline.getDuration().doubleValue(), planningline.getToDateTime()));
			}
		}
	}
	
	public void computeDuration(ActionRequest request, ActionResponse response) {
		
		PlanningLine planningline = request.getContext().asType(PlanningLine.class);
		
		if(planningline != null && planningline.getDuration() != null)  {
			if(planningline.getFromDateTime() != null)  {
				response.setValue("toDateTime", planningLineService.computeEndDateTime(planningline.getFromDateTime(), planningline.getDuration().doubleValue()));
			}
			else if(planningline.getToDateTime() != null)  {
				response.setValue("fromDateTime", planningLineService.computeStartDateTime(planningline.getDuration().doubleValue(), planningline.getToDateTime()));
			}
		}
	}
}
