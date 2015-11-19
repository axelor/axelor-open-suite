/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2015 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.web;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportSettings;
import com.axelor.apps.account.db.AssistantReportInvoice;
import com.axelor.apps.account.report.IReport;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;

public class AssistantReportInvoiceController {
	
	public void printSales(ActionRequest request, ActionResponse response){
		AssistantReportInvoice assistant = request.getContext().asType(AssistantReportInvoice.class);
		Logger logger = LoggerFactory.getLogger(getClass());
		StringBuilder url = new StringBuilder();
		
		String language="";
		try{
			language = AuthUtils.getUser().getLanguage() ;
		}catch (NullPointerException e) {
			language = "en";
		}
		language = language.equals("")? "en": language;

		String format = ReportSettings.FORMAT_PDF;
		if(assistant.getFormatSelect() == 2){
			format = ReportSettings.FORMAT_XLS;
		}
		Set<Long> partnerIds = new HashSet<Long>();
		for (Partner partner : assistant.getPartnerSet()) {
			partnerIds.add(partner.getId());
		}
		
		Set<Long> productsIds = new HashSet<Long>();
		for (Product product : assistant.getProductSet()) {
			productsIds.add(product.getId());
		}
		
		Set<Long> productCategoriesIds = new HashSet<Long>();
		for (ProductCategory productCategory : assistant.getProductCategorySet()) {
			productCategoriesIds.add(productCategory.getId());
		}
		
		String table = "false";
		if(assistant.getGraphTypeSelect() == 2){
			table = "true";
		}
		
		url.append( new ReportSettings(IReport.SALE_INVOICES_DETAILS, format)
							.addParam("Locale", language)
							.addParam("__locale", "fr_FR")
							.addParam("assistantId", assistant.getId().toString())
							.addParam("companyId", assistant.getCompany().getId().toString())
							.addParam("partnersIds", Joiner.on(",").join(partnerIds))
							.addParam("productsIds", Joiner.on(",").join(productsIds))
							.addParam("productCategoriesIds", Joiner.on(",").join(productCategoriesIds))
							.addParam("chart", table)
							.getUrl());
		
		String urlNotExist = URLService.notExist(url.toString());
		logger.debug(url.toString());
		if(urlNotExist == null) {
			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", I18n.get("SaleInvoicesDetails-")+assistant.getFromDate().toString("dd/MM/yyyy-")+assistant.getToDate().toString("dd/MM/yyyy"));
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);
		}
		else {
			response.setFlash(urlNotExist);
		}
	}
	
	public void printPurchases(ActionRequest request, ActionResponse response){
		AssistantReportInvoice assistant = request.getContext().asType(AssistantReportInvoice.class);
		
		StringBuilder url = new StringBuilder();
		
		String language="";
		try{
			language = AuthUtils.getUser().getLanguage() ;
		}catch (NullPointerException e) {
			language = "en";
		}
		language = language.equals("")? "en": language;

		String format = ReportSettings.FORMAT_PDF;
		if(assistant.getFormatSelect() == 2){
			format = ReportSettings.FORMAT_XLS;
		}
		Set<Long> partnerIds = new HashSet<Long>();
		for (Partner partner : assistant.getPartnerSet()) {
			partnerIds.add(partner.getId());
		}
		
		Set<Long> productsIds = new HashSet<Long>();
		for (Product product : assistant.getProductSet()) {
			productsIds.add(product.getId());
		}
		
		Set<Long> productCategoriesIds = new HashSet<Long>();
		for (ProductCategory productCategory : assistant.getProductCategorySet()) {
			productCategoriesIds.add(productCategory.getId());
		}
		
		String table = "false";
		if(assistant.getGraphTypeSelect() == 2){
			table = "true";
		}
		
		url.append( new ReportSettings(IReport.PURCHASE_INVOICES_DETAILS, format)
							.addParam("Locale", language)
							.addParam("__locale", "fr_FR")
							.addParam("assistantId", assistant.getId().toString())
							.addParam("companyId", assistant.getCompany().getId().toString())
							.addParam("partnersIds", Joiner.on(",").join(partnerIds))
							.addParam("productsIds", Joiner.on(",").join(productsIds))
							.addParam("productCategoriesIds", Joiner.on(",").join(productCategoriesIds))
							.addParam("chart", table)
							.getUrl());
		
		String urlNotExist = URLService.notExist(url.toString());

		if(urlNotExist == null) {
			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", I18n.get("PurchaseInvoicesDetails-")+assistant.getFromDate().toString("dd/MM/YYYY-")+assistant.getToDate().toString("dd/MM/YYYY"));
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);
		}
		else {
			response.setFlash(urlNotExist);
		}
	}
}
