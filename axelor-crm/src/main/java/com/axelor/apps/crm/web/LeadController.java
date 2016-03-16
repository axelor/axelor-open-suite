/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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

import org.eclipse.birt.core.exception.BirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.AppSettings;
import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.db.ImportConfiguration;
import com.axelor.apps.base.db.repo.ImportConfigurationRepository;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.repo.LeadRepository;
import com.axelor.apps.crm.db.report.IReport;
import com.axelor.apps.crm.exception.IExceptionMessage;
import com.axelor.apps.crm.service.LeadService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;
import com.google.inject.Inject;


public class LeadController {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject
	private LeadService leadService;
	
	@Inject
	private LeadRepository leadRepo;
	
	/**
	 * Method to generate Lead as a Pdf
	 *
	 * @param request
	 * @param response
	 * @return
	 * @throws BirtException 
	 * @throws IOException 
	 */
	public void print(ActionRequest request, ActionResponse response) throws AxelorException  {


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
			lead = leadRepo.find(new Long(lstSelectedleads.get(0)));
		}else if(lead.getId() != null){
			leadIds = lead.getId().toString();			
		}
		
		if(!leadIds.equals("")){
			
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
			
			String title = " ";
			if(lead.getFirstName() != null)  {
				title += lstSelectedleads == null ? "Lead "+lead.getFirstName():"Leads";
			}

			String fileLink = ReportFactory.createReport(IReport.LEAD, title+"-${date}")
					.addParam("LeadId", leadIds)
					.addParam("Locale", language)
					.generate()
					.getFileLink();

			logger.debug("Printing "+title);
		
			response.setView(ActionView
					.define(title)
					.add("html", fileLink).map());		
				
		}else{
			response.setFlash(I18n.get(IExceptionMessage.LEAD_1));
		}	
	}
	
	public void showLeadsOnMap(ActionRequest request, ActionResponse response) throws IOException {
		
		String appHome = AppSettings.get().get("application.home");
		if (Strings.isNullOrEmpty(appHome)) {
			response.setFlash(I18n.get(IExceptionMessage.LEAD_2));
			return;
		}
		if (!Beans.get(MapService.class).isInternetAvailable()) {
			response.setFlash(I18n.get(IExceptionMessage.LEAD_3));
			return;			
		}		
		String mapUrl = new String(appHome + "/map/gmap-objs.html?apphome=" + appHome + "&object=lead");
		Map<String, Object> mapView = new HashMap<String, Object>();
		mapView.put("title", I18n.get("Leads"));
		mapView.put("resource", mapUrl);
		mapView.put("viewType", "html");		
		response.setView(mapView);
	}	
	
	
	public void setSocialNetworkUrl(ActionRequest request, ActionResponse response) throws IOException {
		
		Lead lead = request.getContext().asType(Lead.class );
		Map<String,String> urlMap = leadService.getSocialNetworkUrl(lead.getName(), lead.getFirstName(), lead.getEnterpriseName());
		response.setAttr("google", "title", urlMap.get("google"));
		response.setAttr("facebook", "title", urlMap.get("facebook"));
		response.setAttr("twitter", "title", urlMap.get("twitter"));
		response.setAttr("linkedin", "title", urlMap.get("linkedin"));
		response.setAttr("youtube", "title", urlMap.get("youtube"));
		
	}
	
	public void getLeadImportConfig(ActionRequest request, ActionResponse response){
		ImportConfiguration leadImportConfig  = Beans.get(ImportConfigurationRepository.class).all().filter("self.bindMetaFile.fileName = ?1","import-config-lead.xml").fetchOne();
		logger.debug("ImportConfig for lead: {}",leadImportConfig);
		if(leadImportConfig == null){
			response.setFlash(I18n.get(IExceptionMessage.LEAD_4));
		}
		else{
			response.setView(ActionView
							  .define(I18n.get(IExceptionMessage.LEAD_5))
							  .model("com.axelor.apps.base.db.ImportConfiguration")
							  .add("form", "import-configuration-form")
							  .param("popup", "reload")
							  .param("forceEdit", "true")
  					  		  .param("popup-save", "false")
  					  		  .param("show-toolbar", "false")
							  .context("_showRecord", leadImportConfig.getId().toString())
							  .map());
		}
	}
	
}
