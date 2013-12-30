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
import com.axelor.apps.supplychain.db.SalesOrderSubLine;
import com.axelor.apps.supplychain.service.SalesOrderSubLineService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class SalesOrderSubLineController {

	@Inject
	private SalesOrderSubLineService salesOrderSubLineService;
	
	@Inject
	private PriceListService priceListService;

	public void compute(ActionRequest request, ActionResponse response){

		SalesOrderSubLine salesOrderSubLine = request.getContext().asType(SalesOrderSubLine.class);

		BigDecimal exTaxTotal = BigDecimal.ZERO;
		BigDecimal companyExTaxTotal = BigDecimal.ZERO;

		try {
			if (salesOrderSubLine.getPrice() != null && salesOrderSubLine.getQty() != null) {
				exTaxTotal = SalesOrderSubLineService.computeAmount(salesOrderSubLine.getQty(), salesOrderSubLineService.computeDiscount(salesOrderSubLine));
			}
			
			if(exTaxTotal != null) {

				SalesOrder salesOrder = null;
				
				if(salesOrderSubLine.getSalesOrderLine() != null)  {
					salesOrder = salesOrderSubLine.getSalesOrderLine().getSalesOrder();
				}
				if(salesOrder == null && request.getContext().getParentContext() != null) {
					salesOrder = request.getContext().getParentContext().getParentContext().asType(SalesOrder.class);
				}

				if(salesOrder != null) {
					companyExTaxTotal = salesOrderSubLineService.getCompanyExTaxTotal(exTaxTotal, salesOrder);
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

		SalesOrderSubLine salesOrderSubLine = request.getContext().asType(SalesOrderSubLine.class);

		SalesOrder salesOrder = null;

		if(salesOrderSubLine.getSalesOrderLine() != null && salesOrderSubLine.getSalesOrderLine().getSalesOrder() != null) {
			salesOrder = salesOrderSubLine.getSalesOrderLine().getSalesOrder();
		}
		if(salesOrder == null) {
			salesOrder = request.getContext().getParentContext().getParentContext().asType(SalesOrder.class);
		}

		if(salesOrder != null && salesOrderSubLine.getProduct() != null) {
			try  {
				BigDecimal price = salesOrderSubLineService.getUnitPrice(salesOrder, salesOrderSubLine);
				
				response.setValue("taxLine", salesOrderSubLineService.getTaxLine(salesOrder, salesOrderSubLine));
				response.setValue("price", salesOrderSubLineService.getUnitPrice(salesOrder, salesOrderSubLine));
				response.setValue("productName", salesOrderSubLine.getProduct().getName());
				response.setValue("unit", salesOrderSubLine.getProduct().getUnit());
				response.setValue("companyCostPrice", salesOrderSubLineService.getCompanyCostPrice(salesOrder, salesOrderSubLine));
				
				PriceList priceList = salesOrder.getPriceList();
				if(priceList != null)  {
					PriceListLine priceListLine = salesOrderSubLineService.getPriceListLine(salesOrderSubLine, priceList);
					
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
		else {
			this.resetProductInformation(response);
		}
	}
	
	
	public void resetProductInformation(ActionResponse response)  {
		
		response.setValue("taxLine", null);
		response.setValue("productName", null);
		response.setValue("unit", null);
		response.setValue("companyCostPrice", null);
		response.setValue("discountAmount", null);
		response.setValue("discountTypeSelect", null);
		response.setValue("price", null);
		
	}
	
	
//	TODO getDiscount
}
