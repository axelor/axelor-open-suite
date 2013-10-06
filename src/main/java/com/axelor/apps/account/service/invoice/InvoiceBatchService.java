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
package com.axelor.apps.account.service.invoice;

import javax.inject.Inject;
import javax.inject.Provider;

import com.axelor.apps.account.db.IInvoice;
import com.axelor.apps.account.db.InvoiceBatch;
import com.axelor.apps.account.service.invoice.generator.batch.BatchStrategy;
import com.axelor.apps.account.service.invoice.generator.batch.BatchValidation;
import com.axelor.apps.account.service.invoice.generator.batch.BatchVentilation;
import com.axelor.apps.base.db.Batch;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;

/**
 * InvoiceBatchService est une classe implémentant l'ensemble des batchs de
 * facturations.
 */
public class InvoiceBatchService {

	@Inject
	private Provider<BatchValidation> validationProvider;
	
	@Inject
	private Provider<BatchVentilation> ventilationProvider;
	

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
		InvoiceBatch invoiceBatch = InvoiceBatch.all().filter("code = ?1", batchCode).fetchOne();
		
		if (invoiceBatch != null){
			switch (invoiceBatch.getActionSelect()) {
			case IInvoice.BATCH_STATUS:
				batch = wkf(invoiceBatch);
				break;
			default:
				throw new AxelorException(String.format("Action %s inconnu pour le traitement %s", invoiceBatch.getActionSelect(), batchCode), IException.INCONSISTENCY);
			}
		}
		else {
			throw new AxelorException(String.format("Batch %s inconnu", batchCode), IException.INCONSISTENCY);
		}
		
		return batch;
	}
	
	
	
	public Batch wkf(InvoiceBatch invoiceBatch) throws AxelorException{
		
		BatchStrategy strategy = null;
		
		if (invoiceBatch.getToStatusSelect().equals(IInvoice.TO_VAL)) { 
			strategy = validationProvider.get(); 
		}
		else if (invoiceBatch.getToStatusSelect().equals(IInvoice.TO_DIS)) { 
			strategy = ventilationProvider.get();
		}
		else {
			throw new AxelorException(String.format("Liste de statuts %s inconnu pour le traitement %s", invoiceBatch.getToStatusSelect(), invoiceBatch.getCode()), IException.INCONSISTENCY);
		}

		return strategy.run(invoiceBatch);
		
	}
	
}
