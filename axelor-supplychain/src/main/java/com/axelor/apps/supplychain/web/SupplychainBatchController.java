package com.axelor.apps.supplychain.web;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.supplychain.db.SupplychainBatch;
import com.axelor.apps.supplychain.service.batch.SupplychainBatchService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class SupplychainBatchController {
	
	@Inject 
	protected SupplychainBatchService supplychainBatchService;
	
	public void billSubscriptions(ActionRequest request, ActionResponse response){
		
		SupplychainBatch supplychainBatch = request.getContext().asType(SupplychainBatch.class);
		
		Batch batch = null;
		
		batch = supplychainBatchService.billSubscriptions(supplychainBatchService.find(supplychainBatch.getId()));
		
		if(batch != null)
			response.setFlash(batch.getComment());
		response.setReload(true);
	}
}
