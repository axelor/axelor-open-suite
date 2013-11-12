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
package com.axelor.apps.account.web;

import java.util.HashMap;
import java.util.Map;

import com.axelor.apps.account.db.AccountingBatch;
import com.axelor.apps.account.db.IAccount;
import com.axelor.apps.account.service.batch.AccountingBatchService;
import com.axelor.apps.base.db.Batch;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class AccountingBatchController {

	@Inject
	private AccountingBatchService accountingBatchService;
	
	/**
	 * Lancer le batch de relance
	 *
	 * @param request
	 * @param response
	 */
	public void actionReminder(ActionRequest request, ActionResponse response){
		
		AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);
		
		Batch batch = null;
		
		if(accountingBatch.getReminderTypeSelect() == IAccount.REMINDER)  {
			batch = accountingBatchService.reminder(AccountingBatch.find(accountingBatch.getId()));
		}
		if(batch != null)
			response.setFlash(batch.getComment());
		response.setReload(true);
	}
	
	/**
	 * Lancer le batch de détermination des créances douteuses
	 *
	 * @param request
	 * @param response
	 */
	public void actionDoubtfulCustomer(ActionRequest request, ActionResponse response){
		
		AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);
		
		Batch batch = null;
		
		batch = accountingBatchService.doubtfulCustomer(AccountingBatch.find(accountingBatch.getId()));
		
		if(batch != null)
			response.setFlash(batch.getComment());
		response.setReload(true);
	}
	
	/**
	 * Lancer le batch de remboursement
	 *
	 * @param request
	 * @param response
	 */
	public void actionReimbursement(ActionRequest request, ActionResponse response){
		
		AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);
		
		Batch batch = null;
		
		if(accountingBatch.getReimbursementTypeSelect() == IAccount.BATCH_REIMBURSEMENT_EXPORT)  {
			batch = accountingBatchService.reimbursementExport(AccountingBatch.find(accountingBatch.getId()));
		}
		else if(accountingBatch.getReimbursementTypeSelect() == IAccount.BATCH_REIMBURSEMENT_IMPORT)  {
			batch = accountingBatchService.reimbursementImport(AccountingBatch.find(accountingBatch.getId()));
		}
		
		if(batch != null)
			response.setFlash(batch.getComment());
		response.setReload(true);
	}
	
	/**
	 * Lancer le batch de prélèvement
	 *
	 * @param request
	 * @param response
	 */
	public void actionDirectDebit(ActionRequest request, ActionResponse response){
		
		AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);
		
		Batch batch = null;
		
		if(accountingBatch.getDirectDebitTypeSelect() == IAccount.BATCH_DIRECT_DEBIT_EXPORT)  {
			batch = accountingBatchService.paymentScheduleExport(AccountingBatch.find(accountingBatch.getId()));
		}
		else if(accountingBatch.getDirectDebitTypeSelect() == IAccount.BATCH_DIRECT_DEBIT_IMPORT)  {
			batch = accountingBatchService.paymentScheduleImport(AccountingBatch.find(accountingBatch.getId()));
		}
		
		if(batch != null)
			response.setFlash(batch.getComment());
		response.setReload(true);
	}
	
	/**
	 * Lancer le batch de prélèvement
	 *
	 * @param request
	 * @param response
	 */
	public void actionInterbankPaymentOrder(ActionRequest request, ActionResponse response){
		
		AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);
		
		Batch batch = null;
		
		if(accountingBatch.getInterbankPaymentOrderTypeSelect() == IAccount.INTERBANK_PAYMENT_ORDER_IMPORT)  {
			batch = accountingBatchService.interbankPaymentOrderImport(AccountingBatch.find(accountingBatch.getId()));
		}
		else if(accountingBatch.getInterbankPaymentOrderTypeSelect() == IAccount.INTERBANK_PAYMENT_ORDER_REJECT_IMPORT)  {
			batch = accountingBatchService.interbankPaymentOrderRejectImport(AccountingBatch.find(accountingBatch.getId()));
		}
		
		if(batch != null)
			response.setFlash(batch.getComment());
		response.setReload(true);
	}
	
	
	/**
	 * Lancer le batch de calcul du compte client
	 *
	 * @param request
	 * @param response
	 */
	public void actionAccountingCustomer(ActionRequest request, ActionResponse response){

		AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);
		
		Batch batch = null;

		batch = accountingBatchService.accountCustomer(AccountingBatch.find(accountingBatch.getId()));

		if(batch != null)
			response.setFlash(batch.getComment());
		response.setReload(true);

	}



	/**
	 * Lancer le batch de calcul du compte client
	 *
	 * @param request
	 * @param response
	 */
	public void actionMoveLineExport(ActionRequest request, ActionResponse response){

		AccountingBatch accountingBatch = request.getContext().asType(AccountingBatch.class);
		
		Batch batch = null;

		batch = accountingBatchService.moveLineExport(AccountingBatch.find(accountingBatch.getId()));

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
	    
		Batch batch = accountingBatchService.run((String) request.getContext().get("code"));
	    Map<String,Object> mapData = new HashMap<String,Object>();   
		mapData.put("anomaly", batch.getAnomaly());
		response.setData(mapData);	       
	}
}
