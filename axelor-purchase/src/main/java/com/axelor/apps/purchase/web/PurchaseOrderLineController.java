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

import java.math.BigDecimal;
import java.util.Map;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderLineServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;

public class PurchaseOrderLineController {

	@Inject
	private PurchaseOrderLineService purchaseOrderLineService;


	public void compute(ActionRequest request, ActionResponse response) throws AxelorException{

		Context context = request.getContext();
		
		PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);

		PurchaseOrder purchaseOrder = this.getPurchaseOrder(context);
		
		Product product = purchaseOrderLine.getProduct();

		try{
			if(purchaseOrder == null || product == null || purchaseOrderLine.getPrice() == null || purchaseOrderLine.getQty() == null)  {  return;  }

			BigDecimal exTaxTotal = BigDecimal.ZERO;
			BigDecimal companyExTaxTotal = BigDecimal.ZERO;
			BigDecimal inTaxTotal = BigDecimal.ZERO;
			BigDecimal companyInTaxTotal = BigDecimal.ZERO;
			BigDecimal priceDiscounted = purchaseOrderLineService.computeDiscount(purchaseOrderLine);
			response.setValue("priceDiscounted", priceDiscounted);
			response.setAttr("priceDiscounted", "hidden", priceDiscounted.compareTo(purchaseOrderLine.getPrice()) == 0);
			
			BigDecimal taxRate = BigDecimal.ZERO;
			if(purchaseOrderLine.getTaxLine() != null)  {  taxRate = purchaseOrderLine.getTaxLine().getValue();  }
			
			if(!purchaseOrder.getInAti()){
				exTaxTotal = PurchaseOrderLineServiceImpl.computeAmount(purchaseOrderLine.getQty(), purchaseOrderLineService.computeDiscount(purchaseOrderLine));
				inTaxTotal = exTaxTotal.add(exTaxTotal.multiply(taxRate));
				companyExTaxTotal = purchaseOrderLineService.getCompanyExTaxTotal(exTaxTotal, purchaseOrder);
				companyInTaxTotal = companyExTaxTotal.add(companyExTaxTotal.multiply(taxRate));
			}
			else  {
				inTaxTotal = PurchaseOrderLineServiceImpl.computeAmount(purchaseOrderLine.getQty(), purchaseOrderLineService.computeDiscount(purchaseOrderLine));
				exTaxTotal = inTaxTotal.divide(taxRate.add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
				companyInTaxTotal = purchaseOrderLineService.getCompanyExTaxTotal(inTaxTotal, purchaseOrder);
				companyExTaxTotal = companyInTaxTotal.divide(taxRate.add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
			}
			
			response.setValue("saleMinPrice", purchaseOrderLineService.getMinSalePrice(purchaseOrder, purchaseOrderLine));
			response.setValue("salePrice", purchaseOrderLineService.getSalePrice(purchaseOrder, product, purchaseOrderLine.getPrice()));
			response.setValue("exTaxTotal", exTaxTotal);
			response.setValue("inTaxTotal", inTaxTotal);
			response.setValue("companyExTaxTotal", companyExTaxTotal);
			response.setValue("companyInTaxTotal", companyInTaxTotal);

		}
		catch(Exception e)  {
			response.setFlash(e.getMessage());
		}
	}
	

	public void getProductInformation(ActionRequest request, ActionResponse response){

		Context context = request.getContext();
		
		PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);

		PurchaseOrder purchaseOrder = this.getPurchaseOrder(context);
		
		Product product = purchaseOrderLine.getProduct();

		if(purchaseOrder == null || product == null) { 
			this.resetProductInformation(response);
			return;
		}
		
		try  {
			
			TaxLine taxLine = purchaseOrderLineService.getTaxLine(purchaseOrder, purchaseOrderLine);
			response.setValue("taxLine", taxLine);
			
			BigDecimal price = purchaseOrderLineService.getUnitPrice(purchaseOrder, purchaseOrderLine, taxLine);

			response.setValue("productName", purchaseOrderLine.getProduct().getName());
			response.setValue("unit", purchaseOrderLineService.getPurchaseUnit(purchaseOrderLine));
			response.setValue("qty", purchaseOrderLineService.getQty(purchaseOrder,purchaseOrderLine));

			response.setValue("saleMinPrice", purchaseOrderLineService.getMinSalePrice(purchaseOrder, purchaseOrderLine));
			response.setValue("salePrice", purchaseOrderLineService.getSalePrice(purchaseOrder, purchaseOrderLine.getProduct(),price));
			
			Map<String,Object> discounts = purchaseOrderLineService.getDiscount(purchaseOrder, purchaseOrderLine, price);
			
			if(discounts != null)  {
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
		response.setValue("exTaxTotal", null);
		response.setValue("inTaxTotal", null);
		response.setValue("companyInTaxTotal", null);
		response.setValue("companyExTaxTotal", null);

	}

	
	public void getDiscount(ActionRequest request, ActionResponse response){

		Context context = request.getContext();
		
		PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);

		PurchaseOrder purchaseOrder = this.getPurchaseOrder(context);

		if(purchaseOrder == null || purchaseOrderLine.getProduct() == null) {  return;  }

		try  {
			BigDecimal price = purchaseOrderLine.getPrice();

			Map<String, Object> discounts = purchaseOrderLineService.getDiscount(purchaseOrder, purchaseOrderLine, price);
			
			if(discounts != null)  {

				response.setValue("discountAmount", discounts.get("discountAmount"));
				response.setValue("discountTypeSelect", discounts.get("discountTypeSelect"));
				
				if(discounts.get("price") != null)  {
					response.setValue("price", (BigDecimal) discounts.get("price"));
				}
			}
		}
		catch(Exception e) {
			response.setFlash(e.getMessage());
		}
	}

	
	public void convertUnitPrice(ActionRequest request, ActionResponse response) {

		Context context = request.getContext();
		
		PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);

		PurchaseOrder purchaseOrder = this.getPurchaseOrder(context);

		if(purchaseOrder == null || purchaseOrderLine.getProduct() == null || !purchaseOrderLineService.unitPriceShouldBeUpdate(purchaseOrder, purchaseOrderLine.getProduct())) {  return;  }

		try  {

			BigDecimal price = purchaseOrderLineService.getUnitPrice(purchaseOrder, purchaseOrderLine, purchaseOrderLine.getTaxLine());
			
			Map<String,Object> discounts = purchaseOrderLineService.getDiscount(purchaseOrder, purchaseOrderLine, price);
			
			if(discounts != null)  {  
			
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
	
	public PurchaseOrder getPurchaseOrder(Context context)  {
		
		Context parentContext = context.getParentContext();
		
		PurchaseOrder purchaseOrder = parentContext.asType(PurchaseOrder.class);
		
		if(!parentContext.getContextClass().toString().equals(PurchaseOrder.class.toString())){
			
			PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);
			
			purchaseOrder = purchaseOrderLine.getPurchaseOrder();
		}
		
		return purchaseOrder;
	}
	
	public void emptyLine(ActionRequest request, ActionResponse response){
		PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
		if(purchaseOrderLine.getIsTitleLine()){
			PurchaseOrderLine newPurchaseOrderLine = new PurchaseOrderLine();
			newPurchaseOrderLine.setIsTitleLine(true);
			newPurchaseOrderLine.setQty(BigDecimal.ZERO);
			newPurchaseOrderLine.setId(purchaseOrderLine.getId());
			newPurchaseOrderLine.setVersion(purchaseOrderLine.getVersion());
			response.setValues(purchaseOrderLine);
		}
	}
}
