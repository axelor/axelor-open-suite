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
package com.axelor.apps.sale.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.sale.db.PackLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SaleOrderLineController {

	@Inject
	private SaleOrderLineService saleOrderLineService;

	public void compute(ActionRequest request, ActionResponse response) {

		Context context = request.getContext();
		
		SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);

		SaleOrder saleOrder = saleOrderLineService.getSaleOrder(context);
		
		try{
			Map<String, BigDecimal> map = saleOrderLineService.computeValues(saleOrder, saleOrderLine);

			response.setValues(map);
			response.setAttr("priceDiscounted", "hidden", map.getOrDefault("priceDiscounted", BigDecimal.ZERO).compareTo(saleOrderLine.getPrice()) == 0);
		}
		catch(Exception e) {
			TraceBackService.trace(response, e);
		}
	}
	
	public void computeSubMargin(ActionRequest request, ActionResponse response) throws AxelorException {

		Context context = request.getContext();

		SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
		SaleOrder saleOrder = saleOrderLineService.getSaleOrder(context);

		saleOrderLine.setSaleOrder(saleOrder);
		saleOrderLineService.computeSubMargin(saleOrderLine);

		response.setValue("subTotalCostPrice", saleOrderLine.getSubTotalCostPrice());
		response.setValue("subTotalGrossMargin", saleOrderLine.getSubTotalGrossMargin());
		response.setValue("subMarginRate", saleOrderLine.getSubMarginRate());
	}



	/**
	 * Called by the sale order line form.
	 * Update all fields when the product is changed.
	 * @param request
	 * @param response
	 */
	public void getProductInformation(ActionRequest request, ActionResponse response)  {

		Context context = request.getContext();
		
		SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);

		SaleOrder saleOrder = saleOrderLineService.getSaleOrder(context);
		
		Product product = saleOrderLine.getProduct();

		if(saleOrder == null || product == null) { 
			this.resetProductInformation(response);
			return;
		}

		try  {
			saleOrderLineService.computeProductInformation(saleOrderLine, saleOrder);
			response.setValue("taxLine", saleOrderLine.getTaxLine());
            response.setValue("taxEquiv", saleOrderLine.getTaxEquiv());
			response.setValue("productName", saleOrderLine.getProductName());
			response.setValue("saleSupplySelect", product.getSaleSupplySelect());
			response.setValue("unit", saleOrderLineService.getSaleUnit(saleOrderLine));
			response.setValue("companyCostPrice", saleOrderLineService.getCompanyCostPrice(saleOrder, saleOrderLine));

			if (saleOrderLine.getDiscountAmount() != null) {
				response.setValue("discountAmount", saleOrderLine.getDiscountAmount());
			}
			if (saleOrderLine.getDiscountTypeSelect() != null) {
				response.setValue("discountTypeSelect", saleOrderLine.getDiscountTypeSelect());
			}
			response.setValue("price", saleOrderLine.getPrice());

		}
		catch(Exception e)  {
			response.setFlash(e.getMessage());
			this.resetProductInformation(response);
		}
	}
	

	public void resetProductInformation(ActionResponse response)  {

		response.setValue("taxLine", null);
		response.setValue("taxEquiv", null);
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

		SaleOrder saleOrder = saleOrderLineService.getSaleOrder(context);

		if(saleOrder == null || saleOrderLine.getProduct() == null) {  return;  }

		try  {
			BigDecimal price = saleOrderLine.getPrice();

			Map<String,Object> discounts = saleOrderLineService.getDiscount(saleOrder, saleOrderLine, price);
			
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

		SaleOrder saleOrder = saleOrderLineService.getSaleOrder(context);

		if(saleOrder == null || saleOrderLine.getProduct() == null || !saleOrderLineService.unitPriceShouldBeUpdate(saleOrder, saleOrderLine.getProduct())) {  return;  }

		try  {

			BigDecimal price = saleOrderLineService.getUnitPrice(saleOrder, saleOrderLine, saleOrderLine.getTaxLine());

			Map<String,Object> discounts = saleOrderLineService.getDiscount(saleOrder, saleOrderLine, price);
			
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
	
	public void emptyLine(ActionRequest request, ActionResponse response){
		SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
		if(saleOrderLine.getTypeSelect() != SaleOrderLineRepository.TYPE_NORMAL){
			SaleOrderLine newSaleOrderLine = new SaleOrderLine();
			newSaleOrderLine.setQty(BigDecimal.ZERO);
			newSaleOrderLine.setId(saleOrderLine.getId());
			newSaleOrderLine.setVersion(saleOrderLine.getVersion());
			newSaleOrderLine.setTypeSelect(saleOrderLine.getTypeSelect());
			response.setValues(Mapper.toMap(newSaleOrderLine));
		}
	}
	
	
	public void createPackLines(ActionRequest request, ActionResponse response) throws AxelorException {
		
		SaleOrderLine soLine = request.getContext().asType(SaleOrderLine.class);
		
		Product product = soLine.getProduct();
		
		if (product != null) {
			
			product = Beans.get(ProductRepository.class).find(product.getId());
			
			if (product.getIsPack()) {
				SaleOrder saleOrder = saleOrderLineService.getSaleOrder(request.getContext());
				List<SaleOrderLine> subLines = new ArrayList<SaleOrderLine>();

				for (PackLine packLine : product.getPackLines()) {
					SaleOrderLine subLine = new SaleOrderLine();
					Product subProduct = packLine.getProduct();
					subLine.setProduct(subProduct);
					subLine.setProductName(subProduct.getName());
					subLine.setPrice(subProduct.getSalePrice());
					subLine.setUnit(saleOrderLineService.getSaleUnit(subLine));
					subLine.setQty(new BigDecimal(packLine.getQuantity()));
					subLine.setCompanyCostPrice(saleOrderLineService.getCompanyCostPrice(saleOrder, subLine));
					TaxLine taxLine = saleOrderLineService.getTaxLine(saleOrder, subLine);
					subLine.setTaxLine(taxLine);
					saleOrderLineService.computeValues(saleOrder, subLine);

					BigDecimal price = saleOrderLineService.getUnitPrice(saleOrder, subLine, taxLine);

					Map<String,Object> discounts = saleOrderLineService.getDiscount(saleOrder, subLine, price);

					if(discounts != null)  {
						subLine.setDiscountAmount((BigDecimal) discounts.get("discountAmount"));
						subLine.setDiscountTypeSelect((Integer) discounts.get("discountTypeSelect"));
						if(discounts.get("price") != null)  {
							price = (BigDecimal) discounts.get("price");
						}
					}
					subLine.setPrice(price);

					subLines.add(subLine);
				}

				if (!subLines.isEmpty()) {
					response.setValue("subLineList", subLines);
				}
				response.setValue("typeSelect", SaleOrderLineRepository.TYPE_PACK);
				response.setValue("qty", 0);
			}

		}
		
	}
	
	public void checkQty(ActionRequest request, ActionResponse response) {

		Context context = request.getContext();
		SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
		saleOrderLineService.checkMultipleQty(saleOrderLine, response);
		
	}

}
