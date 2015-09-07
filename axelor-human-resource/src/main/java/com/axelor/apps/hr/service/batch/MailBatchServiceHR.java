package com.axelor.apps.hr.service.batch;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.MailBatch;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.batch.MailBatchService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;

public class MailBatchServiceHR extends MailBatchService{
	@Override
	public Batch run(String batchCode) throws AxelorException {
		Batch batch = super.run(batchCode);
		MailBatch mailBatch = findByCode(batchCode);
		
		if (batchCode != null){
			switch (mailBatch.getActionSelect()) {
			case ACTION_REMIN_TIMESHEET:
				batch = reminderTimesheet(mailBatch);
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
	
	
	public Batch reminderTimesheet(MailBatch mailBatch) {
		
		return Beans.get(BatchReminderTimesheet.class).run(mailBatch);
		
	}
	
}
