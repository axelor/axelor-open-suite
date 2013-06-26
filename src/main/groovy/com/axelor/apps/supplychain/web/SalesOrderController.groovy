package com.axelor.apps.supplychain.web

import groovy.util.logging.Slf4j

import com.axelor.apps.AxelorSettings
import com.axelor.apps.supplychain.db.SalesOrder
import com.axelor.apps.supplychain.service.SalesOrderService
import com.axelor.exception.AxelorException
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService
import com.axelor.apps.tool.net.URLService

import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import com.google.inject.Inject

import com.axelor.auth.db.User
import com.axelor.apps.googleapps.db.GoogleFile
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.UserInfo
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.googleapps.document.DocumentService
import com.axelor.googleapps.userutils.Utils


@Slf4j
class SalesOrderController {
	
	@Inject
	private SalesOrderService salesOrderService
	
	@Inject
	SequenceService sequenceService;
	
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
	   UserInfo currentUserInfo = UserInfo.all().filter("self.internalUser = ?1", currentUser).fetchOne();
	   
	   GoogleFile documentData = documentSeriveObj.createDocumentWithTemplate(currentUserInfo,dataObject);
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
	
	def void setSequence(ActionRequest request, ActionResponse response) {
		SalesOrder salesOrder = request.context as SalesOrder
		Map<String,String> values = new HashMap<String,String>();
		if(salesOrder.salesOrderSeq ==  null){
			def ref = sequenceService.getSequence(IAdministration.SALES_ORDER,salesOrder.company,false);
			if (ref == null || ref.isEmpty())
				throw new AxelorException(String.format("La société %s n'a pas de séquence de configurée pour les devis",salesOrder.company?.name),
								IException.CONFIGURATION_ERROR);
			else
				values.put("salesOrderSeq",ref);
		}
		response.setValues(values);
	}
	
	def void createTaskByLines(ActionRequest request, ActionResponse response) {
		
		SalesOrder salesOrder = request.context as SalesOrder
		
		if(salesOrder) {
			
			salesOrderService.createTasks(salesOrder)
		}
	}
}
