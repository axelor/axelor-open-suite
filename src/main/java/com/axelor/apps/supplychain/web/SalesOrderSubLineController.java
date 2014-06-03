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
