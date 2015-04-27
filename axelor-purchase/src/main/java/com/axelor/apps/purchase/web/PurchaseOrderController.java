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
package com.axelor.apps.purchase.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportSettings;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.report.IReport;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.apps.tool.net.URLService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class PurchaseOrderController {

	private static final Logger LOG = LoggerFactory.getLogger(PurchaseOrderController.class);
	
	public void setSequence(ActionRequest request, ActionResponse response) throws AxelorException {
		
		PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
		
		if(purchaseOrder != null &&  purchaseOrder.getCompany() != null) {
			
			response.setValue("purchaseOrderSeq", Beans.get(PurchaseOrderService.class).getSequence(purchaseOrder.getCompany()));
		}
	}
	
	public void compute(ActionRequest request, ActionResponse response)  {
		
		PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);

		if(purchaseOrder != null) {
			try {
				purchaseOrder = Beans.get(PurchaseOrderService.class).computePurchaseOrder(purchaseOrder);
				response.setValues(purchaseOrder);
			}
			catch(Exception e)  { TraceBackService.trace(response, e); }
		}
	}
	
	
	public void validateSupplier(ActionRequest request, ActionResponse response) {
		
		PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
			
		response.setValue("supplierPartner", Beans.get(PurchaseOrderService.class).validateSupplier(purchaseOrder));
		
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
		String purchaseOrderIds = "";

		@SuppressWarnings("unchecked")
		List<Integer> lstSelectedPurchaseOrder = (List<Integer>) request.getContext().get("_ids");
		if(lstSelectedPurchaseOrder != null){
			for(Integer it : lstSelectedPurchaseOrder) {
				purchaseOrderIds+= it.toString()+",";
			}
		}
			
		if(!purchaseOrderIds.equals("")){
			purchaseOrderIds = purchaseOrderIds.substring(0,purchaseOrderIds.length()-1);
			purchaseOrder = Beans.get(PurchaseOrderRepository.class).find(new Long(lstSelectedPurchaseOrder.get(0)));
		}else if(purchaseOrder.getId() != null){
			purchaseOrderIds = purchaseOrder.getId().toString();
		}
		String language="";
		try{
			language = purchaseOrder.getSupplierPartner().getLanguageSelect() != null? purchaseOrder.getSupplierPartner().getLanguageSelect() : purchaseOrder.getCompany().getPrintingSettings().getLanguageSelect() != null ? purchaseOrder.getCompany().getPrintingSettings().getLanguageSelect() : "en" ; 
		}catch (NullPointerException e) {
			language = "en";
		}
		language = language.equals("")? "en": language;
		
		url.append(
				new ReportSettings(IReport.PURCHASE_ORDER)
				.addParam("Locale", language)
				.addParam("__locale", "fr_FR")
				.addParam("PurchaseOrderId", purchaseOrderIds)
				.getUrl());
		
		LOG.debug("URL : {}", url);
		String urlNotExist = URLService.notExist(url.toString());
		
		if(urlNotExist == null) {
		
			LOG.debug("Impression du devis "+purchaseOrder.getPurchaseOrderSeq() +" : "+url.toString());
			
			String title = I18n.get("Devis");
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
	public void setDraftSequence(ActionRequest request,ActionResponse response){
		PurchaseOrder purchaseOrder=request.getContext().asType(PurchaseOrder.class);
		if(purchaseOrder.getPurchaseOrderSeq()!=null){
			return;
		}
		response.setValue("purchaseOrderSeq","*"+purchaseOrder.getId().toString());
	}
}
