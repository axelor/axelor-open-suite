/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
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
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.supplychain.service.batch;


import javax.inject.Inject;
import javax.inject.Provider;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.supplychain.db.ISupplychainBatch;
import com.axelor.apps.supplychain.db.SupplychainBatch;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

public class SupplychainBatchService {

	@Inject
	private Provider<BatchInvoicing> invoicingProvider;
	
	
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
				
		SupplychainBatch supplychainBatch = SupplychainBatch.all().filter("code = ?1", batchCode).fetchOne();
		
		if (supplychainBatch != null){
			switch (supplychainBatch.getActionSelect()) {
			
			case ISupplychainBatch.BATCH_INVOICING:
				return invoicing(supplychainBatch);
			
			default:
				throw new AxelorException(String.format("Action %s inconnu pour le traitement %s", supplychainBatch.getActionSelect(), batchCode), IException.INCONSISTENCY);
			}
		}
		else {
			throw new AxelorException(String.format("Batch %s inconnu", batchCode), IException.INCONSISTENCY);
		}
		
	}
	
	
	public Batch invoicing(SupplychainBatch supplychainBatch) {
		
		BatchStrategy strategy = invoicingProvider.get(); 
		return strategy.run(supplychainBatch);
		
	}
	
}
