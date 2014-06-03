/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
