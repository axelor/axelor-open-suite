/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
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
 * Software distributed under the License is distributed on an "AS IS"
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
package com.axelor.apps.account.web;

import java.util.HashMap;
import java.util.Map;

import com.axelor.apps.account.db.InvoiceBatch;
import com.axelor.apps.account.service.invoice.InvoiceBatchService;
import com.axelor.apps.account.service.invoice.generator.batch.BatchWkf;
import com.axelor.apps.base.db.Batch;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;

public class InvoiceBatchController {

	@Inject
	private InvoiceBatchService invoiceBatchService;
	
	/**
	 * Lancer le batch de mise à jour de statut.
	 * 
	 * @param request
	 * @param response
	 * @throws AxelorException 
	 */
	public void actionStatus(ActionRequest request, ActionResponse response) throws AxelorException{
		
		InvoiceBatch invoiceBatch = request.getContext().asType(InvoiceBatch.class);
		
		Batch batch = invoiceBatchService.wkf(InvoiceBatch.find(invoiceBatch.getId()));
		
		response.setFlash(batch.getComment());
		response.setReload(true);
	}
	
	/**
	  * Lancer le batch à travers un web service.
	  *
	  * @param request
	  * @param response
	 * @throws AxelorException 
	  */
	public void run(ActionRequest request, ActionResponse response) throws AxelorException{
		 
		Context context = request.getContext();
				
		Batch batch = invoiceBatchService.run((String) context.get("code"));
		
		Map<String,Object> mapData = new HashMap<String,Object>();
		mapData.put("anomaly", batch.getAnomaly());
		response.setData(mapData);				 
	 }
	
	/**
	  * Appliquer le domaine à la liste de facture à ventiler ou valider.
	  * 
	  * @param request
	  * @param response
	  */
	public void invoiceSetDomain(ActionRequest request, ActionResponse response){
		
		InvoiceBatch invoiceBatch = request.getContext().asType(InvoiceBatch.class);
		 
		 switch (invoiceBatch.getActionSelect()) {
		 case 1:
			response.setAttr("invoiceSet", "domain", BatchWkf.invoiceQuery(invoiceBatch, true));
			break;
		 default:
			 response.setAttr("invoiceSet", "domain", BatchWkf.invoiceQuery(invoiceBatch, false));
			break;
		 }
	 }
}
