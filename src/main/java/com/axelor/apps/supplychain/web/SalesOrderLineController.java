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

import java.math.BigDecimal;
import java.util.Map;

import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.db.SalesOrderLine;
import com.axelor.apps.supplychain.service.SalesOrderLineService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class SalesOrderLineController {

	@Inject
	private SalesOrderLineService salesOrderLineService;
	
	@Inject
	private PriceListService priceListService;

	public void compute(ActionRequest request, ActionResponse response) {

		SalesOrderLine salesOrderLine = request.getContext().asType(SalesOrderLine.class);
		
		BigDecimal exTaxTotal = BigDecimal.ZERO;
		BigDecimal companyExTaxTotal = BigDecimal.ZERO;

		try{
			if (salesOrderLine.getPrice() != null && salesOrderLine.getQty() != null) {
				if(salesOrderLine.getSalesOrderSubLineList() == null || salesOrderLine.getSalesOrderSubLineList().isEmpty()) {
					exTaxTotal = SalesOrderLineService.computeAmount(salesOrderLine.getQty(), salesOrderLineService.computeDiscount(salesOrderLine));
				}
			}
			
			if(exTaxTotal != null) {

				SalesOrder salesOrder = salesOrderLine.getSalesOrder();

				if(salesOrder == null) {
					salesOrder = request.getContext().getParentContext().asType(SalesOrder.class);
				}

				if(salesOrder != null) {
					companyExTaxTotal = salesOrderLineService.getCompanyExTaxTotal(exTaxTotal, salesOrder);
				}
			}
			
			response.setValue("exTaxTotal", exTaxTotal);
			response.setValue("companyExTaxTotal", companyExTaxTotal);
		}
		catch(Exception e) {
			response.setFlash(e.getMessage()); 
		}
	}

	public void getProductInformation(ActionRequest request, ActionResponse response) {

		SalesOrderLine salesOrderLine = request.getContext().asType(SalesOrderLine.class);	

		SalesOrder salesOrder = salesOrderLine.getSalesOrder();
		if(salesOrder == null)  {
			salesOrder = request.getContext().getParentContext().asType(SalesOrder.class);
		}

		if(salesOrder != null && salesOrderLine.getProduct() != null) {

			try  {
				BigDecimal price = salesOrderLineService.getUnitPrice(salesOrder, salesOrderLine);
				
				response.setValue("taxLine", salesOrderLineService.getTaxLine(salesOrder, salesOrderLine));
				response.setValue("productName", salesOrderLine.getProduct().getName());
				response.setValue("saleSupplySelect", salesOrderLine.getProduct().getSaleSupplySelect());
				response.setValue("unit", salesOrderLine.getProduct().getUnit());
				response.setValue("companyCostPrice", salesOrderLineService.getCompanyCostPrice(salesOrder, salesOrderLine));
				
				PriceList priceList = salesOrder.getPriceList();
				if(priceList != null)  {
					PriceListLine priceListLine = salesOrderLineService.getPriceListLine(salesOrderLine, priceList);
					
					Map<String, Object> discounts = priceListService.getDiscounts(priceList, priceListLine, price);
					
					response.setValue("discountAmount", discounts.get("discountAmount"));
					response.setValue("discountTypeSelect", discounts.get("discountTypeSelect"));
					if(discounts.get("price") != null)  {
						price = (BigDecimal) discounts.get("price");
					}
				}
				
				response.setValue("price", price);
				
			}
			catch(Exception e)  {
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
		response.setValue("saleSupplySelect", null);
		response.setValue("unit", null);
		response.setValue("companyCostPrice", null);
		response.setValue("discountAmount", null);
		response.setValue("discountTypeSelect", null);
		response.setValue("price", null);
		
	}
	
	
	public void getDiscount(ActionRequest request, ActionResponse response) {

		SalesOrderLine salesOrderLine = request.getContext().asType(SalesOrderLine.class);	

		SalesOrder salesOrder = salesOrderLine.getSalesOrder();
		if(salesOrder == null)  {
			salesOrder = request.getContext().getParentContext().asType(SalesOrder.class);
		}

		if(salesOrder != null && salesOrderLine.getProduct() != null) {

			try  {
				BigDecimal price = salesOrderLine.getPrice();
				
				PriceList priceList = salesOrder.getPriceList();
				if(priceList != null)  {
					PriceListLine priceListLine = salesOrderLineService.getPriceListLine(salesOrderLine, priceList);
					
					Map<String, Object> discounts = priceListService.getDiscounts(priceList, priceListLine, price);
					
					response.setValue("discountAmount", discounts.get("discountAmount"));
					response.setValue("discountTypeSelect", discounts.get("discountTypeSelect"));
					if(discounts.get("price") != null)  {
						price = (BigDecimal) discounts.get("price");
					}
				}
				
				response.setValue("price", price);
				
			}
			catch(Exception e)  {
				response.setFlash(e.getMessage()); 
			}
		}
	}
}
