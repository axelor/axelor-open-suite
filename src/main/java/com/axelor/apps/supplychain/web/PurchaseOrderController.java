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
package com.axelor.apps.supplychain.web;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.AxelorSettings;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.supplychain.db.PurchaseOrder;
import com.axelor.apps.supplychain.service.PurchaseOrderService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class PurchaseOrderController {

	@Inject
	private SequenceService sequenceService;
	
	@Inject
	private PurchaseOrderService purchaseOrderService;
	
	private static final Logger LOG = LoggerFactory.getLogger(PurchaseOrderController.class);

	
	public void setSequence(ActionRequest request, ActionResponse response) throws AxelorException {
		
		PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
		
		if(purchaseOrder != null && purchaseOrder.getPurchaseOrderSeq() ==  null && purchaseOrder.getCompany() != null) {
			
			response.setValue("purchaseOrderSeq", purchaseOrderService.getSequence(purchaseOrder.getCompany()));
		}
	}
	
	public void compute(ActionRequest request, ActionResponse response)  {
		
		PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);

		if(purchaseOrder != null) {
			try {
				purchaseOrderService.computePurchaseOrder(purchaseOrder);
				response.setReload(true);
			}
			catch(Exception e)  { TraceBackService.trace(response, e); }
		}
	}
	
	public void createStockMoves(ActionRequest request, ActionResponse response) throws AxelorException {
		
		PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
		
		if(purchaseOrder.getId() != null) {

			purchaseOrderService.createStocksMoves(PurchaseOrder.find(purchaseOrder.getId()));
		}
	}
	
	public void getLocation(ActionRequest request, ActionResponse response) {
		
		PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
		
		if(purchaseOrder.getCompany() != null) {
			
			response.setValue("location", purchaseOrderService.getLocation(purchaseOrder.getCompany()));
		}
	}
	
	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void showPurchaseOrder(ActionRequest request, ActionResponse response) {

		PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);

		StringBuilder url = new StringBuilder();
		AxelorSettings axelorSettings = AxelorSettings.get();
		String language="";
		try{
			language = purchaseOrder.getSupplierPartner().getLanguageSelect() != null? purchaseOrder.getSupplierPartner().getLanguageSelect() : purchaseOrder.getCompany().getPrintingSettings().getLanguageSelect() != null ? purchaseOrder.getCompany().getPrintingSettings().getLanguageSelect() : "en" ; 
		}catch (NullPointerException e) {
			language = "en";
		}
		language = language.equals("")? "en": language;

		
		url.append(axelorSettings.get("axelor.report.engine", "")+"/frameset?__report=report/PurchaseOrder.rptdesign&__format=pdf&PurchaseOrderId="+purchaseOrder.getId()+"&__locale=fr_FR&Locale="+language+axelorSettings.get("axelor.report.engine.datasource"));
		LOG.debug("URL : {}", url);
		String urlNotExist = URLService.notExist(url.toString());
		
		if(urlNotExist == null) {
		
			LOG.debug("Impression du devis "+purchaseOrder.getPurchaseOrderSeq() +" : "+url.toString());
			
			String title = "Devis ";
			if(purchaseOrder.getPurchaseOrderSeq() != null)  {
				title += purchaseOrder.getPurchaseOrderSeq();
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
	}
}
