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
package com.axelor.apps.production.web;

import java.math.BigDecimal;

import javax.inject.Inject;

import com.axelor.apps.production.service.ProductionOrderWizardService;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class ProductionOrderWizardController {

	@Inject
	private ProductionOrderWizardService productionOrderWizardService;
	
	public void validate (ActionRequest request, ActionResponse response) throws AxelorException {

		Context context = request.getContext();
		
		if(context.get("qty") == null || new BigDecimal((String)context.get("qty")).compareTo(BigDecimal.ZERO) <= 0)  {
			response.setFlash("Veuillez entrer une quantité positive !");
		}
		else if(context.get("billOfMaterial") == null)  {
			response.setFlash("Veuillez sélectionner une nomenclature !");
		}
		else  {
			
			response.setFlash(productionOrderWizardService.validate(context));
			
		}
	}
	
}
