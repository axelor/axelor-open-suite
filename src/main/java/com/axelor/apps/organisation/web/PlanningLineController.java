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

import java.math.BigDecimal;

import org.joda.time.LocalDateTime;

import com.axelor.apps.base.db.Unit;
import com.axelor.apps.organisation.db.PlanningLine;
import com.axelor.apps.organisation.service.PlanningLineService;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class PlanningLineController {

	@Inject
	private PlanningLineService planningLineService;
	
	public void computeStartDateTime(ActionRequest request, ActionResponse response) throws AxelorException {
				
		PlanningLine planningLine = request.getContext().asType(PlanningLine.class);
		
		LocalDateTime fromDateTime = planningLine.getFromDateTime();
		LocalDateTime toDateTime = planningLine.getToDateTime();
		Unit unit = planningLine.getUnit();
		
		if(planningLine.getFromDateTime() != null)  {
			if(planningLine.getToDateTime() != null) {
				response.setValue("duration", planningLineService.getDuration(fromDateTime, toDateTime, unit));
			}
			else if(planningLine.getDuration() != null) {
				response.setValue("toDateTime", planningLineService.computeEndDateTime(fromDateTime, planningLine.getDuration(), unit));
			}
		}
	}
	
	public void computeEndDateTime(ActionRequest request, ActionResponse response) throws AxelorException {
		
		PlanningLine planningLine = request.getContext().asType(PlanningLine.class);
		
		LocalDateTime fromDateTime = planningLine.getFromDateTime();
		LocalDateTime toDateTime = planningLine.getToDateTime();
		Unit unit = planningLine.getUnit();
		
		if(toDateTime != null)  {
			if(fromDateTime != null)  {
				response.setValue("duration", planningLineService.getDuration(planningLine.getFromDateTime(), toDateTime, unit));
			}
			else if(planningLine.getDuration() != null)  {
				response.setValue("fromDateTime", planningLineService.computeStartDateTime(planningLine.getDuration(), toDateTime, unit));
			}
		}
	}
 	
	public void computeDuration(ActionRequest request, ActionResponse response) throws AxelorException {
		
		PlanningLine planningLine = request.getContext().asType(PlanningLine.class);
		
		BigDecimal duration = planningLine.getDuration();
		
		if(duration != null)  {
			if(planningLine.getFromDateTime() != null)  {
				response.setValue("toDateTime", planningLineService.computeEndDateTime(planningLine.getFromDateTime(), duration, planningLine.getUnit()));
			}
			else if(planningLine.getToDateTime() != null)  {
				response.setValue("fromDateTime", planningLineService.computeStartDateTime(duration, planningLine.getToDateTime(), planningLine.getUnit()));
			}
		}
	}
	
	public void computeUnit(ActionRequest request, ActionResponse response) throws AxelorException {
		
		PlanningLine planningLine = request.getContext().asType(PlanningLine.class);
		
		LocalDateTime fromDateTime = planningLine.getFromDateTime();
		LocalDateTime toDateTime = planningLine.getToDateTime();
		
		if(fromDateTime != null && toDateTime != null)  {
			response.setValue("duration", planningLineService.getDuration(fromDateTime, toDateTime, planningLine.getUnit()));
		}
	}
	
	
}
