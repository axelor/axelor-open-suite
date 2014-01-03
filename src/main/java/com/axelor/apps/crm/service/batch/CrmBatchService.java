/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.crm.service.batch;

import javax.inject.Inject;
import javax.inject.Provider;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.crm.service.batch.BatchStrategy;
import com.axelor.apps.crm.db.CrmBatch;
import com.axelor.apps.crm.db.ICrmBatch;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

/**
 * InvoiceBatchService est une classe implémentant l'ensemble des batchs de
 * comptabilité et assimilé.
 * 
 * @author Geoffrey DUBAUX
 * 
 * @version 0.1
 */
public class CrmBatchService {

	@Inject
	private Provider<BatchEventReminder> eventReminderProvider;
	
	@Inject
	private Provider<BatchTarget> targetProvider;
	

// Appel 	
	
	/**
	 * Lancer un batch à partir de son code.
	 * 
	 * @param batchCode
	 * 		Le code du batch souhaité.
	 * 
	 * @throws AxelorException
	 */
	public Batch run(String batchCode) throws AxelorException {
				
		Batch batch;
		CrmBatch crmBatch = CrmBatch.all().filter("code = ?1", batchCode).fetchOne();
		
		if (crmBatch != null){
			switch (crmBatch.getActionSelect()) {
			
			case ICrmBatch.BATCH_EVENT_REMINDER:
				batch = eventReminder(crmBatch);
				break;
				
			case ICrmBatch.BATCH_TARGET:
				batch = target(crmBatch);
				break;
				
			default:
				throw new AxelorException(String.format("Action %s inconnu pour le traitement %s", crmBatch.getActionSelect(), batchCode), IException.INCONSISTENCY);
			}
		}
		else {
			throw new AxelorException(String.format("Batch %s inconnu", batchCode), IException.INCONSISTENCY);
		}
		
		return batch;
	}
	
	
	public Batch eventReminder(CrmBatch crmBatch) {
		
		BatchStrategy strategy = eventReminderProvider.get(); 
		return strategy.run(crmBatch);
		
	}
	
	public Batch target(CrmBatch crmBatch) {
		
		BatchStrategy strategy = targetProvider.get(); 
		return strategy.run(crmBatch);
		
	}

	
	
}
