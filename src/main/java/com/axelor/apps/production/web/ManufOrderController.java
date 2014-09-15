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
package com.axelor.apps.production.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportSettings;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.report.IReport;
import com.axelor.apps.production.service.ManufOrderService;
import com.axelor.apps.production.service.ManufOrderWorkflowService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;
import com.google.inject.Provider;

public class ManufOrderController {

	@Inject
	private Provider<ManufOrderService> manufOrderProvider;

	@Inject
	private Provider<ManufOrderWorkflowService> manufOrderWorkflowProvider;
	
	private static final Logger LOG = LoggerFactory.getLogger(ManufOrderController.class);
	
	public void propagateIsToInvoice (ActionRequest request, ActionResponse response) {

		ManufOrder manufOrder = request.getContext().asType( ManufOrder.class );

		manufOrderProvider.get().propagateIsToInvoice(manufOrderProvider.get().find(manufOrder.getId()));
		
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

		manufOrderWorkflowProvider.get().start(manufOrderProvider.get().find(manufOrder.getId()));
		
		response.setReload(true);
		
	}
	
	public void pause (ActionRequest request, ActionResponse response) {
		
		ManufOrder manufOrder = request.getContext().asType( ManufOrder.class );

		manufOrderWorkflowProvider.get().pause(manufOrderProvider.get().find(manufOrder.getId()));
		
		response.setReload(true);
		
	}
	
	public void resume (ActionRequest request, ActionResponse response) {
		
		ManufOrder manufOrder = request.getContext().asType( ManufOrder.class );

		manufOrderWorkflowProvider.get().resume(manufOrderProvider.get().find(manufOrder.getId()));
		
		response.setReload(true);
		
	}
	
	public void finish (ActionRequest request, ActionResponse response) throws AxelorException {
		
		ManufOrder manufOrder = request.getContext().asType( ManufOrder.class );

		manufOrderWorkflowProvider.get().finish(manufOrderProvider.get().find(manufOrder.getId()));
		
		response.setReload(true);
		
	}
	
	public void cancel (ActionRequest request, ActionResponse response) throws AxelorException {
		
		ManufOrder manufOrder = request.getContext().asType( ManufOrder.class );

		manufOrderWorkflowProvider.get().cancel(manufOrderProvider.get().find(manufOrder.getId()));
		
		response.setReload(true);
		
	}
	
	public void plan (ActionRequest request, ActionResponse response) throws AxelorException {
		
		ManufOrder manufOrder = request.getContext().asType( ManufOrder.class );

		manufOrderWorkflowProvider.get().plan(manufOrderProvider.get().find(manufOrder.getId()));
		
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
			manufOrderIds = manufOrderIds.substring(0, manufOrderIds.length()-1);	
			manufOrder = manufOrderProvider.get().find(new Long(lstSelectedManufOrder.get(0)));
		}else if(manufOrder.getId() != null){
			manufOrderIds = manufOrder.getId().toString();			
		}
		
		if(!manufOrderIds.equals("")){
			StringBuilder url = new StringBuilder();			
			
			User user = AuthUtils.getUser();
			Company company = manufOrder.getCompany();
			
			String language = "en";
			if(user != null && !Strings.isNullOrEmpty(user.getLanguage()))  {
				language = user.getLanguage();
			}
			else if(company != null && company.getPrintingSettings() != null && !Strings.isNullOrEmpty(company.getPrintingSettings().getLanguageSelect())) {
				language = company.getPrintingSettings().getLanguageSelect();
			}

			url.append(new ReportSettings(IReport.MANUF_ORDER)
						.addParam("Locale", language)
						.addParam("__locale", "fr_FR")
						.addParam("ManufOrderId", manufOrderIds)
						.getUrl());
			
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
