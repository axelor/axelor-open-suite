package com.axelor.apps.base.web

import java.util.List;

import com.axelor.apps.AxelorSettings
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.Invoice
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Partner
import com.axelor.apps.base.service.PartnerService
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException
import com.axelor.apps.tool.net.URLService
import com.axelor.exception.service.TraceBackService
import com.axelor.exception.db.IException;
import com.axelor.rpc.ActionRequest
import com.axelor.rpc.ActionResponse
import groovy.util.logging.Slf4j
import com.axelor.auth.db.User
import com.google.inject.Inject;

@Slf4j
class PartnerControllerSimple {
	
	@Inject
	SequenceService sequenceService;
	
	@Inject
	PartnerService partnerService;
	
	void setPartnerSequence(ActionRequest request, ActionResponse response) {
		Partner partner = request.context as Partner
		Map<String,String> values = new HashMap<String,String>();
		if(partner.partnerSeq ==  null){
			def ref = sequenceService.getSequence(IAdministration.PARTNER,false);
			if (ref == null)  
				throw new AxelorException("Aucune séquence configurée pour les tiers",
								IException.CONFIGURATION_ERROR);
			else
				values.put("partnerSeq",ref);
		}
		response.setValues(values);
	}
	
	
	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	def void showPartnerInfo(ActionRequest request, ActionResponse response) {

		Partner partner = request.context as Partner

		StringBuilder url = new StringBuilder()
		AxelorSettings axelorSettings = AxelorSettings.get()
		
		url.append("${axelorSettings.get('axelor.report.engine', '')}/frameset?__report=report/Partner.rptdesign&__format=pdf&PartnerId=${partner.id}&__locale=fr_FR${axelorSettings.get('axelor.report.engine.datasource')}")

		log.debug("URL : {}", url)
		
		String urlNotExist = URLService.notExist(url.toString())
		if (urlNotExist == null){
		
			log.debug("Impression des informations sur le partenaire ${partner.partnerSeq} : ${url.toString()}")
			
			response.view = [
				"title": "Partenaire ${partner.partnerSeq}",
				"resource": url,
				"viewType": "html"
			]
		
		}
		else {
			response.flash = urlNotExist
		}
	}
	
	/**
	* Fonction appeler par le bouton imprimer
	*
	* @param request
	* @param response
	* @return
	*/
   def void printContactPhonebook(ActionRequest request, ActionResponse response) {

	   AxelorSettings axelorSettings = AxelorSettings.get()
	   
	   StringBuilder url = new StringBuilder()
	   User user = request.context.get("__user__")
	   
	   url.append("${axelorSettings.get('axelor.report.engine', '')}/frameset?__report=report/PhoneBook.rptdesign&__format=html&UserId=${user.id}&__locale=fr_FR${axelorSettings.get('axelor.report.engine.datasource')}")
	   log.debug("URL : {}", url)
	   String urlNotExist = URLService.notExist(url.toString())
	   if (urlNotExist == null){
	   
		   log.debug("Impression des informations sur le partenaire Employee PhoneBook")
		   
		   response.view = [
			   "title": "Phone Book",
			   "resource": url,
			   "viewType": "html"
		   ]
	   
	   }
	   else {
		   response.flash = urlNotExist
	   }
   }

	
	def void createAccountingSituations(ActionRequest request, ActionResponse response) {
		
		Partner partner = request.context as Partner
		
		if(partner) {
			List<AccountingSituation> accountingSituationList = partnerService.createAccountingSituation(partner)
			
			if(accountingSituationList) {
				
				response.values = ["accountingSituationList": accountingSituationList]
			}
		}
	}
}
