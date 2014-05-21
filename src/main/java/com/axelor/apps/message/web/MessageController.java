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
package com.axelor.apps.message.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportSettings;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.report.IReport;
import com.axelor.apps.message.db.IMessage;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class MessageController {

	@Inject
	private Provider<MessageService> messageService;
	
	
	private static final Logger LOG = LoggerFactory.getLogger(MessageController.class);
	
	
	public void sendByEmail(ActionRequest request, ActionResponse response) {
		
		Message message = request.getContext().asType(Message.class);
		
		message = messageService.get().sendMessageByEmail(Message.find(message.getId()));
		
		response.setReload(true);
		
		if(message.getStatusSelect() == IMessage.STATUS_SENT)  {
			if(message.getSentByEmail())  {
				response.setFlash("Email envoyé");
			}
			else  {
				response.setFlash("Message envoyé");
			}
			
		}
		else  {
			response.setFlash("Echec envoie email");
		}
	}
	
	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void print(ActionRequest request, ActionResponse response) {


		Message message = request.getContext().asType(Message.class );
		String messageIds = "";

		@SuppressWarnings("unchecked")
		List<Integer> lstSelectedMessages = (List<Integer>) request.getContext().get("_ids");
		if(lstSelectedMessages != null){
			for(Integer it : lstSelectedMessages) {
				messageIds+= it.toString()+",";
			}
		}	
			
		if(!messageIds.equals("")){
			messageIds = messageIds.substring(0, messageIds.length()-1);	
			message = Message.find(new Long(lstSelectedMessages.get(0)));
		}else if(message.getId() != null){
			messageIds = message.getId().toString();			
		}
		
		if(!messageIds.equals("")){
			StringBuilder url = new StringBuilder();			
			
			User user = AuthUtils.getUser();
			Company company = message.getCompany();
			
			String language = "en";
			if(user != null && user.getLanguage() != null && !user.getLanguage().isEmpty())  {
				language = user.getLanguage();
			}
			else if(company != null && company.getPrintingSettings() != null && company.getPrintingSettings().getLanguageSelect() !=null && !company.getPrintingSettings().getLanguageSelect().isEmpty() ) {
				language = company.getPrintingSettings().getLanguageSelect();
			}

			url.append(
					new ReportSettings(IReport.MESSAGE_PDF)
					.addParam("Locale", language)
					.addParam("__locale", "fr_FR")
					.addParam("MessageId", messageIds)
					.getUrl());
			
			LOG.debug("URL : {}", url);
			
			String urlNotExist = URLService.notExist(url.toString());
			if (urlNotExist == null){
				LOG.debug("Impression de Message "+message.getSubject()+" : "+url.toString());
				
				String title = " ";
				if(message.getSubject() != null)  {
					title += lstSelectedMessages == null ? "Message "+message.getSubject():"Messages";
				}
				
				Map<String,Object> mapView = new HashMap<String,Object>();
				mapView.put("title", title);
				mapView.put("resource", url);
				mapView.put("viewType", "html");
				response.setView(mapView);	
					
			}
			else {
				response.setFlash(urlNotExist);
			}
		}else{
			response.setFlash("Please select the Message(s) to print.");
		}	
	}
	
	
}