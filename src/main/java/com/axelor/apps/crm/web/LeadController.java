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
package com.axelor.apps.crm.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportSettings;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.report.IReport;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;


public class LeadController {

	private static final Logger LOG = LoggerFactory.getLogger(LeadController.class);
	
	
	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void print(ActionRequest request, ActionResponse response) {


		Lead lead = request.getContext().asType(Lead.class );
		String leadIds = "";

		@SuppressWarnings("unchecked")
		List<Integer> lstSelectedleads = (List<Integer>) request.getContext().get("_ids");
		if(lstSelectedleads != null){
			for(Integer it : lstSelectedleads) {
				leadIds+= it.toString()+",";
			}
		}	
			
		if(!leadIds.equals("")){
			leadIds = leadIds.substring(0, leadIds.length()-1);	
			lead = Lead.find(new Long(lstSelectedleads.get(0)));
		}else if(lead.getId() != null){
			leadIds = lead.getId().toString();			
		}
		
		if(!leadIds.equals("")){
			StringBuilder url = new StringBuilder();			
			
			User user = AuthUtils.getUser();
			String language = "en";
			try {
			
				if(user != null && !Strings.isNullOrEmpty(user.getLanguage()))  {
					language = user.getLanguage();
				}
				else if (lead.getPartner().getLanguageSelect()!= null){
					language = lead.getPartner().getLanguageSelect()!= null? lead.getPartner().getLanguageSelect():"en";
				} 
			}catch(Exception e){}
			
			url.append(
					new ReportSettings(IReport.LEAD)
					.addParam("Locale", language)
					.addParam("__locale", "fr_FR")
					.addParam("LeadId", leadIds)
					.getUrl());
			
			LOG.debug("URL : {}", url);
			
			String urlNotExist = URLService.notExist(url.toString());
			if (urlNotExist == null){
				LOG.debug("Impression de l'O.F.  "+lead.getFullName()+" : "+url.toString());
				
				String title = " ";
				if(lead.getFirstName() != null)  {
					title += lstSelectedleads == null ? "Lead "+lead.getFirstName():"Leads";
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
			response.setFlash("Please select the Lead(s) to print.");
		}	
	}
	
	
}
