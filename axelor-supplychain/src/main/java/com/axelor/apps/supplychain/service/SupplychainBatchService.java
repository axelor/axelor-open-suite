package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.supplychain.db.SupplychainBatch;
import com.axelor.apps.supplychain.db.repo.SupplychainBatchRepository;
import com.axelor.apps.supplychain.service.batch.BatchSubscription;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class SupplychainBatchService extends SupplychainBatchRepository{
	
	@Inject
	protected BatchSubscription batchSubscription;
	
	public Batch run(String batchCode) throws AxelorException {
		
		Batch batch;
		SupplychainBatch supplychainBatch = findByCode(batchCode);
		
		if (batchCode != null){
			switch (supplychainBatch.getActionSelect()) {
			case ACTION_BILL_SUB:
				batch = billSubscriptions(supplychainBatch);
				break;
			default:
				throw new AxelorException(String.format(I18n.get(IExceptionMessage.BASE_BATCH_1), supplychainBatch.getActionSelect(), batchCode), IException.INCONSISTENCY);
			}
		}
		else {
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.BASE_BATCH_2), batchCode), IException.INCONSISTENCY);
		}
		
		return batch;
	}
	
	public Batch billSubscriptions(SupplychainBatch supplychainBatch){
		return batchSubscription.run(supplychainBatch);
	}

}
