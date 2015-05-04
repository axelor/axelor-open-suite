package com.axelor.apps.base.web;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.MailBatch;
import com.axelor.apps.base.db.repo.MailBatchRepository;
import com.axelor.apps.base.service.batch.MailBatchService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class MailBatchController{
	
	@Inject 
	MailBatchService mailBatchService;
	
	public void remindTimesheet(ActionRequest request, ActionResponse response) throws AxelorException{
		
		MailBatch mailBatch = request.getContext().asType(MailBatch.class);
		
		Batch batch = null;
		
		batch = mailBatchService.remindMail(mailBatchService.find(mailBatch.getId()));
		
		if(batch != null)
			response.setFlash(batch.getComment());
		response.setReload(true);
	}
	
	public void remindTimesheetGeneral(ActionRequest request, ActionResponse response) throws AxelorException{
		
		MailBatch mailBatch = Beans.get(MailBatchRepository.class).findByCode(MailBatchRepository.CODE_BATCH_EMAIL_TIME_SHEET);
		
		Batch batch = null;
		
		batch = mailBatchService.remindMail(mailBatch);
		
		if(batch != null)
			response.setFlash(batch.getComment());
		response.setReload(true);
	}
}
