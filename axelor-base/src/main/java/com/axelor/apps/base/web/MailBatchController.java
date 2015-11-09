package com.axelor.apps.base.web;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.MailBatch;
import com.axelor.apps.base.db.repo.MailBatchRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.batch.MailBatchService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class MailBatchController{

	@Inject
	MailBatchService mailBatchService;
	
	@Inject
	MailBatchRepository mailBatchRepo;

	public void remindTimesheet(ActionRequest request, ActionResponse response) throws AxelorException{

		MailBatch mailBatch = request.getContext().asType(MailBatch.class);

		Batch batch = null;

		batch = mailBatchService.remindMail(mailBatchRepo.find(mailBatch.getId()));

		if(batch != null)
			response.setFlash(batch.getComments());
		response.setReload(true);
	}

	public void remindTimesheetGeneral(ActionRequest request, ActionResponse response) throws AxelorException{

		MailBatch mailBatch = Beans.get(MailBatchRepository.class).findByCode(MailBatchRepository.CODE_BATCH_EMAIL_TIME_SHEET);
		if(mailBatch != null){
			Batch batch = null;
			batch = mailBatchService.remindMail(mailBatch);

			if(batch != null)
				response.setFlash(batch.getComments());
			response.setReload(true);
		}
		else{
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.BASE_BATCH_2), MailBatchRepository.CODE_BATCH_EMAIL_TIME_SHEET), IException.INCONSISTENCY);
		}
	}
}
