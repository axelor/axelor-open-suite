package com.axelor.apps.base.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.AxelorSettings;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.db.User;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ProductControllerSimple {
	
	private static final Logger LOG = LoggerFactory.getLogger(ProductControllerSimple.class);
	
	public void printProductCatelog(ActionRequest request, ActionResponse response) {

		AxelorSettings axelorSettings = AxelorSettings.get();
		
		StringBuilder url = new StringBuilder();
		User user = (User) request.getContext().get("__user__");
		
		int currentYear = GeneralService.getTodayDateTime().getYear();
		String productIds = "";

		List<Long> lstSelectedPartner = (List<Long>) request.getContext().get("_ids");

		for(Long it : lstSelectedPartner) {
			productIds+= it+",";
		}

		if(!productIds.equals("")){
			productIds = "&ProductIds="+productIds.substring(0, productIds.length()-1);	
		}
		
		url.append(axelorSettings.get("axelor.report.engine", "")+"/frameset?__report=report/ProductCatalog_PGQL.rptdesign&__format=pdf"+productIds+"&UserId="+user.getId()+"&CurrYear="+currentYear+"&__locale=fr_FR"+axelorSettings.get("axelor.report.engine.datasource"));
		
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
