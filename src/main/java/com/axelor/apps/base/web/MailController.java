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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportSettings;
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
		
		MailModel mailModel = mail.getMailModel();
		
		if(mailModel == null )  {
			response.setFlash("Aucun modèle de Courrier/Email de défini");
		}
		
		String pdfName = mailModel.getPdfModelPath();
		
		if(pdfName == null || pdfName.isEmpty())  {
			response.setFlash("Aucun modèle d'impression Birt de défini dans le modèle de Courrier/Email");
		}
		
		url.append(new ReportSettings(pdfName)
					.addParam("__locale", "fr_FR")
					.addParam("MailId", mail.getId().toString())
					.getUrl());
		
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
