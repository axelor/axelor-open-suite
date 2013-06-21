package com.axelor.apps.base.web

import com.axelor.apps.AxelorSettings
import com.axelor.apps.account.db.Invoice
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Product
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException
import com.axelor.apps.tool.net.URLService
import com.axelor.exception.service.TraceBackService
import com.axelor.exception.db.IException;
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.axelor.auth.db.User
import com.axelor.apps.tool.net.URLService
import groovy.util.logging.Slf4j
import com.google.inject.Inject;

@Slf4j
class ProductControllerSimple {
	
	
	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	def void printProductCatelog(ActionRequest request, ActionResponse response) {

		Product product = request.context as Product

		StringBuilder url = new StringBuilder()
		User user = request.context.get("__user__")
		
		def currentYear = "2013"
		AxelorSettings axelorSettings = AxelorSettings.get()
		String productIds = ""
		def lstSelectedPartner = request.context.get("_ids")
		lstSelectedPartner.each {
			productIds+= it+","
		}
		if(!productIds.equals("")){
			productIds = "&ProductIds="+productIds.substring(0, productIds.size()-1)	
		}
		print("Context.."+request.context)
		url.append("${axelorSettings.get('axelor.report.engine', '')}/frameset?__report=report/ProductCatalog_PGQL.rptdesign&__format=pdf${productIds}&UserId=${user.id}&CurrYear=${currentYear}&__locale=fr_FR${axelorSettings.get('axelor.report.engine.datasource')}")
		log.debug("URL : {}", url)
		String urlNotExist = URLService.notExist(url.toString())
		if (urlNotExist == null){
		
			log.debug("Impression des informations sur le partenaire Product Catelog ${currentYear}")
			
			response.view = [
				"title": "Product Catelog "+currentYear,
				"resource": url,
				"viewType": "html"
			]
		
		}
		else {
			response.flash = urlNotExist
		}
	}
	
}
