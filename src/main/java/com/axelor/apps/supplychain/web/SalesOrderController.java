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
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.UserInfo;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.googleapps.db.GoogleFile;
import com.axelor.apps.supplychain.db.ILocation;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.service.SalesOrderService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.googleapps.document.DocumentService;
import com.axelor.googleapps.userutils.Utils;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class SalesOrderController {

	@Inject
	private SalesOrderService salesOrderService;

	@Inject
	SequenceService sequenceService;

	@Inject 
	DocumentService documentSeriveObj;

	@Inject 
	Utils userUtils;

	private static final Logger LOG = LoggerFactory.getLogger(SalesOrderController.class);
	
	/**
	 * saves the document for any type of entity using template
	 * @param request
	 * @param response
	 */
	public void saveDocumentForOrder(ActionRequest request,ActionResponse response) {

		userUtils.validAppsConfig(request, response);

		// in this line change the Class as per the Module requirement i.e SalesOrder class here used
		SalesOrder dataObject = request.getContext().asType(SalesOrder.class);
		User currentUser = 	AuthUtils.getUser();
		UserInfo currentUserInfo = UserInfo.all().filter("self.internalUser = ?1", currentUser).fetchOne();

		GoogleFile documentData = documentSeriveObj.createDocumentWithTemplate(currentUserInfo,dataObject);
		if(documentData == null) {
			response.setFlash("The Document Can't be created because the template for this type of Entity not Found..!");
			return;
		}
		response.setFlash("Document Created in Your Root Directory");
	}
	
	public void compute(ActionRequest request, ActionResponse response)  {
		
		SalesOrder salesOrder = request.getContext().asType(SalesOrder.class);

		try {		
			salesOrderService.computeSalesOrder(salesOrder);
			response.setReload(true);
			response.setFlash("Montant du devis : "+salesOrder.getInTaxTotal()+" TTC");
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
		AxelorSettings axelorSettings = AxelorSettings.get();

		url.append(axelorSettings.get("axelor.report.engine", "")+"/frameset?__report=report/SalesOrder.rptdesign&__format=pdf&SalesOrderId="+salesOrder.getId()+"&__locale=fr_FR"+axelorSettings.get("axelor.report.engine.datasource"));
		LOG.debug("URL : {}", url);
		String urlNotExist = URLService.notExist(url.toString());
		
		if(urlNotExist == null) {
		
			LOG.debug("Impression du devis "+salesOrder.getSalesOrderSeq()+" : "+url.toString());
			
			Map<String,Object> mapView = new HashMap<String,Object>();
			mapView.put("title", "Devis "+salesOrder.getSalesOrderSeq());
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
			String ref = sequenceService.getSequence(IAdministration.SALES_ORDER,salesOrder.getCompany(),false);
			if (ref == null)
				throw new AxelorException(String.format("La société %s n'a pas de séquence de configurée pour les devis",salesOrder.getCompany().getName()),
								IException.CONFIGURATION_ERROR);
			else
				response.setValue("salesOrderSeq", ref);
		}
	}
	
	
	
	public void createStockMoves(ActionRequest request, ActionResponse response) throws AxelorException {
		
		SalesOrder salesOrder = request.getContext().asType(SalesOrder.class);
		
		if(salesOrder != null) {
			
			salesOrderService.createStocksMovesFromSalesOrder(salesOrder);
		}
	}
	
	public void getLocation(ActionRequest request, ActionResponse response) {
		
		SalesOrder salesOrder = request.getContext().asType(SalesOrder.class);
		
		if(salesOrder != null) {
			
			Location location = Location.all().filter("company = ? and isDefaultLocation = ? and typeSelect = ?", salesOrder.getCompany(), true, ILocation.INTERNAL).fetchOne();
			
			if(location != null) {
				response.setValue("location", location);
			}
		}
	}
}
