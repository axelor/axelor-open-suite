/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
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
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.base.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.AxelorSettings;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.meta.db.MetaUser;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ProductControllerSimple {
	
	private static final Logger LOG = LoggerFactory.getLogger(ProductControllerSimple.class);
	
	public void printProductCatelog(ActionRequest request, ActionResponse response) {

		AxelorSettings axelorSettings = AxelorSettings.get();
		
		StringBuilder url = new StringBuilder();
		User user =  AuthUtils.getUser();
		
		int currentYear = GeneralService.getTodayDateTime().getYear();
		String productIds = "";

		List<Integer> lstSelectedPartner = (List<Integer>) request.getContext().get("_ids");
		for(Integer it : lstSelectedPartner) {
			productIds+= it.toString()+",";
		}

		if(!productIds.equals("")){
			productIds = "&ProductIds="+productIds.substring(0, productIds.length()-1);	
		}

		MetaUser metaUser = MetaUser.findByUser( AuthUtils.getUser());
		String language = metaUser.getLanguage() != null? metaUser.getLanguage() : "en"; 
		url.append(axelorSettings.get("axelor.report.engine", "")+"/frameset?__report=report/ProductCatalog_PGQL.rptdesign&Locale="+language+"&__format=pdf"+productIds+"&UserId="+user.getId()+"&CurrYear="+currentYear+"&__locale=fr_FR"+axelorSettings.get("axelor.report.engine.datasource"));
		
		LOG.debug("URL : {}", url);
		String urlNotExist = URLService.notExist(url.toString());
		if (urlNotExist == null){
		
			LOG.debug("Impression des informations sur le partenaire Product Catelog "+currentYear);
			
			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Product Catelog "+currentYear);
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);		
		}
		else {
			response.setFlash(urlNotExist);
		}
	}
}
