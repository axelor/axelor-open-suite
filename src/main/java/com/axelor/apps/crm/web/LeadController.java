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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.AppSettings;
import com.axelor.apps.ReportSettings;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.crm.db.Lead;
import com.axelor.apps.crm.db.report.IReport;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.views.Field;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.inject.Inject;


public class LeadController {

	@Inject
	AddressService addressService;
	
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
		if (!addressService.isInternetAvailable()) {
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
	
	public Set<MetaField> setFields(String model) throws IOException {
		LOG.debug("Model: {}",model);
		Set<MetaField> fieldSet = new HashSet<MetaField>();
		List<String> fields = new ArrayList<String>();
		for(MetaField field :MetaField.all_().filter("metaModel.fullName = ?1 AND (relationship = null OR relationship = 'ManyToOne')",model).fetch()){
			fieldSet.add(field);
			fields.add(field.getName());
		}
			
		LOG.debug("Fields set: {}",fields);
		return fieldSet;
	}
	
	@SuppressWarnings("unchecked")
	public void showDuplicate(ActionRequest request, ActionResponse response){
		String object = (String) request.getContext().get("object");
		LOG.debug("Duplicate record model: {}",object);
		MetaModel model =  MetaModel.all_().filter("fullName = ?1", object).fetchOne();
		if(model == null){
			response.setFlash("No meta model");
			return;
		}
		String table = model.getTableName();
		LOG.debug("Duplicate record table: {}",table);
		List<String> fieldList = new ArrayList<String>();
		List<String> joinList = new ArrayList<String>();
		for(HashMap<String,Object> field:(List<HashMap<String,Object>>) request.getContext().get("fieldsSet")){
			if((Boolean)field.get("selected")){
				String name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, (String)field.get("name"));
				fieldList.add(name);
				joinList.add(String.format("model.%s=dmodel.%s",name,name));
			}
		}
		if(fieldList.isEmpty()){
			response.setFlash("Please select key fields to check duplicate");
			return;
		}
		String fields =  Joiner.on(",").join(fieldList);
		LOG.debug("Duplicate record fieldList: {}",fields);
		LOG.debug("Duplicate record joinList: {}",joinList);
		Query query = JPA.em().createNativeQuery(String.format("SELECT id FROM %s model join (SELECT %s FROM %s GROUP BY %s HAVING COUNT(*) > 1) dmodel on (%s)", table,fields,table,fields,Joiner.on(" AND ").join(joinList)));
		List<String> ids = new ArrayList<String>();
		for(Object id : query.getResultList())
			ids.add(id.toString());
		if(ids.isEmpty())
			response.setFlash("No duplicate records found");
		else
			response.setView(ActionView
					  .define("Duplicate records")
					  .model(object)
					  .add("grid", "lead-grid")
					  .add("form", "lead-form")
					  .domain("self.id in ("+Joiner.on(",").join(ids)+")")
					  .map());
	}
	
}
