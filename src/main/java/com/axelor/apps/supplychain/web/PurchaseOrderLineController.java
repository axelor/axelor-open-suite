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

import java.math.BigDecimal;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.supplychain.db.PurchaseOrder;
import com.axelor.apps.supplychain.db.PurchaseOrderLine;
import com.axelor.apps.supplychain.service.PurchaseOrderLineService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class PurchaseOrderLineController {

	private static final Logger LOG = LoggerFactory.getLogger(PurchaseOrderLineController.class); 
	
	@Inject
	private PurchaseOrderLineService purchaseOrderLineService;
	
	@Inject
	private PriceListService priceListService;
	
	public void compute(ActionRequest request, ActionResponse response){
		
		PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
		BigDecimal exTaxTotal = BigDecimal.ZERO;
		BigDecimal companyExTaxTotal = BigDecimal.ZERO;
		
		try{
			if (purchaseOrderLine.getPrice() != null && purchaseOrderLine.getQty() != null){
				
				exTaxTotal = PurchaseOrderLineService.computeAmount(purchaseOrderLine.getQty(), purchaseOrderLineService.computeDiscount(purchaseOrderLine));
			
			}
			
			if(exTaxTotal != null) {

				PurchaseOrder purchaseOrder = purchaseOrderLine.getPurchaseOrder();

				if(purchaseOrder == null) {
					purchaseOrder = request.getContext().getParentContext().asType(PurchaseOrder.class);
				}

				if(purchaseOrder != null) {
					companyExTaxTotal = purchaseOrderLineService.getCompanyExTaxTotal(exTaxTotal, purchaseOrder);
					response.setValue("saleMinPrice", purchaseOrderLineService.getMinSalePrice(purchaseOrder, purchaseOrderLine));
					response.setValue("salePrice", purchaseOrderLineService.getSalePrice(purchaseOrder, purchaseOrderLine.getPrice()));
				}
				
			}
			
			response.setValue("exTaxTotal", exTaxTotal);
			response.setValue("companyExTaxTotal", companyExTaxTotal);
		}
		catch(Exception e)  {
			response.setFlash(e.getMessage());
		}	
	}
	
	public void getProductInformation(ActionRequest request, ActionResponse response){
		
		PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
		
		PurchaseOrder purchaseOrder = purchaseOrderLine.getPurchaseOrder();
		
		if(purchaseOrder == null)  {
			purchaseOrder = request.getContext().getParentContext().asType(PurchaseOrder.class);
		}
			
		if(purchaseOrder != null && purchaseOrderLine.getProduct() != null)  {
			
			try  {
				BigDecimal price = purchaseOrderLineService.getUnitPrice(purchaseOrder, purchaseOrderLine);
				response.setValue("taxLine", purchaseOrderLineService.getTaxLine(purchaseOrder, purchaseOrderLine));
				response.setValue("productName", purchaseOrderLine.getProduct().getName());
				response.setValue("unit", purchaseOrderLine.getProduct().getUnit());
				response.setValue("qty", purchaseOrderLineService.getQty(purchaseOrder,purchaseOrderLine));
				
				response.setValue("saleMinPrice", purchaseOrderLineService.getMinSalePrice(purchaseOrder, purchaseOrderLine));
				response.setValue("salePrice", purchaseOrderLineService.getSalePrice(purchaseOrder, price));
				PriceList priceList = purchaseOrder.getPriceList();
				if(priceList != null)  {
					PriceListLine priceListLine = purchaseOrderLineService.getPriceListLine(purchaseOrderLine, priceList);
					
					Map<String, Object> discounts = priceListService.getDiscounts(priceList, priceListLine, price);
					
					response.setValue("discountAmount", discounts.get("discountAmount"));
					response.setValue("discountTypeSelect", discounts.get("discountTypeSelect"));
					if(discounts.get("price") != null)  {
						price = (BigDecimal) discounts.get("price");
					}
				}
				response.setValue("price", price);
			}
			catch(Exception e) {
				e.printStackTrace();
				response.setFlash(e.getMessage());
				this.resetProductInformation(response);
			}
		}
		else  {
			this.resetProductInformation(response);
		}
	}
	
	
	public void resetProductInformation(ActionResponse response)  {
		
		response.setValue("taxLine", null);
		response.setValue("productName", null);
		response.setValue("unit", null);
		response.setValue("discountAmount", null);
		response.setValue("discountTypeSelect", null);
		response.setValue("price", null);
		response.setValue("saleMinPrice", null);
		response.setValue("salePrice", null);
		
	}
	
	
	public void getDiscount(ActionRequest request, ActionResponse response){
		
		PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
		
		PurchaseOrder purchaseOrder = purchaseOrderLine.getPurchaseOrder();
		
		if(purchaseOrder == null)  {
			purchaseOrder = request.getContext().getParentContext().asType(PurchaseOrder.class);
		}
			
		if(purchaseOrder != null && purchaseOrderLine.getProduct() != null)  {
			
			try  {
				BigDecimal price = purchaseOrderLine.getPrice();
				
				PriceList priceList = purchaseOrder.getPriceList();
				if(priceList != null)  {
					PriceListLine priceListLine = purchaseOrderLineService.getPriceListLine(purchaseOrderLine, priceList);
					
					Map<String, Object> discounts = priceListService.getDiscounts(priceList, priceListLine, price);
					
					response.setValue("discountAmount", discounts.get("discountAmount"));
					response.setValue("discountTypeSelect", discounts.get("discountTypeSelect"));
					if(discounts.get("price") != null)  {
						price = (BigDecimal) discounts.get("price");
					}
				}
				
				response.setValue("price", price);
			}
			catch(Exception e) {
				response.setFlash(e.getMessage());
			}
		}
	}
}
