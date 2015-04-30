package com.axelor.apps.base.web;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.MailBatch;
import com.axelor.apps.base.service.batch.MailBatchService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class MailBatchController{
	
	@Inject 
	MailBatchService mailBatchService;
	
	public void remindTimesheet(ActionRequest request, ActionResponse response){
		
		MailBatch mailBatch = request.getContext().asType(MailBatch.class);
		
		Batch batch = null;
		
		batch = mailBatchService.remindMail(mailBatchService.find(mailBatch.getId()));
		
		if(batch != null)
			response.setFlash(batch.getComment());
		response.setReload(true);
	}
}
