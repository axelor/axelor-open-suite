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
