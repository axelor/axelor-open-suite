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
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.report.IReport;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ProductController {
	
	private static final Logger LOG = LoggerFactory.getLogger(ProductController.class);
	
	
	public void generateProductVariants(ActionRequest request, ActionResponse response) throws AxelorException {
		Product product = request.getContext().asType(Product.class);
		ProductService productService = Beans.get(ProductService.class);
		product = productService.find(product.getId());
		
		if(product.getProductVariantConfig() != null)  {
			productService.generateProductVariants(product);
			
			response.setFlash(I18n.get(IExceptionMessage.PRODUCT_1));
			response.setReload(true);
		}
	}
	
	public void updateProductsPrices(ActionRequest request, ActionResponse response) throws AxelorException {
		Product product = request.getContext().asType(Product.class);
		ProductService productService = Beans.get(ProductService.class);
		
		product = productService.find(product.getId());
		
		productService.updateProductPrice(product);
		
		response.setFlash(I18n.get(IExceptionMessage.PRODUCT_2));
		response.setReload(true);
	}
	
	
	
	public void printProductCatelog(ActionRequest request, ActionResponse response) {

		StringBuilder url = new StringBuilder();
		User user =  Beans.get(UserService.class).getUser();
		
		int currentYear = GeneralService.getTodayDateTime().getYear();
		String productIds = "";

		List<Integer> lstSelectedPartner = (List<Integer>) request.getContext().get("_ids");
		for(Integer it : lstSelectedPartner) {
			productIds+= it.toString()+",";
		}

		if(!productIds.equals("")){
			productIds = productIds.substring(0, productIds.length()-1);	
		}

		String language = user != null? (user.getLanguage() == null || user.getLanguage().equals(""))? "en" : user.getLanguage() : "en"; 
		
		url.append(new ReportSettings(IReport.PRODUCT_CATALOG)
					.addParam("Locale", language)
					.addParam("__locale", "fr_FR")
					.addParam("UserId", user.getId().toString())
					.addParam("CurrYear", Integer.toString(currentYear))
					.addParam("ProductIds", productIds)
					.getUrl());
		
		LOG.debug("URL : {}", url);
		String urlNotExist = URLService.notExist(url.toString());
		if (urlNotExist == null){
		
			LOG.debug("Impression des informations sur le partenaire Product Catelog "+currentYear);
			
			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", I18n.get(IExceptionMessage.PRODUCT_3)+" "+currentYear);
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);		
		}
		else {
			response.setFlash(urlNotExist);
		}
	}
	
	public void printProductSheet(ActionRequest request, ActionResponse response) {

		Product product = request.getContext().asType(Product.class);
		User user =  Beans.get(UserService.class).getUser();
	
		StringBuilder url = new StringBuilder();
		 
		String language = user != null? (user.getLanguage() == null || user.getLanguage().equals(""))? "en" : user.getLanguage() : "en"; 
		
		url.append(new ReportSettings(IReport.PRODUCT_SHEET)
					.addParam("Locale", language)
					.addParam("__locale", "fr_FR")
					.addParam("ProductId", product.getId().toString())
					.addParam("CompanyId", user.getActiveCompany().getId().toString())
					.getUrl());
		
		LOG.debug("URL : {}", url);
		String urlNotExist = URLService.notExist(url.toString());
		if (urlNotExist == null){
		
			LOG.debug("Impression des informations sur le partenaire Product "+product.getName());
			
			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", I18n.get(IExceptionMessage.PRODUCT_4)+" "+product.getCode());
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);		
		}
		else {
			response.setFlash(urlNotExist);
		}
	}
}
