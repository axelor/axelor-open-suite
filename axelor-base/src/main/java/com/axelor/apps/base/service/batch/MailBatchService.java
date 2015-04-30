package com.axelor.apps.base.service.batch;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.MailBatch;
import com.axelor.apps.base.db.repo.MailBatchRepository;
import com.google.inject.Inject;

public class MailBatchService extends MailBatchRepository{
	
	@Inject
	protected BatchReminderMail batchReminderMail;
	
	public Batch remindMail(MailBatch mailBatch){
		return batchReminderMail.run(mailBatch);
	}
}
