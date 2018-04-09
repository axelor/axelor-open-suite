/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.exception.IExceptionMessage;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;


@Singleton
public class PurchaseOrderLineController {

	@Inject
	private PurchaseOrderLineService purchaseOrderLineService;

	public void compute(ActionRequest request, ActionResponse response) throws AxelorException{

		try{
			Context context = request.getContext();
			PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);
			PurchaseOrder purchaseOrder = this.getPurchaseOrder(context);

			Map<String, BigDecimal> map = purchaseOrderLineService.compute(purchaseOrderLine, purchaseOrder);
			response.setValues(map);
			response.setAttr("priceDiscounted", "hidden", map.getOrDefault("priceDiscounted", BigDecimal.ZERO).compareTo(purchaseOrderLine.getPrice()) == 0);
		}
		catch(Exception e)  {
			TraceBackService.trace(response, e);
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

		try {
			TaxLine taxLine = purchaseOrderLineService.getTaxLine(purchaseOrder, purchaseOrderLine);
			response.setValue("taxLine", taxLine);
			
			BigDecimal price = purchaseOrderLineService.getUnitPrice(purchaseOrder, purchaseOrderLine, taxLine);
			String productName = purchaseOrderLineService.getProductSupplierInfos(purchaseOrder, purchaseOrderLine)[0];
			String productCode = purchaseOrderLineService.getProductSupplierInfos(purchaseOrder, purchaseOrderLine)[1];

			if (price == null || productName == null || productCode == null) {
				price = BigDecimal.ZERO;
				productName = "";
				productCode = "";
				response.setFlash(I18n.get(IExceptionMessage.PURCHASE_ORDER_LINE_NO_SUPPLIER_CATALOG));
			}

			response.setValue("unit", purchaseOrderLineService.getPurchaseUnit(purchaseOrderLine));
			response.setValue("qty", purchaseOrderLineService.getQty(purchaseOrder,purchaseOrderLine));

			Tax tax = Beans.get(AccountManagementService.class).getProductTax(Beans.get(AccountManagementService.class).getAccountManagement(product, purchaseOrder.getCompany()),true);
			TaxEquiv taxEquiv = Beans.get(FiscalPositionService.class).getTaxEquiv(purchaseOrder.getSupplierPartner().getFiscalPosition(), tax);
			response.setValue("taxEquiv", taxEquiv);

			response.setValue("saleMinPrice", purchaseOrderLineService.getMinSalePrice(purchaseOrder, purchaseOrderLine));
			response.setValue("salePrice", purchaseOrderLineService.getSalePrice(purchaseOrder, purchaseOrderLine.getProduct(),price));

			Map<String,Object> discounts = purchaseOrderLineService.getDiscount(purchaseOrder, purchaseOrderLine, price);
			
			if(discounts != null) {
				response.setValue("discountAmount", discounts.get("discountAmount"));
				response.setValue("discountTypeSelect", discounts.get("discountTypeSelect"));
				if(discounts.get("price") != null) {
					price = (BigDecimal) discounts.get("price");
				}
			}
			response.setValue("price", price);
			response.setValue("productName", productName);
			response.setValue("productCode", productCode);
		}
		catch(Exception e) {
			this.resetProductInformation(response);
			response.setFlash(e.getMessage());
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
		response.setValue("productCode", null);
		response.setAttr("minQtyNotRespectedLabel", "hidden", true);
		response.setAttr("multipleQtyNotRespectedLabel", "hidden", true);

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

		Context parentContext = context.getParent();
		PurchaseOrder purchaseOrder = null;
		
		if(parentContext != null && parentContext.getContextClass() == PurchaseOrder.class) {

			purchaseOrder = parentContext.asType(PurchaseOrder.class);
			if(!parentContext.getContextClass().toString().equals(PurchaseOrder.class.toString())){

				PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);

				purchaseOrder = purchaseOrderLine.getPurchaseOrder();
			}
			
		} else {
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
			response.setValues(Mapper.toMap(purchaseOrderLine));
		}
	}

	public void checkQty(ActionRequest request, ActionResponse response) {

		Context context = request.getContext();
		PurchaseOrderLine purchaseOrderLine = context.asType(PurchaseOrderLine.class);
		PurchaseOrder purchaseOrder = getPurchaseOrder(context);
		
		purchaseOrderLineService.checkMinQty(purchaseOrder, purchaseOrderLine, request, response);
		
		purchaseOrderLineService.checkMultipleQty(purchaseOrderLine, response);
		
	}
	
	
	
}
