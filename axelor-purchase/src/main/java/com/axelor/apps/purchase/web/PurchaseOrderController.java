/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.birt.core.exception.BirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.purchase.exception.IExceptionMessage;
import com.axelor.apps.purchase.report.IReport;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Inject;

public class PurchaseOrderController {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject
	private PurchaseOrderService purchaseOrderService;
	
	@Inject
	private PurchaseOrderRepository purchaseOrderRepo;

	public void setSequence(ActionRequest request, ActionResponse response) throws AxelorException {

		PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);

		if(purchaseOrder != null &&  purchaseOrder.getCompany() != null) {

			response.setValue("purchaseOrderSeq", purchaseOrderService.getSequence(purchaseOrder.getCompany()));
		}
	}

	public void compute(ActionRequest request, ActionResponse response)  {

		PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);

		if(purchaseOrder != null) {
			try {
				purchaseOrder = purchaseOrderService.computePurchaseOrder(purchaseOrder);
				response.setValues(purchaseOrder);
			}
			catch(Exception e)  { TraceBackService.trace(response, e); }
		}
	}


	public void validateSupplier(ActionRequest request, ActionResponse response) {

		PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);

		response.setValue("supplierPartner", purchaseOrderService.validateSupplier(purchaseOrder));

	}



	/**
	 * Fonction appeler par le bouton imprimer
	 *
	 * @param request
	 * @param response
	 * @return
	 * @throws BirtException 
	 * @throws IOException 
	 */
	public void showPurchaseOrder(ActionRequest request, ActionResponse response) throws AxelorException {

		PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);

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

		String title = I18n.get("Purchase order");
		if(purchaseOrder.getPurchaseOrderSeq() != null)  {
			title += purchaseOrder.getPurchaseOrderSeq();
		}

		String fileLink = ReportFactory.createReport(IReport.PURCHASE_ORDER, title+"-${date}")
				.addParam("PurchaseOrderId", purchaseOrderIds)
				.addParam("Locale", language)
				.generate()
				.getFileLink();

		logger.debug("Printing "+title);
	
		response.setView(ActionView
				.define(title)
				.add("html", fileLink).map());	
			
	}

	public void requestPurchaseOrder(ActionRequest request, ActionResponse response) throws Exception {

		PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);

		purchaseOrderService.requestPurchaseOrder(purchaseOrderRepo.find(purchaseOrder.getId()));

		response.setReload(true);

	}
	
	public void updateCostPrice(ActionRequest request, ActionResponse response) throws Exception {

		PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
		purchaseOrderService.updateCostPrice(purchaseOrderRepo.find(purchaseOrder.getId()));

	}

	//Generate single purchase order from several
	@SuppressWarnings("rawtypes")
	public void mergePurchaseOrder(ActionRequest request, ActionResponse response)  {
		List<PurchaseOrder> purchaseOrderList = new ArrayList<PurchaseOrder>();
		List<Long> purchaseOrderIdList = new ArrayList<Long>();
		boolean fromPopup = false;

		if (request.getContext().get("purchaseOrderToMerge") != null){

			if (request.getContext().get("purchaseOrderToMerge") instanceof List){
				//No confirmation popup, purchase orders are content in a parameter list
				List<Map> purchaseOrderMap = (List<Map>)request.getContext().get("purchaseOrderToMerge");
				for (Map map : purchaseOrderMap) {
					purchaseOrderIdList.add(new Long((Integer)map.get("id")));
				}
			}else{
				//After confirmation popup, purchase order's id are in a string separated by ","
				String purchaseOrderIdListStr = (String)request.getContext().get("purchaseOrderToMerge");
				for (String purchaseOrderId : purchaseOrderIdListStr.split(",")) {
					purchaseOrderIdList.add(new Long(purchaseOrderId));
				}
				fromPopup = true;
			}
		}

		//Check if currency, supplierPartner and company are the same for all selected purchase orders
		Currency commonCurrency = null;
		Partner commonSupplierPartner = null;
		Company commonCompany = null;
		Partner commonContactPartner = null;
		//Useful to determine if a difference exists between contact partners of all purchase orders
		boolean existContactPartnerDiff = false;
		PriceList commonPriceList = null;
		//Useful to determine if a difference exists between price lists of all purchase orders
		boolean existPriceListDiff = false;
		PurchaseOrder purchaseOrderTemp;
		int count = 1;
		for (Long purchaseOrderId : purchaseOrderIdList) {
			purchaseOrderTemp = JPA.em().find(PurchaseOrder.class, purchaseOrderId);
			purchaseOrderList.add(purchaseOrderTemp);
			if(count == 1){
				commonCurrency = purchaseOrderTemp.getCurrency();
				commonSupplierPartner = purchaseOrderTemp.getSupplierPartner();
				commonCompany = purchaseOrderTemp.getCompany();
				commonContactPartner = purchaseOrderTemp.getContactPartner();
				commonPriceList = purchaseOrderTemp.getPriceList();
			}else{
				if (commonCurrency != null
						&& !commonCurrency.equals(purchaseOrderTemp.getCurrency())){
					commonCurrency = null;
				}
				if (commonSupplierPartner != null
						&& !commonSupplierPartner.equals(purchaseOrderTemp.getSupplierPartner())){
					commonSupplierPartner = null;
				}
				if (commonCompany != null
						&& !commonCompany.equals(purchaseOrderTemp.getCompany())){
					commonCompany = null;
				}
				if (commonContactPartner != null
						&& !commonContactPartner.equals(purchaseOrderTemp.getContactPartner())){
					commonContactPartner = null;
					existContactPartnerDiff = true;
				}
				if (commonPriceList != null
						&& !commonPriceList.equals(purchaseOrderTemp.getPriceList())){
					commonPriceList = null;
					existPriceListDiff = true;
				}
			}
			count++;
		}

		StringBuilder fieldErrors = new StringBuilder();
		if (commonCurrency == null){
			fieldErrors.append(I18n.get(IExceptionMessage.PURCHASE_ORDER_MERGE_ERROR_CURRENCY));
		}
		if (commonSupplierPartner == null){
			if (fieldErrors.length() > 0){
				fieldErrors.append("<br/>");
			}
			fieldErrors.append(I18n.get(IExceptionMessage.PURCHASE_ORDER_MERGE_ERROR_SUPPLIER_PARTNER));
		}
		if (commonCompany == null){
			if (fieldErrors.length() > 0){
				fieldErrors.append("<br/>");
			}
			fieldErrors.append(I18n.get(IExceptionMessage.PURCHASE_ORDER_MERGE_ERROR_COMPANY));
		}

		if (fieldErrors.length() > 0){
			response.setFlash(fieldErrors.toString());
			return;
		}

		//Check if priceList or contactPartner are content in parameters
		if (request.getContext().get("priceList") != null){
			commonPriceList = JPA.em().find(PriceList.class, new Long((Integer)((Map)request.getContext().get("priceList")).get("id")));
		}
		if (request.getContext().get("contactPartner") != null){
			commonContactPartner = JPA.em().find(Partner.class, new Long((Integer)((Map)request.getContext().get("contactPartner")).get("id")));
		}

		if (!fromPopup
				&& (existContactPartnerDiff || existPriceListDiff)){
			//Need to display intermediate screen to select some values
			ActionViewBuilder confirmView = ActionView
										.define("Confirm merge purchase order")
										.model(Wizard.class.getName())
										.add("form", "purchase-order-merge-confirm-form")
										.param("popup", "true")
										.param("show-toolbar", "false")
										.param("show-confirm", "false")
										.param("popup-save", "false")
										.param("forceEdit", "true");

			if (existPriceListDiff){
				confirmView.context("contextPriceListToCheck", "true");
			}
			if (existContactPartnerDiff){
				confirmView.context("contextContactPartnerToCheck", "true");
				confirmView.context("contextPartnerId", commonSupplierPartner.getId().toString());
			}

			confirmView.context("purchaseOrderToMerge", Joiner.on(",").join(purchaseOrderIdList));

			response.setView(confirmView.map());

			return;
		}


		try{
			PurchaseOrder purchaseOrder = purchaseOrderService.mergePurchaseOrders(purchaseOrderList, commonCurrency, commonSupplierPartner, commonCompany, commonContactPartner, commonPriceList);
			if (purchaseOrder != null){
				//Open the generated purchase order in a new tab
				response.setView(ActionView
						.define("Purchase Order")
						.model(PurchaseOrder.class.getName())
						.add("grid", "purchase-order-grid")
						.add("form", "purchase-order-form")
						.param("forceEdit", "true")
						.context("_showRecord", String.valueOf(purchaseOrder.getId())).map());
				response.setCanClose(true);
			}
		}catch(AxelorException ae){
			response.setFlash(ae.getLocalizedMessage());
		}
	}
}
