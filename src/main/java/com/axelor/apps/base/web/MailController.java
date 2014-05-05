/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
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
 * Software distributed under the License is distributed on an "AS IS"
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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.AppSettings;
import com.axelor.apps.AxelorSettings;
import com.axelor.apps.base.db.Mail;
import com.axelor.apps.base.db.MailModel;
import com.axelor.apps.tool.net.URLService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class MailController {

	private static final Logger LOG = LoggerFactory.getLogger(MailController.class);
	
	/**
	 * Fonction appeler par le bouton imprimer
	 * Permet d'ouvrir le mail généré au format pdf 
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void openMail(ActionRequest request, ActionResponse response) {

		Mail mail = request.getContext().asType(Mail.class);

		StringBuilder url = new StringBuilder();
			
		if(mail.getPdfFilePath() == null || mail.getPdfFilePath().isEmpty())  {
			response.setFlash("Aucun modèle de Courrier/Email de défini");
		}
		
		url.append(mail.getPdfFilePath());
		
		LOG.debug("URL : {}", url);
		
		String urlNotExist = URLService.notExist(url.toString());
		if (urlNotExist == null){
		
			LOG.debug("Impression du mail "+mail.getCode()+" : "+url.toString());
		
			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Courrier/Email "+mail.getCode());
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
	public void printMail(ActionRequest request, ActionResponse response) {

		Mail mail = request.getContext().asType(Mail.class);

		StringBuilder url = new StringBuilder();
		AppSettings appSettings = AppSettings.get();
		
		MailModel mailModel = mail.getMailModel();
		
		if(mailModel == null )  {
			response.setFlash("Aucun modèle de Courrier/Email de défini");
		}
		
		String pdfName = mailModel.getPdfModelPath();
		
		if(pdfName == null || pdfName.isEmpty())  {
			response.setFlash("Aucun modèle d'impression Birt de défini dans le modèle de Courrier/Email");
		}
		
		url.append(appSettings.get("axelor.report.engine", "")+"/frameset?__report=report/"+pdfName+"&__format=pdf&MailId="+mail.getId()+"&__locale=fr_FR"+AxelorSettings.getAxelorReportEngineDatasource());
		
		LOG.debug("URL : {}", url);
		
		String urlNotExist = URLService.notExist(url.toString());
		if (urlNotExist == null){
		
			LOG.debug("Impression du mail "+mail.getCode()+" : "+url.toString());
		
			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Courrier/Email "+mail.getCode());
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);	
		}
		else {
			response.setFlash(urlNotExist);
		}
	}
}
