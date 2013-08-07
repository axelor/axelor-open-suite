package com.axelor.apps.base.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.AxelorSettings;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class PartnerControllerSimple {

	@Inject
	SequenceService sequenceService;

	@Inject
	PartnerService partnerService;

	private static final Logger LOG = LoggerFactory.getLogger(PartnerControllerSimple.class);

	public void setPartnerSequence(ActionRequest request, ActionResponse response) throws AxelorException {
		Partner partner = request.getContext().asType(Partner.class);
		if(partner.getPartnerSeq() ==  null) {
			String ref = sequenceService.getSequence(IAdministration.PARTNER,false);
			if (ref == null)  
				throw new AxelorException("Aucune séquence configurée pour les tiers",
						IException.CONFIGURATION_ERROR);
			else
				response.setValue("partnerSeq", ref);
		}
	}

	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void showPartnerInfo(ActionRequest request, ActionResponse response) {

		Partner partner = request.getContext().asType(Partner.class);

		StringBuilder url = new StringBuilder();
		AxelorSettings axelorSettings = AxelorSettings.get();

		url.append(axelorSettings.get("axelor.report.engine", "")+"/frameset?__report=report/Partner.rptdesign&__format=pdf&PartnerId="+partner.getId()+"&__locale=fr_FR"+axelorSettings.get("axelor.report.engine.datasource"));

		LOG.debug("URL : {}", url);

		String urlNotExist = URLService.notExist(url.toString());
		if (urlNotExist == null){

			LOG.debug("Impression des informations sur le partenaire "+partner.getPartnerSeq()+" : "+url.toString());

			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Partenaire "+partner.getPartnerSeq());
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);	
		}
		else {
			response.setFlash(urlNotExist);
		}
	}

	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void printContactPhonebook(ActionRequest request, ActionResponse response) {

		AxelorSettings axelorSettings = AxelorSettings.get();

		StringBuilder url = new StringBuilder();
		User user = (User) request.getContext().get("__user__");

		url.append(axelorSettings.get("axelor.report.engine", "")+"/frameset?__report=report/PhoneBook.rptdesign&__format=html&UserId="+user.getId()+"&__locale=fr_FR"+axelorSettings.get("axelor.report.engine.datasource"));

		LOG.debug("URL : {}", url);
		String urlNotExist = URLService.notExist(url.toString());
		if (urlNotExist == null){

			LOG.debug("Impression des informations sur le partenaire Employee PhoneBook");

			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Phone Book");
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);		   
		}
		else {
			response.setFlash(urlNotExist);
		}
	}
	
	public void createAccountingSituations(ActionRequest request, ActionResponse response) {
		
		Partner partner = request.getContext().asType(Partner.class);
		
		if(partner != null) {
			List<AccountingSituation> accountingSituationList = partnerService.createAccountingSituation(partner);
			
			if(accountingSituationList != null) {
				response.setValue("accountingSituationList", accountingSituationList);
			}
		}
	}
}
