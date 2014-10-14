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
package com.axelor.apps.sale.web;

import java.math.BigDecimal;
import java.util.Map;

import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderSubLine;
import com.axelor.apps.sale.service.SaleOrderSubLineService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class SaleOrderSubLineController {

	@Inject
	private SaleOrderSubLineService saleOrderSubLineService;
	
	@Inject
	private PriceListService priceListService;

	public void compute(ActionRequest request, ActionResponse response){

		SaleOrderSubLine saleOrderSubLine = request.getContext().asType(SaleOrderSubLine.class);

		BigDecimal exTaxTotal = BigDecimal.ZERO;
		BigDecimal companyExTaxTotal = BigDecimal.ZERO;

		try {
			if (saleOrderSubLine.getPrice() != null && saleOrderSubLine.getQty() != null) {
				exTaxTotal = SaleOrderSubLineService.computeAmount(saleOrderSubLine.getQty(), saleOrderSubLineService.computeDiscount(saleOrderSubLine));
			}
			
			if(exTaxTotal != null) {

				SaleOrder saleOrder = null;
				
				if(saleOrderSubLine.getSaleOrderLine() != null)  {
					saleOrder = saleOrderSubLine.getSaleOrderLine().getSaleOrder();
				}
				if(saleOrder == null && request.getContext().getParentContext() != null) {
					saleOrder = request.getContext().getParentContext().getParentContext().asType(SaleOrder.class);
				}

				if(saleOrder != null) {
					companyExTaxTotal = saleOrderSubLineService.getCompanyExTaxTotal(exTaxTotal, saleOrder);
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

		SaleOrderSubLine saleOrderSubLine = request.getContext().asType(SaleOrderSubLine.class);

		SaleOrder saleOrder = null;

		if(saleOrderSubLine.getSaleOrderLine() != null && saleOrderSubLine.getSaleOrderLine().getSaleOrder() != null) {
			saleOrder = saleOrderSubLine.getSaleOrderLine().getSaleOrder();
		}
		if(saleOrder == null) {
			saleOrder = request.getContext().getParentContext().getParentContext().asType(SaleOrder.class);
		}

		if(saleOrder != null && saleOrderSubLine.getProduct() != null) {
			try  {
				BigDecimal price = saleOrderSubLineService.getUnitPrice(saleOrder, saleOrderSubLine);
				
				response.setValue("taxLine", saleOrderSubLineService.getTaxLine(saleOrder, saleOrderSubLine));
				response.setValue("price", saleOrderSubLineService.getUnitPrice(saleOrder, saleOrderSubLine));
				response.setValue("productName", saleOrderSubLine.getProduct().getName());
				response.setValue("unit", saleOrderSubLine.getProduct().getUnit());
				response.setValue("companyCostPrice", saleOrderSubLineService.getCompanyCostPrice(saleOrder, saleOrderSubLine));
				
				PriceList priceList = saleOrder.getPriceList();
				if(priceList != null)  {
					PriceListLine priceListLine = saleOrderSubLineService.getPriceListLine(saleOrderSubLine, priceList);
					
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
