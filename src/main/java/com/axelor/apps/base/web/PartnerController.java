/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.base.web;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.AxelorSettings;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.user.UserInfoService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.meta.db.MetaUser;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class PartnerController {

	@Inject
	private SequenceService sequenceService;
	
	@Inject
	UserInfoService userInfoService;

	private static final Logger LOG = LoggerFactory.getLogger(PartnerController.class);

	public void setPartnerSequence(ActionRequest request, ActionResponse response) throws AxelorException {
		Partner partner = request.getContext().asType(Partner.class);
		partner = Partner.find(partner.getId());
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
		MetaUser metaUser = MetaUser.findByUser( AuthUtils.getUser());

		StringBuilder url = new StringBuilder();
		AxelorSettings axelorSettings = AxelorSettings.get();
		String language = (partner.getLanguageSelect() == null || partner.getLanguageSelect().equals(""))? metaUser != null? (metaUser.getLanguage() == null || metaUser.getLanguage().equals(""))? "en" : metaUser.getLanguage() : "en" : partner.getLanguageSelect(); 
		url.append(axelorSettings.get("axelor.report.engine", "")+"/frameset?__report=report/Partner.rptdesign&__format=pdf&Locale="+language+"&PartnerId="+partner.getId()+"&__locale=fr_FR"+axelorSettings.get("axelor.report.engine.datasource"));
		
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
		User user = AuthUtils.getUser();
		MetaUser metaUser = MetaUser.findByUser(user);
		String language = metaUser != null? (metaUser.getLanguage() == null || metaUser.getLanguage().equals(""))? "en" : metaUser.getLanguage() : "en"; 
		url.append(axelorSettings.get("axelor.report.engine", "")+"/frameset?__report=report/PhoneBook.rptdesign&__format=html&Locale="+language+"&UserId="+user.getId()+"&__locale=fr_FR"+axelorSettings.get("axelor.report.engine.datasource"));
		
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

		AxelorSettings axelorSettings = AxelorSettings.get();

		StringBuilder url = new StringBuilder();

		User user = AuthUtils.getUser();
		MetaUser metaUser = MetaUser.findByUser(user);
		String language = metaUser != null? (metaUser.getLanguage() == null || metaUser.getLanguage().equals(""))? "en" : metaUser.getLanguage() : "en"; 

		url.append(axelorSettings.get("axelor.report.engine", "")+"/frameset?__report=report/CompanyPhoneBook.rptdesign&__format=html&Locale="+language+"&UserId="+user.getId()+"&__locale=fr_FR"+axelorSettings.get("axelor.report.engine.datasource"));

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

		AxelorSettings axelorSettings = AxelorSettings.get();
		Partner partner = request.getContext().asType(Partner.class);

		StringBuilder url = new StringBuilder();
		User user = AuthUtils.getUser();
		MetaUser metaUser = MetaUser.findByUser(user);
		String language = (partner.getLanguageSelect() == null || partner.getLanguageSelect().equals(""))? metaUser != null? (metaUser.getLanguage() == null || metaUser.getLanguage().equals(""))? "en" : metaUser.getLanguage() : "en" : partner.getLanguageSelect(); 
		url.append(axelorSettings.get("axelor.report.engine", "")+"/frameset?__report=report/ClientSituation.rptdesign&__format=pdf&PartnerId="+partner.getId()+"&Locale="+language+"&UserId="+user.getId()+"&__locale=fr_FR"+axelorSettings.get("axelor.report.engine.datasource"));
		
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
