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
			if(request.getContext().getParentContext().getContextClass().getName().equals(BillOfMaterial.class.getName())){
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
