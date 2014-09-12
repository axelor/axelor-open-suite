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
package com.axelor.apps.sale.web;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportSettings;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.report.IReport;
import com.axelor.apps.sale.service.SaleOrderService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Provider;
//import com.axelor.googleapps.connector.utils.Utils;
//import com.axelor.googleapps.service.DocumentService;

public class SaleOrderController {

	private static final Logger LOG = LoggerFactory.getLogger(SaleOrderController.class);
	
	@Inject
	private Provider<SaleOrderService> saleOrderProvider;
	
	@Inject
	private Provider<SequenceService> sequenceProvider;

//	@Inject 
//	private Provider<DocumentService> documentSeriveObj;
//
//	@Inject 
//	private Provider<Utils> userUtils;

	
	
	/**
	 * saves the document for any type of entity using template
	 * @param request
	 * @param response
	 * @throws Exception 
	 */
	public void saveDocumentForOrder(ActionRequest request,ActionResponse response) throws Exception {

//		userUtils.get().validAppsConfig(request, response);

		// in this line change the Class as per the Module requirement i.e SaleOrder class here used
//		SaleOrder dataObject = request.getContext().asType(SaleOrder.class);

//		GoogleFile documentData = documentSeriveObj.get().createDocumentWithTemplate(dataObject);
//		if(documentData == null) {
//			response.setFlash("The Document Can't be created because the template for this type of Entity not Found..!");
//			return;
//		}
//		response.setFlash("Document Created in Your Root Directory");
	}
	
	
	public void compute(ActionRequest request, ActionResponse response)  {
		
		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

		try {		
			saleOrderProvider.get().computeSaleOrder(saleOrder);
			response.setReload(true);
		}
		catch(Exception e)  { TraceBackService.trace(response, e); }
	}
	
	
	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void showSaleOrder(ActionRequest request, ActionResponse response) {

		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

		StringBuilder url = new StringBuilder();
		
		String language="";
		try{
			language = saleOrder.getClientPartner().getLanguageSelect() != null? saleOrder.getClientPartner().getLanguageSelect() : saleOrder.getCompany().getPrintingSettings().getLanguageSelect() != null ? saleOrder.getCompany().getPrintingSettings().getLanguageSelect() : "en" ; 
		}catch (NullPointerException e) {
			language = "en";
		}
		language = language.equals("")? "en": language;
		
		url.append(
				new ReportSettings(IReport.SALES_ORDER, ReportSettings.FORMAT_PDF)
				.addParam("Locale", language)
				.addParam("__locale", "fr_FR")
				.addParam("SaleOrderId", saleOrder.getId().toString())
				.getUrl());

		LOG.debug("URL : {}", url);
		String urlNotExist = URLService.notExist(url.toString());
		
		if(urlNotExist == null) {
		
			LOG.debug("Impression du devis "+saleOrder.getSaleOrderSeq()+" : "+url.toString());
			
			String title = "Devis ";
			if(saleOrder.getSaleOrderSeq() != null)  {
				title += saleOrder.getSaleOrderSeq();
			}
			
			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Devis "+title);
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);	
		}
		else {
			response.setFlash(urlNotExist);
		}
	}
	
	
	public void exportSaleOrderExcel(ActionRequest request, ActionResponse response) {

		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

		StringBuilder url = new StringBuilder();
		
		String language="";
		try{
			language = saleOrder.getClientPartner().getLanguageSelect() != null? saleOrder.getClientPartner().getLanguageSelect() : saleOrder.getCompany().getPrintingSettings().getLanguageSelect() != null ? saleOrder.getCompany().getPrintingSettings().getLanguageSelect() : "en" ; 
		}catch (NullPointerException e) {
			language = "en";
		}
		language = language.equals("")? "en": language;

		url.append(
				new ReportSettings(IReport.SALES_ORDER, ReportSettings.FORMAT_XLS)
				.addParam("Locale", language)
				.addParam("__locale", "fr_FR")
				.addParam("SaleOrderId", saleOrder.getId().toString())
				.getUrl());

		LOG.debug("URL : {}", url);
		String urlNotExist = URLService.notExist(url.toString());
		
		if(urlNotExist == null) {
		
			LOG.debug("Impression du devis "+saleOrder.getSaleOrderSeq()+" : "+url.toString());
			
			String title = "Devis ";
			if(saleOrder.getSaleOrderSeq() != null)  {
				title += saleOrder.getSaleOrderSeq();
			}
			
			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Devis "+title);
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);	
		}
		else {
			response.setFlash(urlNotExist);
		}
	}
	
	
	
	public void exportSaleOrderWord(ActionRequest request, ActionResponse response) {

		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

		StringBuilder url = new StringBuilder();
		
		String language="";
		try{
			language = saleOrder.getClientPartner().getLanguageSelect() != null? saleOrder.getClientPartner().getLanguageSelect() : saleOrder.getCompany().getPrintingSettings().getLanguageSelect() != null ? saleOrder.getCompany().getPrintingSettings().getLanguageSelect() : "en" ; 
		}catch (NullPointerException e) {
			language = "en";
		}
		language = language.equals("")? "en": language;

		url.append(
				new ReportSettings(IReport.SALES_ORDER, ReportSettings.FORMAT_DOC)
				.addParam("Locale", language)
				.addParam("__locale", "fr_FR")
				.addParam("SaleOrderId", saleOrder.getId().toString())
				.getUrl());
		
		LOG.debug("URL : {}", url);
		String urlNotExist = URLService.notExist(url.toString());
		
		if(urlNotExist == null) {
		
			LOG.debug("Impression du devis "+saleOrder.getSaleOrderSeq()+" : "+url.toString());
			
			String title = "Devis ";
			if(saleOrder.getSaleOrderSeq() != null)  {
				title += saleOrder.getSaleOrderSeq();
			}
			
			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Devis "+title);
			mapView.put("resource", url);
			mapView.put("viewType", "html");
			response.setView(mapView);	
		}
		else {
			response.setFlash(urlNotExist);
		}
	}
	
	
	
	public void setSequence(ActionRequest request, ActionResponse response) throws AxelorException {
		
		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);

		if(saleOrder != null &&  saleOrder.getCompany() != null) {
			
			response.setValue("saleOrderSeq", saleOrderProvider.get().getSequence(saleOrder.getCompany()));
			
		}
	}
	
	
	
	public void validateCustomer(ActionRequest request, ActionResponse response) {
		
		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
		
		response.setValue("clientPartner", saleOrderProvider.get().validateCustomer(saleOrder));
		
	}
	public void setDraftSequence(ActionRequest request,ActionResponse response){
		SaleOrder saleOrder=request.getContext().asType(SaleOrder.class);
		if(saleOrder.getSaleOrderSeq()!=null){
			return;
		}
		response.setValue("saleOrderSeq","*"+saleOrder.getId().toString());
	}
}
