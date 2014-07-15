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
package com.axelor.apps.sale.web;

import java.math.BigDecimal;
import java.util.Map;

import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.SaleOrderLineService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class SaleOrderLineController {

	@Inject
	private SaleOrderLineService saleOrderLineService;
	
	@Inject
	private PriceListService priceListService;

	public void compute(ActionRequest request, ActionResponse response) {

		SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
		
		BigDecimal exTaxTotal = BigDecimal.ZERO;
		BigDecimal companyExTaxTotal = BigDecimal.ZERO;

		try{
			if (saleOrderLine.getPrice() != null && saleOrderLine.getQty() != null) {
				if(saleOrderLine.getSaleOrderSubLineList() == null || saleOrderLine.getSaleOrderSubLineList().isEmpty()) {
					exTaxTotal = SaleOrderLineService.computeAmount(saleOrderLine.getQty(), saleOrderLineService.computeDiscount(saleOrderLine));
				}
			}
			
			if(exTaxTotal != null) {

				SaleOrder saleOrder = saleOrderLine.getSaleOrder();

				if(saleOrder == null) {
					saleOrder = request.getContext().getParentContext().asType(SaleOrder.class);
				}

				if(saleOrder != null) {
					companyExTaxTotal = saleOrderLineService.getCompanyExTaxTotal(exTaxTotal, saleOrder);
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

		SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);	

		SaleOrder saleOrder = saleOrderLine.getSaleOrder();
		if(saleOrder == null)  {
			saleOrder = request.getContext().getParentContext().asType(SaleOrder.class);
		}

		if(saleOrder != null && saleOrderLine.getProduct() != null) {

			try  {
				BigDecimal price = saleOrderLineService.getUnitPrice(saleOrder, saleOrderLine);
				
				response.setValue("taxLine", saleOrderLineService.getTaxLine(saleOrder, saleOrderLine));
				response.setValue("productName", saleOrderLine.getProduct().getName());
				response.setValue("saleSupplySelect", saleOrderLine.getProduct().getSaleSupplySelect());
				response.setValue("unit", saleOrderLine.getProduct().getUnit());
				response.setValue("companyCostPrice", saleOrderLineService.getCompanyCostPrice(saleOrder, saleOrderLine));
				
				PriceList priceList = saleOrder.getPriceList();
				if(priceList != null)  {
					PriceListLine priceListLine = saleOrderLineService.getPriceListLine(saleOrderLine, priceList);
					
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

		SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);	

		SaleOrder saleOrder = saleOrderLine.getSaleOrder();
		if(saleOrder == null)  {
			saleOrder = request.getContext().getParentContext().asType(SaleOrder.class);
		}

		if(saleOrder != null && saleOrderLine.getProduct() != null) {

			try  {
				BigDecimal price = saleOrderLine.getPrice();
				
				PriceList priceList = saleOrder.getPriceList();
				if(priceList != null)  {
					PriceListLine priceListLine = saleOrderLineService.getPriceListLine(saleOrderLine, priceList);
					
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
	
	public void customizeBillOfMaterial(ActionRequest request, ActionResponse response) {

		SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
		
		BillOfMaterial copyBillOfMaterial = saleOrderLineService.customizeBillOfMaterial(saleOrderLine);
		
		if(copyBillOfMaterial != null)  {
		
			response.setValue("billOfMaterial", copyBillOfMaterial);
			response.setFlash("Nomenclature personnalisé créée");
		}
		
	}
	
}
