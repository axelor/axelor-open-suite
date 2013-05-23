package com.axelor.apps.account.web

import com.axelor.apps.account.db.InvoiceBatch
import com.axelor.apps.account.service.invoice.InvoiceBatchService
import com.axelor.apps.base.db.Batch
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.axelor.rpc.Context
import com.google.inject.Inject

class InvoiceBatchController {
	
	@Inject
	private InvoiceBatchService invoiceBatchService
	
// Action Method
	
	/**
	 * Lancer le batch de facturation.
	 * 
	 * @param request
	 * @param response
	 */
	def void actionInvoice(ActionRequest request, ActionResponse response){
		
		InvoiceBatch invoiceBatch = request.context as InvoiceBatch
		
		Batch batch = invoiceBatchService.creates(InvoiceBatch.find(invoiceBatch.id))
		
		response.flash = "${batch?.comment}"
		response.reload = true
		
	}
	
	/**
	 * Lancer le batch de mise à jour de statut.
	 * 
	 * @param request
	 * @param response
	 */
	def void actionStatus(ActionRequest request, ActionResponse response){
		
		InvoiceBatch invoiceBatch = request.context as InvoiceBatch
		
		Batch batch = invoiceBatchService.wkf(InvoiceBatch.find(invoiceBatch.id))
		
		response.flash = "${batch?.comment}"
		response.reload = true
		
	}
	
	/**
	 * Lancer le batch de mise à jour de statut.
	 * 
	 * @param request
	 * @param response
	 */
	def void actionAlarm(ActionRequest request, ActionResponse response){
		
		InvoiceBatch invoiceBatch = request.context as InvoiceBatch
		
		Batch batch = invoiceBatchService.alarms(InvoiceBatch.find(invoiceBatch.id))
		
		response.flash = "${batch?.comment}"
		response.reload = true
		
	}
	
	
	/**
	 * Lancer le batch de facturation des mémoires
	 *
	 * @param request
	 * @param response
	 */
	def void actionMemory(ActionRequest request, ActionResponse response){
		
		InvoiceBatch invoiceBatch = request.context as InvoiceBatch
		
		Batch batch = invoiceBatchService.memory(InvoiceBatch.find(invoiceBatch.id))
		
		response.flash = "${batch?.comment}"
		response.reload = true
		
	}
	
	/**
	 * Lancer le batch de confirmation des mémoires bordereau
	 *
	 * @param request
	 * @param response
	 */
	def void actionConfirmMemory(ActionRequest request, ActionResponse response){
		
		InvoiceBatch invoiceBatch = request.context as InvoiceBatch
		
		Batch batch = invoiceBatchService.confirmMemory(InvoiceBatch.find(invoiceBatch.id))
		
		response.flash = "${batch?.comment}"
		response.reload = true
		
	}

// Action Attrs
	 
	 /**
	  * Appliquer le domaine à la liste de facture à ventiler ou valider.
	  * 
	  * @param request
	  * @param response
	  */
	 def void invoiceSetDomain(ActionRequest request, ActionResponse response){
		
		InvoiceBatch invoiceBatch = request.context as InvoiceBatch
		 
		 switch (invoiceBatch.actionSelect) {
		 case 1:
			response.attrs = [
			 "invoiceSet": ["domain": BatchWkf.invoiceQuery(invoiceBatch, true)]
			]
			break;

		 default:
			response.attrs = [
			 "invoiceSet": ["domain": BatchWkf.invoiceQuery(invoiceBatch, false)]
			]
			break;
		 }
		 
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
				
		Batch batch = invoiceBatchService.run(context.code)
		response.data = [
			"anomaly":batch.anomaly
		]
				 
	 }
}

