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
package com.axelor.apps.production.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.AxelorSettings;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.service.ManufOrderService;
import com.axelor.apps.production.service.ManufOrderWorkflowService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.inject.Provider;

public class ManufOrderController {

	@Inject
	private Provider<ManufOrderService> manufOrderProvider;

	@Inject
	private Provider<ManufOrderWorkflowService> manufOrderWorkflowProvider;
	
	private static final Logger LOG = LoggerFactory.getLogger(ManufOrderController.class);
	
	public void propagateIsToInvoice (ActionRequest request, ActionResponse response) {

		ManufOrder manufOrder = request.getContext().asType( ManufOrder.class );

		manufOrderProvider.get().propagateIsToInvoice(ManufOrder.find(manufOrder.getId()));
		
		response.setReload(true);
		
	}
	
	
//	public void copyToConsume (ActionRequest request, ActionResponse response) {
//
//		ManufOrder manufOrder = request.getContext().asType( ManufOrder.class );
//
//		manufOrderService.copyToConsume(ManufOrder.find(manufOrder.getId()));
//		
//		response.setReload(true);
//		
//	}
	
	
//	public void copyToProduce (ActionRequest request, ActionResponse response) {
//	
//		ManufOrder manufOrder = request.getContext().asType( ManufOrder.class );
//
//		manufOrderService.copyToProduce(ManufOrder.find(manufOrder.getId()));
//		
//		response.setReload(true);
//		
//	}
	
	
	public void start (ActionRequest request, ActionResponse response) {
		
		ManufOrder manufOrder = request.getContext().asType( ManufOrder.class );

		manufOrderWorkflowProvider.get().start(ManufOrder.find(manufOrder.getId()));
		
		response.setReload(true);
		
	}
	
	public void pause (ActionRequest request, ActionResponse response) {
		
		ManufOrder manufOrder = request.getContext().asType( ManufOrder.class );

		manufOrderWorkflowProvider.get().pause(ManufOrder.find(manufOrder.getId()));
		
		response.setReload(true);
		
	}
	
	public void resume (ActionRequest request, ActionResponse response) {
		
		ManufOrder manufOrder = request.getContext().asType( ManufOrder.class );

		manufOrderWorkflowProvider.get().resume(ManufOrder.find(manufOrder.getId()));
		
		response.setReload(true);
		
	}
	
	public void finish (ActionRequest request, ActionResponse response) throws AxelorException {
		
		ManufOrder manufOrder = request.getContext().asType( ManufOrder.class );

		manufOrderWorkflowProvider.get().finish(ManufOrder.find(manufOrder.getId()));
		
		response.setReload(true);
		
	}
	
	public void cancel (ActionRequest request, ActionResponse response) throws AxelorException {
		
		ManufOrder manufOrder = request.getContext().asType( ManufOrder.class );

		manufOrderWorkflowProvider.get().cancel(ManufOrder.find(manufOrder.getId()));
		
		response.setReload(true);
		
	}
	
	
	
	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void print(ActionRequest request, ActionResponse response) {


		ManufOrder manufOrder = request.getContext().asType( ManufOrder.class );
		String manufOrderIds = "";

		@SuppressWarnings("unchecked")
		List<Integer> lstSelectedManufOrder = (List<Integer>) request.getContext().get("_ids");
		if(lstSelectedManufOrder != null){
			for(Integer it : lstSelectedManufOrder) {
				manufOrderIds+= it.toString()+",";
			}
		}	
			
		if(!manufOrderIds.equals("")){
			manufOrderIds = "&ManufOrderId="+manufOrderIds.substring(0, manufOrderIds.length()-1);	
			manufOrder = ManufOrder.find(new Long(lstSelectedManufOrder.get(0)));
		}else if(manufOrder.getId() != null){
			manufOrderIds = "&ManufOrderId="+manufOrder.getId();			
		}
		
		if(!manufOrderIds.equals("")){
			StringBuilder url = new StringBuilder();			
			AxelorSettings axelorSettings = AxelorSettings.get();
			
			User user = AuthUtils.getUser();
			Company company = manufOrder.getCompany();
			
			String language = "en";
			if(user != null && !Strings.isNullOrEmpty(user.getLanguage()))  {
				language = user.getLanguage();
			}
			else if(company != null && company.getPrintingSettings() != null && !Strings.isNullOrEmpty(company.getPrintingSettings().getLanguageSelect())) {
				language = company.getPrintingSettings().getLanguageSelect();
			}

			url.append(axelorSettings.get("axelor.report.engine", "")+"/frameset?__report=report/ManufOrder.rptdesign&__format=pdf&Locale="+language+manufOrderIds+"&__locale=fr_FR"+axelorSettings.get("axelor.report.engine.datasource"));

			LOG.debug("URL : {}", url);
			
			String urlNotExist = URLService.notExist(url.toString());
			if (urlNotExist == null){
				LOG.debug("Impression de l'O.F.  "+manufOrder.getManufOrderSeq()+" : "+url.toString());
				
				String title = " ";
				if(manufOrder.getManufOrderSeq() != null)  {
					title += lstSelectedManufOrder == null ? "OF "+manufOrder.getManufOrderSeq():"OFs";
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
			response.setFlash("Please select the Manufacturing order(s) to print.");
		}	
	}
	
	
}
