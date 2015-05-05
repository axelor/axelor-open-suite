package com.axelor.apps.base.service.batch;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.MailBatch;
import com.axelor.apps.base.db.repo.MailBatchRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class MailBatchService extends MailBatchRepository{
	
	@Inject
	protected BatchReminderMail batchReminderMail;
	
	public Batch run(String batchCode) throws AxelorException {
		
		Batch batch;
		MailBatch mailBatch = findByCode(batchCode);
		
		if (batchCode != null){
			switch (mailBatch.getActionSelect()) {
			case ACTION_REMIN_TIMESHEET:
				batch = null;
				break;
			default:
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.BASE_BATCH_1), mailBatch.getActionSelect(), batchCode), IException.INCONSISTENCY);
			}
		}
		else {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.BASE_BATCH_2), batchCode), IException.INCONSISTENCY);
		}
		
		return batch;
	}
	
	public Batch remindMail(MailBatch mailBatch) throws AxelorException{
		return this.run(mailBatch.getCode());
	}
}
