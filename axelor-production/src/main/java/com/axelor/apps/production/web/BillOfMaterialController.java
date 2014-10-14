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

import javax.inject.Inject;

import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.service.BillOfMaterialService;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class BillOfMaterialController {

	@Inject
	BillOfMaterialService billOfMaterialService;
	
	public void computeCostPrice (ActionRequest request, ActionResponse response) throws AxelorException {

		BillOfMaterial billOfMaterial = request.getContext().asType( BillOfMaterial.class );

		billOfMaterialService.computeCostPrice(billOfMaterialService.find(billOfMaterial.getId()));
		
		response.setReload(true);
		
	}
	
	
	public void updateProductCostPrice (ActionRequest request, ActionResponse response) throws AxelorException {

		BillOfMaterial billOfMaterial = request.getContext().asType( BillOfMaterial.class );

		billOfMaterialService.updateProductCostPrice(billOfMaterialService.find(billOfMaterial.getId()));
		
		response.setReload(true);
		
	}
	
}
