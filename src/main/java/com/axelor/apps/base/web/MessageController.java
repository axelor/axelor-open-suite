/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportSettings;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.report.IReport;
import com.axelor.apps.base.service.message.MessageServiceBaseImpl;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class MessageController {

	@Inject
	private Provider<MessageServiceBaseImpl> messageServiceBaseImpl;
	
	@Inject
	private MessageRepository messageRepo;
	
	private static final Logger LOG = LoggerFactory.getLogger(MessageController.class);
	
	
	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void printMessage(ActionRequest request, ActionResponse response) {
		Message message = request.getContext().asType(Message.class);
		String pdfPath = messageServiceBaseImpl.get().printMessage(message);
		if(pdfPath != null){
			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Message "+message.getSubject());
			mapView.put("resource", pdfPath);
			mapView.put("viewType", "html");
			response.setView(mapView);	
		}
		else
			response.setFlash("Error in print. Please check report configuration and print setting.");
	}
	
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
			message = messageRepo.find(new Long(lstSelectedMessages.get(0)));
		}else if(message.getId() != null){
			messageIds = message.getId().toString();			
		}
		
		if(!messageIds.equals("")){
			StringBuilder url = new StringBuilder();			
			
			User user = AuthUtils.getUser();
//			Company company = message.getCompany();
			
			String language = "en";
//			if(user != null && user.getLanguage() != null && !user.getLanguage().isEmpty())  {
//				language = user.getLanguage();
//			}
//			else if(company != null && company.getPrintingSettings() != null && company.getPrintingSettings().getLanguageSelect() !=null && !company.getPrintingSettings().getLanguageSelect().isEmpty() ) {
//				language = company.getPrintingSettings().getLanguageSelect();
//			}

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