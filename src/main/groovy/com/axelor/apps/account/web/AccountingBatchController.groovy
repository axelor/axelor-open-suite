package com.axelor.apps.account.web

import com.axelor.apps.account.db.AccountingBatch
import com.axelor.apps.account.service.generator.batch.AccountingBatchService
import com.axelor.apps.base.db.Batch
import com.axelor.meta.views.Action.Context
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject

class AccountingBatchController {
	@Inject
	private AccountingBatchService accountingBatchService
	
	/**
	 * Lancer le batch Agence en ligne.
	 *
	 * @param request
	 * @param response
	 */
	def void actionOnlineAgency(ActionRequest request, ActionResponse response){
		
		AccountingBatch accountingBatch = request.context as AccountingBatch
		
		Batch batch = null;
		
		if(accountingBatch.getOnlineAgencyTypeSelect() == IAccount.ONLINE_AGENCY_EXPORT)  {
			batch = accountingBatchService.onlineAgencyExport(AccountingBatch.find(accountingBatch.id))
		}
		else if(accountingBatch.getOnlineAgencyTypeSelect() == IAccount.ONLINE_AGENCY_IMPORT)  {
			batch = accountingBatchService.onlineAgencyImport(AccountingBatch.find(accountingBatch.id))
		}
		
		response.flash = "${batch?.comment}"
		response.reload = true
		
	}
	
	
	/**
	 * Lancer le batch de relance
	 *
	 * @param request
	 * @param response
	 */
	def void actionReminder(ActionRequest request, ActionResponse response){
		
		AccountingBatch accountingBatch = request.context as AccountingBatch
		
		Batch batch = null;
		
		if(accountingBatch.getReminderTypeSelect() == IDebtRecovery.REMINDER)  {
			batch = accountingBatchService.reminder(AccountingBatch.find(accountingBatch.id))
		}
		
		response.flash = "${batch?.comment}"
		response.reload = true
		
	}
	
	/**
	 * Lancer le batch de détermination des créances douteuses
	 *
	 * @param request
	 * @param response
	 */
	def void actionDoubtfulCustomer(ActionRequest request, ActionResponse response){
		
		AccountingBatch accountingBatch = request.context as AccountingBatch
		
		Batch batch = null;
		
		batch = accountingBatchService.doubtfulCustomer(AccountingBatch.find(accountingBatch.id))
		
		response.flash = "${batch?.comment}"
		response.reload = true
		
	}
	
	/**
	 * Lancer le batch de remboursement
	 *
	 * @param request
	 * @param response
	 */
	def void actionReimbursement(ActionRequest request, ActionResponse response){
		
		AccountingBatch accountingBatch = request.context as AccountingBatch
		
		Batch batch = null;
		
		if(accountingBatch.getReimbursementTypeSelect() == IAccount.BATCH_REIMBURSEMENT_EXPORT)  {
			batch = accountingBatchService.reimbursementExport(AccountingBatch.find(accountingBatch.id));
		}
		else if(accountingBatch.getReimbursementTypeSelect() == IAccount.BATCH_REIMBURSEMENT_IMPORT)  {
			batch = accountingBatchService.reimbursementImport(AccountingBatch.find(accountingBatch.id));
		}
		
		response.flash = "${batch?.comment}"
		response.reload = true
		
	}
	
	
	
	/**
	 * Lancer le batch de prélèvement
	 *
	 * @param request
	 * @param response
	 */
	def void actionDirectDebit(ActionRequest request, ActionResponse response){
		
		AccountingBatch accountingBatch = request.context as AccountingBatch
		
		Batch batch = null;
		
		if(accountingBatch.getDirectDebitTypeSelect() == IAccount.BATCH_DIRECT_DEBIT_EXPORT)  {
			batch = accountingBatchService.paymentScheduleExport(AccountingBatch.find(accountingBatch.id));
		}
		else if(accountingBatch.getDirectDebitTypeSelect() == IAccount.BATCH_DIRECT_DEBIT_IMPORT)  {
			batch = accountingBatchService.paymentScheduleImport(AccountingBatch.find(accountingBatch.id));
		}
		
		response.flash = "${batch?.comment}"
		response.reload = true
		
	}
	
	
	/**
	 * Lancer le batch de prélèvement
	 *
	 * @param request
	 * @param response
	 */
	def void actionInterbankPaymentOrder(ActionRequest request, ActionResponse response){
		
		AccountingBatch accountingBatch = request.context as AccountingBatch
		
		Batch batch = null;
		
		if(accountingBatch.getInterbankPaymentOrderTypeSelect() == IAccount.INTERBANK_PAYMENT_ORDER_IMPORT)  {
			batch = accountingBatchService.interbankPaymentOrderImport(AccountingBatch.find(accountingBatch.id));
		}
		else if(accountingBatch.getInterbankPaymentOrderTypeSelect() == IAccount.INTERBANK_PAYMENT_ORDER_REJECT_IMPORT)  {
			batch = accountingBatchService.interbankPaymentOrderRejectImport(AccountingBatch.find(accountingBatch.id));
		}
		
		response.flash = "${batch?.comment}"
		response.reload = true
		
	}
	
	
	
	// WS
	
	/**
	 * Lancer le batch à travers un web service.
	 *
	 * @param request
	 * @param response
	 */
	def void run(ActionRequest request, ActionResponse response){
		
	   Context context = request.context
			   
	   Batch batch = accountingBatchService.run(context.code)
	   response.data = [
		   "anomaly":batch.anomaly
	   ]
				
	}
	
}
