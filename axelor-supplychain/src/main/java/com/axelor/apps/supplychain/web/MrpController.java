/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.supplychain.db.Mrp;
import com.axelor.apps.supplychain.db.repo.MrpRepository;
import com.axelor.apps.supplychain.service.MrpService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.meta.db.repo.MetaActionRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class MrpController {
	
	@Inject
	protected MrpService mrpService;
	
	@Inject
	protected MrpRepository mrpRepository;
	
	@Inject
	protected MetaActionRepository metaActionRepository;
	
	public void runCalculation(ActionRequest request, ActionResponse response)  {
	
		Mrp mrp = request.getContext().asType(Mrp.class);
		
		try {
			mrpService.runCalculation(mrpRepository.find(mrp.getId()));
		} catch (AxelorException e) {
			TraceBackService.trace(response, e);
			mrpService.reset(mrpRepository.find(mrp.getId()));
		}
		finally  {
			response.setReload(true);
		}
		
	}
	
	public void generateAllProposals(ActionRequest request, ActionResponse response) throws AxelorException  {
		Mrp mrp = request.getContext().asType(Mrp.class);
		mrpService.generateProposals(mrpRepository.find(mrp.getId()));
		response.setReload(true);
	}
	
}
