package com.axelor.apps.account.web;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.AxelorSettings;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.tool.net.URLService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class InvoiceControllerSimple {

	private static final Logger LOG = LoggerFactory.getLogger(InvoiceControllerSimple.class);
	
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
	public void showInvoice(ActionRequest request, ActionResponse response) {

		Invoice invoice = request.getContext().asType(Invoice.class);

		StringBuilder url = new StringBuilder();			
		AxelorSettings axelorSettings = AxelorSettings.get();
		url.append(axelorSettings.get("axelor.report.engine", "")+"/frameset?__report=report/Invoice.rptdesign&__format=pdf&InvoiceId="+invoice.getId()+"&__locale=fr_FR"+axelorSettings.get("axelor.report.engine.datasource"));
		//url.append("${axelorSettings.get('axelor.report.engine', '')}/frameset?__report=report/Invoice.rptdesign&__format=pdf&InvoiceId=${invoice.id}&__locale=fr_FR${axelorSettings.get('axelor.report.engine.datasource')}")

		LOG.debug("URL : {}", url);
		
		String urlNotExist = URLService.notExist(url.toString());
		if (urlNotExist == null){
		
			LOG.debug("Impression de la facture "+invoice.getInvoiceId()+" : "+url.toString());
			
			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Facture "+invoice.getInvoiceId());
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);		
		}
		else {
			response.setFlash(urlNotExist);
		}
	}
}
