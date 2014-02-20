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
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.service.OperationOrderWorkflowService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaUser;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

public class OperationOrderController {

	@Inject
	private Provider<OperationOrderWorkflowService> operationOrderWorkflowProvider;

	private static final Logger LOG = LoggerFactory.getLogger(ManufOrderController.class);
	
//	public void copyToConsume (ActionRequest request, ActionResponse response) {
//
//		OperationOrder operationOrder = request.getContext().asType( OperationOrder.class );
//
//		operationOrderService.copyToConsume(OperationOrder.find(operationOrder.getId()));
//		
//		response.setReload(true);
//		
//	}
	
	
//	public void copyToProduce (ActionRequest request, ActionResponse response) {
//
//		OperationOrder operationOrder = request.getContext().asType( OperationOrder.class );
//
//		operationOrderService.copyToProduce(OperationOrder.find(operationOrder.getId()));
//		
//		response.setReload(true);
//		
//	}
	
	
	public void computeDuration(ActionRequest request, ActionResponse response) {
		
		OperationOrder operationOrder = request.getContext().asType( OperationOrder.class );
		
		OperationOrderWorkflowService operationOrderWorkflowService = operationOrderWorkflowProvider.get();
		
		if(operationOrder.getPlannedStartDateT() != null && operationOrder.getPlannedEndDateT() != null) {
			response.setValue("plannedDuration", 
					operationOrderWorkflowService.getDuration(
							operationOrderWorkflowService.computeDuration(operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT())));
		}
		
		if(operationOrder.getRealStartDateT() != null && operationOrder.getRealEndDateT() != null) {
			response.setValue("realDuration", 
					operationOrderWorkflowService.getDuration(
							operationOrderWorkflowService.computeDuration(operationOrder.getRealStartDateT(), operationOrder.getRealEndDateT())));
		}
	}
	
	
	public void plan (ActionRequest request, ActionResponse response) throws AxelorException {

		OperationOrder operationOrder = request.getContext().asType( OperationOrder.class );

		operationOrderWorkflowProvider.get().plan(OperationOrder.find(operationOrder.getId()));
		
		response.setReload(true);
		
	}
	
	
	public void finish (ActionRequest request, ActionResponse response) throws AxelorException {

		OperationOrder operationOrder = request.getContext().asType( OperationOrder.class );

		operationOrderWorkflowProvider.get().finish(OperationOrder.find(operationOrder.getId()));
		
		response.setReload(true);
		
	}
	
	
	
	
//	TODO A SUPPRIMER UNE FOIS BUG FRAMEWORK CORRIGE
	public void saveOperationOrder(ActionRequest request, ActionResponse response) throws AxelorException {
		OperationOrder operationOrder = request.getContext().asType( OperationOrder.class );
		OperationOrder persistOperationOrder = OperationOrder.find(operationOrder.getId());
		persistOperationOrder.setStatusSelect(operationOrder.getStatusSelect());
		persistOperationOrder.setRealStartDateT(operationOrder.getRealStartDateT());
		persistOperationOrder.setRealEndDateT(operationOrder.getRealEndDateT());
		
		this.saveOperationOrder(persistOperationOrder);
	}
	
	
	@Transactional
	public void saveOperationOrder(OperationOrder operationOrder){
		operationOrder.save();
	}
	
	
	
	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	public void print(ActionRequest request, ActionResponse response) {


		OperationOrder operationOrder = request.getContext().asType( OperationOrder.class );
		String operationOrderIds = "";

		@SuppressWarnings("unchecked")
		List<Integer> lstSelectedOperationOrder = (List<Integer>) request.getContext().get("_ids");
		if(lstSelectedOperationOrder != null){
			for(Integer it : lstSelectedOperationOrder) {
				operationOrderIds+= it.toString()+",";
			}
		}	
			
		if(!operationOrderIds.equals("")){
			operationOrderIds = "&OperationOrderId="+operationOrderIds.substring(0, operationOrderIds.length()-1);	
			operationOrder = OperationOrder.find(new Long(lstSelectedOperationOrder.get(0)));
		}else if(operationOrder.getId() != null){
			operationOrderIds = "&OperationOrderId="+operationOrder.getId();			
		}
		
		if(!operationOrderIds.equals("")){
			StringBuilder url = new StringBuilder();			
			AxelorSettings axelorSettings = AxelorSettings.get();
			
			MetaUser metaUser = MetaUser.findByUser( AuthUtils.getUser());
			
			Company company = null;
			if(operationOrder.getManufOrder() != null)  {
				company = operationOrder.getManufOrder().getCompany();
			}
			
			String language = "en";
			if(metaUser != null && !Strings.isNullOrEmpty(metaUser.getLanguage()))  {
				language = metaUser.getLanguage();
			}
			else if(company != null && company.getPrintingSettings() != null && !Strings.isNullOrEmpty(company.getPrintingSettings().getLanguageSelect())) {
				language = company.getPrintingSettings().getLanguageSelect();
			}

			url.append(axelorSettings.get("axelor.report.engine", "")+"/frameset?__report=report/OperationOrder.rptdesign&__format=pdf&Locale="+language+operationOrderIds+"&__locale=fr_FR"+axelorSettings.get("axelor.report.engine.datasource"));

			LOG.debug("URL : {}", url);
			
			String urlNotExist = URLService.notExist(url.toString());
			if (urlNotExist == null){
				LOG.debug("Impression de l'Opération de production "+operationOrder.getName()+" : "+url.toString());
				
				String title = " ";
				if(operationOrder.getName() != null)  {
					title += lstSelectedOperationOrder == null ? "Op "+operationOrder.getName():"Ops";
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
			response.setFlash("Please select the Operation order(s) to print.");
		}	
	}
	
	
	
	
	
	
	
}

