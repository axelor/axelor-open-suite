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
package com.axelor.apps.production.web;

import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.service.ProdProcessService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ProdProcessController {
	
	@Inject
	protected ProdProcessService prodProcessService;
	
	public void validateProdProcess(ActionRequest request, ActionResponse response) throws AxelorException{
		ProdProcess prodProcess = request.getContext().asType(ProdProcess.class);
		if(prodProcess.getIsConsProOnOperation()){
			BillOfMaterial bom = null;
			if(request.getContext().getParentContext() != null && request.getContext().getParentContext().getContextClass().getName().equals(BillOfMaterial.class.getName())){
				bom = request.getContext().getParentContext().asType(BillOfMaterial.class);
			}
			else{
				bom = Beans.get(BillOfMaterialRepository.class).all().filter("self.prodProcess.id = ?1", prodProcess.getId()).fetchOne();
			}
			if(bom != null){
				prodProcessService.validateProdProcess(prodProcess,bom);
			}
		}
	}
}
