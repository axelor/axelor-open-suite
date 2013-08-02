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
	
	// WS
	
	/**
	 * Lancer le batch à travers un web service.
	 *
	 * @param request
	 * @param response
	 * @throws AxelorException 
	 */
	public void run(ActionRequest request, ActionResponse response) throws AxelorException{
	    
		//Batch batch = accountingBatchService.run( request.context["code"] as String )
		Batch batch = accountingBatchService.run(request.getContext().asType(String.class));
	    Map<String,Object> mapData = new HashMap<String,Object>();   
		mapData.put("anomaly", batch.getAnomaly());
		response.setData(mapData);	       
	}
}
