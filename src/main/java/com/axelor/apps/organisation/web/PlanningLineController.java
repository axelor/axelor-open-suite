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
