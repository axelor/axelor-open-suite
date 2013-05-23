package com.axelor.apps.base.web

import groovy.util.logging.Slf4j

import javax.inject.Provider

import com.axelor.apps.account.service.invoice.InvoiceService
import com.axelor.apps.base.db.Alarm
import com.axelor.exception.service.TraceBackService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject

@Slf4j
class AlarmController {
   
	@Inject
	private Provider<InvoiceService> invoiceServiceProvider
	
	/**
	 * Fonction appeler par le bouton lever alarmes.
	 * 
	 * @param request
	 * @param response
	 */
	def void acquitAlarms(ActionRequest request, ActionResponse response) {

		Alarm alarm = request.context as Alarm
		alarm = Alarm.find(alarm.id)
		
		if ( alarm.acquitOk && Alarm.all().filter("self.invoice = ?1 AND self.acquitOk = false", alarm.invoice).count() == 0 && alarm.invoice?.status?.code == "ala") {
			
			try { 
				invoiceServiceProvider.get().validate(alarm.invoice)
				response.flash = "Facture ${alarm.invoice.invoiceName} validée"
			}
			catch(Exception e)  { TraceBackService.trace(response, e) }
		}
		
	}
}
