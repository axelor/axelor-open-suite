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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportSettings;
import com.axelor.apps.base.db.UserInfo;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.googleapps.db.GoogleFile;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.report.IReport;
import com.axelor.apps.supplychain.service.SalesOrderPurchaseService;
import com.axelor.apps.supplychain.service.SalesOrderService;
import com.axelor.apps.supplychain.service.SalesOrderStockMoveService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.googleapps.document.DocumentService;
import com.axelor.googleapps.userutils.Utils;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class SalesOrderController {

	private static final Logger LOG = LoggerFactory.getLogger(SalesOrderController.class);
	
	@Inject
	private Provider<SalesOrderService> salesOrderProvider;
	
	@Inject
	private Provider<SalesOrderStockMoveService> salesOrderStockMoveProvider;
	
	@Inject
	private Provider<SalesOrderPurchaseService> salesOrderPurchaseProvider;
	
	@Inject
	private Provider<SequenceService> sequenceProvider;

	@Inject 
	private Provider<DocumentService> documentProvider;

	@Inject 
	private Provider<Utils> userUtils;

	
	
	/**
	 * saves the document for any type of entity using template
	 * @param request
	 * @param response
	 */
	public void saveDocumentForOrder(ActionRequest request,ActionResponse response) {

		userUtils.get().validAppsConfig(request, response);

		// in this line change the Class as per the Module requirement i.e SalesOrder class here used
		SalesOrder dataObject = request.getContext().asType(SalesOrder.class);
		User currentUser = 	AuthUtils.getUser();
		UserInfo currentUserInfo = UserInfo.filter("self.internalUser = ?1", currentUser).fetchOne();

		GoogleFile documentData = documentProvider.get().createDocumentWithTemplate(currentUserInfo,dataObject);
		if(documentData == null) {
			response.setFlash("The Document Can't be created because the template for this type of Entity not Found..!");
			return;
		}
		response.setFlash("Document Created in Your Root Directory");
	}
	
	
	public void compute(ActionRequest request, ActionResponse response)  {
		
		SalesOrder salesOrder = request.getContext().asType(SalesOrder.class);

		try {		
			salesOrderProvider.get().computeSalesOrder(salesOrder);
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
	public void showSalesOrder(ActionRequest request, ActionResponse response) {

		SalesOrder salesOrder = request.getContext().asType(SalesOrder.class);

		StringBuilder url = new StringBuilder();
		
		String language="";
		try{
			language = salesOrder.getClientPartner().getLanguageSelect() != null? salesOrder.getClientPartner().getLanguageSelect() : salesOrder.getCompany().getPrintingSettings().getLanguageSelect() != null ? salesOrder.getCompany().getPrintingSettings().getLanguageSelect() : "en" ; 
		}catch (NullPointerException e) {
			language = "en";
		}
		language = language.equals("")? "en": language;
		
		url.append(
				new ReportSettings(IReport.SALES_ORDER, ReportSettings.FORMAT_PDF)
				.addParam("Locale", language)
				.addParam("__locale", "fr_FR")
				.addParam("SalesOrderId", salesOrder.getId().toString())
				.getUrl());

		LOG.debug("URL : {}", url);
		String urlNotExist = URLService.notExist(url.toString());
		
		if(urlNotExist == null) {
		
			LOG.debug("Impression du devis "+salesOrder.getSalesOrderSeq()+" : "+url.toString());
			
			String title = "Devis ";
			if(salesOrder.getSalesOrderSeq() != null)  {
				title += salesOrder.getSalesOrderSeq();
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
	
	
	public void exportSalesOrderExcel(ActionRequest request, ActionResponse response) {

		SalesOrder salesOrder = request.getContext().asType(SalesOrder.class);

		StringBuilder url = new StringBuilder();
		
		String language="";
		try{
			language = salesOrder.getClientPartner().getLanguageSelect() != null? salesOrder.getClientPartner().getLanguageSelect() : salesOrder.getCompany().getPrintingSettings().getLanguageSelect() != null ? salesOrder.getCompany().getPrintingSettings().getLanguageSelect() : "en" ; 
		}catch (NullPointerException e) {
			language = "en";
		}
		language = language.equals("")? "en": language;

		url.append(
				new ReportSettings(IReport.SALES_ORDER, ReportSettings.FORMAT_XLS)
				.addParam("Locale", language)
				.addParam("__locale", "fr_FR")
				.addParam("SalesOrderId", salesOrder.getId().toString())
				.getUrl());

		LOG.debug("URL : {}", url);
		String urlNotExist = URLService.notExist(url.toString());
		
		if(urlNotExist == null) {
		
			LOG.debug("Impression du devis "+salesOrder.getSalesOrderSeq()+" : "+url.toString());
			
			String title = "Devis ";
			if(salesOrder.getSalesOrderSeq() != null)  {
				title += salesOrder.getSalesOrderSeq();
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
	
	
	
	public void exportSalesOrderWord(ActionRequest request, ActionResponse response) {

		SalesOrder salesOrder = request.getContext().asType(SalesOrder.class);

		StringBuilder url = new StringBuilder();
		
		String language="";
		try{
			language = salesOrder.getClientPartner().getLanguageSelect() != null? salesOrder.getClientPartner().getLanguageSelect() : salesOrder.getCompany().getPrintingSettings().getLanguageSelect() != null ? salesOrder.getCompany().getPrintingSettings().getLanguageSelect() : "en" ; 
		}catch (NullPointerException e) {
			language = "en";
		}
		language = language.equals("")? "en": language;

		url.append(
				new ReportSettings(IReport.SALES_ORDER, ReportSettings.FORMAT_DOC)
				.addParam("Locale", language)
				.addParam("__locale", "fr_FR")
				.addParam("SalesOrderId", salesOrder.getId().toString())
				.getUrl());
		
		LOG.debug("URL : {}", url);
		String urlNotExist = URLService.notExist(url.toString());
		
		if(urlNotExist == null) {
		
			LOG.debug("Impression du devis "+salesOrder.getSalesOrderSeq()+" : "+url.toString());
			
			String title = "Devis ";
			if(salesOrder.getSalesOrderSeq() != null)  {
				title += salesOrder.getSalesOrderSeq();
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
		
		SalesOrder salesOrder = request.getContext().asType(SalesOrder.class);

		if(salesOrder != null && salesOrder.getSalesOrderSeq() ==  null && salesOrder.getCompany() != null) {
			
			response.setValue("salesOrderSeq", salesOrderProvider.get().getSequence(salesOrder.getCompany()));
			
		}
	}
	
	
	
	public void createStockMoves(ActionRequest request, ActionResponse response) throws AxelorException {
		
		SalesOrder salesOrder = request.getContext().asType(SalesOrder.class);
		
		if(salesOrder.getId() != null) {
			
			salesOrderStockMoveProvider.get().createStocksMovesFromSalesOrder(SalesOrder.find(salesOrder.getId()));
		}
	}
	
	public void getLocation(ActionRequest request, ActionResponse response) {
		
		SalesOrder salesOrder = request.getContext().asType(SalesOrder.class);
		
		if(salesOrder != null) {
			
			Location location = salesOrderProvider.get().getLocation(salesOrder.getCompany());
			
			if(location != null) {
				response.setValue("location", location);
			}
		}
	}
	
	
	public void validateCustomer(ActionRequest request, ActionResponse response) {
		
		SalesOrder salesOrder = request.getContext().asType(SalesOrder.class);
		
		response.setValue("clientPartner", salesOrderProvider.get().validateCustomer(salesOrder));
		
	}
	
	public void createPurchaseOrders(ActionRequest request, ActionResponse response) throws AxelorException {
		
		SalesOrder salesOrder = request.getContext().asType(SalesOrder.class);
		
		if(salesOrder.getId() != null) {
			
			salesOrderPurchaseProvider.get().createPurchaseOrders(SalesOrder.find(salesOrder.getId()));
		}
	}
	
}
