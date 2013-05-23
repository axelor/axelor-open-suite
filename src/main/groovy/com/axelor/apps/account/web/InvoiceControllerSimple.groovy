package com.axelor.apps.account.web

import groovy.util.logging.Slf4j

import com.axelor.apps.AxelorSettings
import com.axelor.apps.account.db.IInvoice;
import com.axelor.apps.account.db.MoveLine
import com.axelor.apps.account.db.Invoice
import com.axelor.apps.tool.net.URLService
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse

@Slf4j
class InvoiceControllerSimple {
	
//	/**
//	 * Affiche les moveLines de la move remplie dans la facture
//	 * @param request
//	 * @param response
//	 */
//	 def void showMoveLine(ActionRequest request, ActionResponse response) {
//
//		Invoice invoice = request.context as Invoice
//		
//		if (invoice.move) {
//			
//			long id = invoice.move.id
//			 
//			if(invoice.oldMove)  {
//				id = invoice.oldMove.id
//			}
//		
//			response.view = [
//				title : "Lignes d'écriture : Facture ${invoice.invoiceId}",
//				resource : MoveLine.class.name,
//				domain : "self.move.id = ${id}"
//			]
//		}
//		else response.flash = "Aucune ligne d'écriture"
//		 
//	 }
	 
	  
	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	def void showInvoice(ActionRequest request, ActionResponse response) {

		Invoice invoice = request.context as Invoice

		StringBuilder url = new StringBuilder()			
		AxelorSettings axelorSettings = AxelorSettings.get()
		
		url.append("${axelorSettings.get('axelor.report.engine', '')}/frameset?__report=report/Invoice.rptdesign&__format=pdf&InvoiceId=${invoice.id}&__locale=fr_FR${axelorSettings.get('axelor.report.engine.datasource')}")

		log.debug("URL : {}", url)
		
		String urlNotExist = URLService.notExist(url.toString())
		if (urlNotExist == null){
		
			log.debug("Impression de la facture ${invoice.invoiceId} : ${url.toString()}")
			
			response.view = [
				"title": "Facture ${invoice.invoiceId}",
				"resource": url,
				"viewType": "html"
			]
		
		}
		else {
			response.flash = urlNotExist
		}
	}

}
