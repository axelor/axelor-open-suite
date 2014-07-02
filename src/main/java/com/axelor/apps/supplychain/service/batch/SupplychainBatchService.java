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
				
		SupplychainBatch supplychainBatch = SupplychainBatch.findByCode(batchCode);
		
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
