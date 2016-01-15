/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2015 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.IPriceListLine;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.GeneralRepository;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.SaleOrderLineService;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;

public class SaleOrderLineController {

	@Inject
	private SaleOrderLineService saleOrderLineService;

	@Inject
	private PriceListService priceListService;

	@Inject
	protected GeneralService generalService;


	public void compute(ActionRequest request, ActionResponse response) throws AxelorException {

		Context context = request.getContext();
		
		SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);

		SaleOrder saleOrder = this.getSaleOrder(context);
		
		try{
			if(saleOrder == null || saleOrderLine.getProduct() == null || saleOrderLine.getPrice() == null || saleOrderLine.getQty() == null)  {  
				this.resetProductInformation(response);
				return;  
			}

			BigDecimal exTaxTotal = BigDecimal.ZERO;
			BigDecimal companyExTaxTotal = BigDecimal.ZERO;
			BigDecimal inTaxTotal = BigDecimal.ZERO;
			BigDecimal companyInTaxTotal = BigDecimal.ZERO;
			BigDecimal priceDiscounted = saleOrderLineService.computeDiscount(saleOrderLine);
			response.setValue("priceDiscounted", priceDiscounted);
			response.setAttr("priceDiscounted", "hidden", priceDiscounted.compareTo(saleOrderLine.getPrice()) == 0);
			
			BigDecimal taxRate = BigDecimal.ZERO;
			if(saleOrderLine.getTaxLine() != null)  {  taxRate = saleOrderLine.getTaxLine().getValue();  }
			
			if(!saleOrder.getInAti()){
				exTaxTotal = saleOrderLineService.computeAmount(saleOrderLine.getQty(), priceDiscounted);
				inTaxTotal = exTaxTotal.add(exTaxTotal.multiply(taxRate));
				companyExTaxTotal = saleOrderLineService.getAmountInCompanyCurrency(exTaxTotal, saleOrder);
				companyInTaxTotal = companyExTaxTotal.add(companyExTaxTotal.multiply(taxRate));
			}
			else  {
				inTaxTotal = saleOrderLineService.computeAmount(saleOrderLine.getQty(), priceDiscounted);
				exTaxTotal = inTaxTotal.divide(taxRate.add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
				companyInTaxTotal = saleOrderLineService.getAmountInCompanyCurrency(inTaxTotal, saleOrder);
				companyExTaxTotal = companyInTaxTotal.divide(taxRate.add(BigDecimal.ONE), 2, BigDecimal.ROUND_HALF_UP);
			}
			
			response.setValue("exTaxTotal", exTaxTotal);
			response.setValue("inTaxTotal", inTaxTotal);
			response.setValue("companyInTaxTotal", companyInTaxTotal);
			response.setValue("companyExTaxTotal", companyExTaxTotal);
			
		}
		catch(Exception e) {
			response.setFlash(e.getMessage());
		}
	}
	

	public void getProductInformation(ActionRequest request, ActionResponse response)  {

		Context context = request.getContext();
		
		SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);

		SaleOrder saleOrder = this.getSaleOrder(context);
		
		Product product = saleOrderLine.getProduct();

		if(saleOrder == null || product == null) { 
			this.resetProductInformation(response);
			return;
		}

		try  {
			TaxLine taxLine = saleOrderLineService.getTaxLine(saleOrder, saleOrderLine);
			response.setValue("taxLine", taxLine);
			
			BigDecimal price = saleOrderLineService.getUnitPrice(saleOrder, saleOrderLine, taxLine);

			response.setValue("productName", product.getName());
			response.setValue("saleSupplySelect", product.getSaleSupplySelect());
			response.setValue("unit", saleOrderLineService.getSaleUnit(saleOrderLine));
			response.setValue("companyCostPrice", saleOrderLineService.getCompanyCostPrice(saleOrder, saleOrderLine));

			Map<String,Object> discounts = this.getDiscount(saleOrder, saleOrderLine, price);
			
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
			this.resetProductInformation(response);
		}
	}
	
	public Map<String,Object> getDiscount(SaleOrder saleOrder, SaleOrderLine saleOrderLine, BigDecimal price)  {
		
		PriceList priceList = saleOrder.getPriceList();
		if(priceList != null)  {
			int discountTypeSelect = 0;
			
			PriceListLine priceListLine = saleOrderLineService.getPriceListLine(saleOrderLine, priceList);
			if(priceListLine != null){
				discountTypeSelect = priceListLine.getTypeSelect();
			}
			
			Map<String, Object> discounts = priceListService.getDiscounts(priceList, priceListLine, price);
			
			int computeMethodDiscountSelect = generalService.getGeneral().getComputeMethodDiscountSelect();
			if((computeMethodDiscountSelect == GeneralRepository.INCLUDE_DISCOUNT_REPLACE_ONLY && discountTypeSelect == IPriceListLine.TYPE_REPLACE) 
					|| computeMethodDiscountSelect == GeneralRepository.INCLUDE_DISCOUNT)  {
				
				price = priceListService.computeDiscount(price, (int) discounts.get("discountTypeSelect"), (BigDecimal) discounts.get("discountAmount"));
				discounts.put("price", price);
			}
			return discounts;
		}
		
		return null;
		
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
		response.setValue("exTaxTotal", null);
		response.setValue("inTaxTotal", null);
		response.setValue("companyInTaxTotal", null);
		response.setValue("companyExTaxTotal", null);


	}


	public void getDiscount(ActionRequest request, ActionResponse response) {

		Context context = request.getContext();
		
		SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);

		SaleOrder saleOrder = this.getSaleOrder(context);

		if(saleOrder == null || saleOrderLine.getProduct() == null) {  return;  }

		try  {
			BigDecimal price = saleOrderLine.getPrice();

			Map<String,Object> discounts = this.getDiscount(saleOrder, saleOrderLine, price);
			
			if(discounts == null)  {  return;  }
			
			response.setValue("discountAmount", discounts.get("discountAmount"));
			response.setValue("discountTypeSelect", discounts.get("discountTypeSelect"));
			if(discounts.get("price") != null)  {
				response.setValue("price", (BigDecimal) discounts.get("price"));
			}
			
		}
		catch(Exception e)  {
			response.setFlash(e.getMessage());
		}
	}

	public void convertUnitPrice(ActionRequest request, ActionResponse response) {

		Context context = request.getContext();
		
		SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);

		SaleOrder saleOrder = this.getSaleOrder(context);

		if(saleOrder == null || saleOrderLine.getProduct() == null || !this.unitPriceShouldBeUpdate(saleOrder, saleOrderLine.getProduct())) {  return;  }

		try  {

			BigDecimal price = saleOrderLineService.getUnitPrice(saleOrder, saleOrderLine, saleOrderLine.getTaxLine());

			Map<String,Object> discounts = this.getDiscount(saleOrder, saleOrderLine, price);
			
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
	
	public boolean unitPriceShouldBeUpdate(SaleOrder saleOrder, Product product)  {
		
		if(product != null && product.getInAti() != saleOrder.getInAti())  {
			return true;
		}
		return false;
		
	}
	
	public void emptyLine(ActionRequest request, ActionResponse response){
		SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
		if(saleOrderLine.getIsTitleLine()){
			SaleOrderLine newSaleOrderLine = new SaleOrderLine();
			newSaleOrderLine.setIsTitleLine(true);
			newSaleOrderLine.setQty(BigDecimal.ZERO);
			response.setValues(newSaleOrderLine);
		}
	}
	
	
	public SaleOrder getSaleOrder(Context context)  {
		
		Context parentContext = context.getParentContext();
		
		SaleOrder saleOrder = parentContext.asType(SaleOrder.class);
		
		if(!parentContext.getContextClass().toString().equals(SaleOrder.class.toString())){
			
			SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
			
			saleOrder = saleOrderLine.getSaleOrder();
		}
		
		return saleOrder;
	}

}
