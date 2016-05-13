package com.axelor.apps.hr.service.batch;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.hr.db.HrBatch;
import com.axelor.apps.hr.db.repo.HrBatchRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;

public class HrBatchService {
	
	public Batch run(HrBatch hrBatch) throws AxelorException{
			
			Batch batch = null;
			
			switch (hrBatch.getActionSelect()) {
			case HrBatchRepository.ACTION_LEAVE_MANAGEMENT:
				batch = leaveManagement(hrBatch);
				break;
			default:
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.BASE_BATCH_1), hrBatch.getActionSelect(), hrBatch.getCode()), IException.INCONSISTENCY);
			}
			
			return batch;
		}


	
	public Batch leaveManagement(HrBatch hrBatch){
		
		return Beans.get(BatchLeaveManagement.class).run(hrBatch);
	}


}

