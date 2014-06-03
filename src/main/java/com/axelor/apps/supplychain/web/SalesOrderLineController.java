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
import com.axelor.apps.production.db.BillOfMaterial;
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
	
	public void customizeBillOfMaterial(ActionRequest request, ActionResponse response) {

		SalesOrderLine salesOrderLine = request.getContext().asType(SalesOrderLine.class);
		
		BillOfMaterial copyBillOfMaterial = salesOrderLineService.customizeBillOfMaterial(salesOrderLine);
		
		if(copyBillOfMaterial != null)  {
		
			response.setValue("billOfMaterial", copyBillOfMaterial);
			response.setFlash("Nomenclature personnalisé créée");
		}
		
	}
	
}
