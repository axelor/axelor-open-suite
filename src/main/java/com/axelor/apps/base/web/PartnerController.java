/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.web;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportSettings;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.report.IReport;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.user.UserInfoService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class PartnerController {

	@Inject
	private SequenceService sequenceService;
	
	@Inject
	UserInfoService userInfoService;
	
	@Inject
	AddressService addressService;

	private static final Logger LOG = LoggerFactory.getLogger(PartnerController.class);

	public void setPartnerSequence(ActionRequest request, ActionResponse response) throws AxelorException {
		Partner partner = request.getContext().asType(Partner.class);
		partner = Partner.find(partner.getId());
		if(partner.getPartnerSeq() ==  null) {
			String seq = sequenceService.getSequenceNumber(IAdministration.PARTNER);
			if (seq == null)  
				throw new AxelorException("Aucune séquence configurée pour les tiers",
						IException.CONFIGURATION_ERROR);
			else
				response.setValue("partnerSeq", seq);
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
		User user = AuthUtils.getUser();

		StringBuilder url = new StringBuilder();
		
		String language = (partner.getLanguageSelect() == null || partner.getLanguageSelect().equals(""))? user != null? (user.getLanguage() == null || user.getLanguage().equals(""))? "en" : user.getLanguage() : "en" : partner.getLanguageSelect(); 
		
		url.append(new ReportSettings(IReport.PARTNER)
					.addParam("Locale", language)
					.addParam("__locale", "fr_FR")
					.addParam("PartnerId", partner.getId().toString())
					.getUrl());
		
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

		StringBuilder url = new StringBuilder();
		User user = AuthUtils.getUser();
		String language = user != null? (user.getLanguage() == null || user.getLanguage().equals(""))? "en" : user.getLanguage() : "en"; 
		
		url.append(new ReportSettings(IReport.PHONE_BOOK)
					.addParam("Locale", language)
					.addParam("__locale", "fr_FR")
					.addParam("UserId", user.getId().toString())
					.getUrl());
		
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
	
	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void printCompanyPhonebook(ActionRequest request, ActionResponse response) {

		StringBuilder url = new StringBuilder();

		User user = AuthUtils.getUser();
		String language = user != null? (user.getLanguage() == null || user.getLanguage().equals(""))? "en" : user.getLanguage() : "en"; 

		url.append(new ReportSettings(IReport.COMPANY_PHONE_BOOK)
					.addParam("Locale", language)
					.addParam("__locale", "fr_FR")
					.addParam("UserId", user.getId().toString())
					.getUrl());
		
		LOG.debug("URL : {}", url);
		String urlNotExist = URLService.notExist(url.toString());
		if (urlNotExist == null){

			LOG.debug("Impression des informations sur le partenaire Company PhoneBook");

			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Company PhoneBook");
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);		   
		}
		else {
			response.setFlash(urlNotExist);
		}
	}
	

	/* Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void printClientSituation(ActionRequest request, ActionResponse response) {

		Partner partner = request.getContext().asType(Partner.class);

		StringBuilder url = new StringBuilder();
		User user = AuthUtils.getUser();
		String language = (partner.getLanguageSelect() == null || partner.getLanguageSelect().equals(""))? user != null? (user.getLanguage() == null || user.getLanguage().equals(""))? "en" : user.getLanguage() : "en" : partner.getLanguageSelect(); 
		
		url.append(new ReportSettings(IReport.CLIENT_SITUATION)
		.addParam("Locale", language)
		.addParam("__locale", "fr_FR")
		.addParam("UserId", user.getId().toString())
		.addParam("PartnerId", partner.getId().toString())
		.getUrl());
		
		LOG.debug("URL : {}", url);
		String urlNotExist = URLService.notExist(url.toString());
		if (urlNotExist == null){

			LOG.debug("Impression de ma situation client");

			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Client Situation");
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);		   
		}
		else {
			response.setFlash(urlNotExist);
		}
	}
	
	public Set<Company> getActiveCompany(){
		Set<Company> companySet = new HashSet<Company>();
		companySet.add(userInfoService.getUserInfo().getActiveCompany());	
		return companySet;
	}
}