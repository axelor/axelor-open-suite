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
package com.axelor.apps.supplychain.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.AxelorSettings;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.ProductFamily;
import com.axelor.apps.supplychain.db.Inventory;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.supplychain.service.InventoryService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.inject.Inject;

public class LocationController {

	@Inject
	private InventoryService inventoryService;
	private static final Logger LOG = LoggerFactory.getLogger(LocationController.class);
	
	public void checkIsDefaultLocation(ActionRequest request, ActionResponse response){
		
		Location location = request.getContext().asType(Location.class);
		
		if(location != null && location.getIsDefaultLocation() && location.getCompany() != null && location.getTypeSelect() != null) {
			
			Location findLocation = Location.all().filter("company = ? and typeSelect = ? and isDefaultLocation = ?", location.getCompany(),location.getTypeSelect(),location.getIsDefaultLocation()).fetchOne();
			
			if(findLocation != null) {
				response.setFlash("Il existe déjà un entrepot par défaut, veuillez d'abord désactiver l'entrepot "+findLocation.getName());
				response.setValue("isDefaultLocation", false);
			}
		}
	}
	
	public void createInventory(ActionRequest request, ActionResponse response) throws Exception {
		Context context = request.getContext();
		LocalDate date = new LocalDate(context.get("inventoryDate"));
		String description = (String) context.get("description");
		
		boolean excludeOutOfStock = (Boolean) context.get("excludeOutOfStock");
		boolean includeObsolete = (Boolean) context.get("includeObsolete");
		
		// Récupération de l'entrepot
		Map<String, Object> locationContext = (Map<String, Object>) context.get("location");
		
		Location location = null;
		
		if(locationContext != null)  {
			location = Location.find(((Integer)locationContext.get("id")).longValue());
		}
		
		// Récupération de la famille de produit
		Map<String, Object> productFamilyContext = (Map<String, Object>) context.get("productFamily");
		
		ProductFamily productFamily = null;
		
		if (productFamilyContext != null) {
			productFamily = ProductFamily.find(((Integer)productFamilyContext.get("id")).longValue());
		}
		
		// Récupération de la catégorie de produit
		Map<String, Object> productCategoryContext = (Map<String, Object>) context.get("productCategory");
		
		ProductCategory productCategory = null;
		
		if (productCategoryContext != null) {
			productCategory = ProductCategory.find(((Integer)productCategoryContext.get("id")).longValue());
		}
		
		
		Inventory inventory = inventoryService.createInventoryFromWizard(date, description, location, excludeOutOfStock,
										includeObsolete, productFamily, productCategory);
		response.setValue("inventoryId", inventory.getId());
	}
	
	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void print(ActionRequest request, ActionResponse response) {


		Location location = request.getContext().asType(Location.class );
		String locationIds = "";

		@SuppressWarnings("unchecked")
		List<Integer> lstSelectedLocations = (List<Integer>) request.getContext().get("_ids");
		if(lstSelectedLocations != null){
			for(Integer it : lstSelectedLocations) {
				locationIds+= it.toString()+",";
			}
		}	
			
		if(!locationIds.equals("")){
			locationIds = "&StockLocationId="+locationIds.substring(0, locationIds.length()-1);	
			location = Location.find(new Long(lstSelectedLocations.get(0)));
		}else if(location.getId() != null){
			locationIds = "&StockLocationId="+location.getId();			
		}
		
		if(!locationIds.equals("")){
			StringBuilder url = new StringBuilder();			
			AxelorSettings axelorSettings = AxelorSettings.get();
			
			User user = AuthUtils.getUser();
			Company company = location.getCompany();
			
			String language = "en";
			if(user != null && !Strings.isNullOrEmpty(user.getLanguage()))  {
				language = user.getLanguage();
			}
			else if(company != null && company.getPrintingSettings() != null && !Strings.isNullOrEmpty(company.getPrintingSettings().getLanguageSelect())) {
				language = company.getPrintingSettings().getLanguageSelect();
			}

			url.append(axelorSettings.get("axelor.report.engine", "")+"/frameset?__report=report/StockLocation.rptdesign&__format=pdf&Locale="+language+locationIds+"&__locale=fr_FR"+axelorSettings.get("axelor.report.engine.datasource"));

			LOG.debug("URL : {}", url);
			
			String urlNotExist = URLService.notExist(url.toString());
			if (urlNotExist == null){
				LOG.debug("Impression de l'O.F.  "+location.getName()+" : "+url.toString());
				
				String title = " ";
				if(location.getName() != null)  {
					title += lstSelectedLocations == null ? "Stock "+location.getName():"Stocks";
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
			response.setFlash("Please select the Stock Location(s) to print.");
		}	
	}
	
}
