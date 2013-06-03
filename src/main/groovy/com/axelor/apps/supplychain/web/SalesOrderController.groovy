package com.axelor.apps.supplychain.web

import groovy.util.logging.Slf4j

import com.axelor.apps.AxelorSettings
import com.axelor.apps.supplychain.db.SalesOrder
import com.axelor.apps.supplychain.service.SalesOrderService
import com.axelor.exception.service.TraceBackService
import com.axelor.apps.tool.net.URLService

import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject

import com.axelor.auth.db.User
import com.axelor.apps.base.db.GoogleFile
import com.axelor.googleapps.document.DocumentService
import com.axelor.googleapps.userutils.Utils


@Slf4j
class SalesOrderController {
	
	@Inject
	private SalesOrderService salesOrderService
	
	@Inject DocumentService documentSeriveObj
	@Inject Utils userUtils
	/**
	* saves the document for any type of entity using template
	* @param request
	* @param response
	*/
   void saveDocumentForOrder(ActionRequest request,ActionResponse response){
	   
	   userUtils.validAppsConfig(request, response)
	   
	   // in this line change the Class as per the Module requirement i.e SalesOrder class here used
	   SalesOrder dataObject = request.context as SalesOrder
	   User currentUser=request.context.get("__user__")
	   
	   GoogleFile documentData = documentSeriveObj.createDocumentWithTemplate(currentUser,dataObject);
	   if(documentData == null){
			   response.flash = "The Document Can't be created because the template for this type of Entity not Found..!"
			return
	   }
	   response.flash = "Document Created in Your Root Directory"
   }

	
	def void compute(ActionRequest request, ActionResponse response)  {
		
		SalesOrder salesOrder = request.context as SalesOrder

		try {
			
			salesOrderService.computeSalesOrder(salesOrder)
			response.reload = true
			response.flash = "Montant du devis : ${salesOrder.inTaxTotal} TTC"
			
		}
		catch(Exception e)  { TraceBackService.trace(response, e) }
		
	}
	
	
	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	def void showSalesOrder(ActionRequest request, ActionResponse response) {

		SalesOrder salesOrder = request.context as SalesOrder

		StringBuilder url = new StringBuilder()
		AxelorSettings axelorSettings = AxelorSettings.get()
		
		url.append("${axelorSettings.get('axelor.report.engine', '')}/frameset?__report=report/SalesOrder.rptdesign&__format=pdf&SalesOrderId=${salesOrder.id}&__locale=fr_FR${axelorSettings.get('axelor.report.engine.datasource')}")

		log.debug("URL : {}", url)
		
		String urlNotExist = URLService.notExist(url.toString())
		if (urlNotExist == null){
		
			log.debug("Impression du devis ${salesOrder.salesOrderSeq} : ${url.toString()}")
			
			response.view = [
				"title": "Devis ${salesOrder.salesOrderSeq}",
				"resource": url,
				"viewType": "html"
			]
		
		}
		else {
			response.flash = urlNotExist
		}
	}
}
