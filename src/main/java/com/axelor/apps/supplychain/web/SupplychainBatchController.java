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
package com.axelor.apps.supplychain.web;

import java.util.HashMap;
import java.util.Map;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.supplychain.db.SupplychainBatch;
import com.axelor.apps.supplychain.service.batch.SupplychainBatchService;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class SupplychainBatchController {

	@Inject
	private SupplychainBatchService supplychainBatchService;
	
	/**
	 * Lancer le batch de facturation des devis
	 *
	 * @param request
	 * @param response
	 */
	public void actionInvoicing(ActionRequest request, ActionResponse response){
		
		SupplychainBatch supplychainBatch = request.getContext().asType(SupplychainBatch.class);
		
		Batch batch = supplychainBatchService.invoicing(SupplychainBatch.find(supplychainBatch.getId()));
		
		if(batch != null)
			response.setFlash(batch.getComment());
		response.setReload(true);
	}
	
	// WS
	
	/**
	 * Lancer le batch à travers un web service.
	 *
	 * @param request
	 * @param response
	 * @throws AxelorException 
	 */
	public void run(ActionRequest request, ActionResponse response) throws AxelorException{
	    
		Batch batch = supplychainBatchService.run((String) request.getContext().get("code"));
	    Map<String,Object> mapData = new HashMap<String,Object>();   
		mapData.put("anomaly", batch.getAnomaly());
		response.setData(mapData);	       
	}
}
