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
package com.axelor.apps.crm.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.AppSettings;
import com.axelor.apps.ReportSettings;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.report.IReport;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;


public class LeadController {

	@Inject
	private Provider<MapService> mapProvider;
	
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
	
	public void showLeadsOnMap(ActionRequest request, ActionResponse response) throws IOException {
		
		String appHome = AppSettings.get().get("application.home");
		if (Strings.isNullOrEmpty(appHome)) {
			response.setFlash("Can not open map, Please Configure Application Home First.");
			return;
		}
		if (!mapProvider.get().isInternetAvailable()) {
			response.setFlash("Can not open map, Please Check your Internet connection.");
			return;			
		}		
		String mapUrl = new String(appHome + "/map/gmap-objs.html?apphome=" + appHome + "&object=lead");
		Map<String, Object> mapView = new HashMap<String, Object>();
		mapView.put("title", "Leads");
		mapView.put("resource", mapUrl);
		mapView.put("viewType", "html");		
		response.setView(mapView);
	}	
	
}
