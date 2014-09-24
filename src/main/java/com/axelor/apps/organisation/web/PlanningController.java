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

import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.organisation.db.Planning;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class PlanningController {

	@Inject
	private Provider<PeriodService> periodService;
	
	
	
	public void getPeriod(ActionRequest request, ActionResponse response) {
		
		Planning planning = request.getContext().asType(Planning.class);
	
		try {
			if(planning.getDate() != null && planning.getUser() != null && planning.getUser().getActiveCompany() != null) {
				
				response.setValue("period", periodService.get().rightPeriod(planning.getDate(), planning.getUser().getActiveCompany()));				
			}
			else {
				response.setValue("period", null);
			}
		}
		catch (Exception e){ TraceBackService.trace(response, e); }
	}
	
}
