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
package com.axelor.apps.purchase.web;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.axelor.apps.base.db.IPriceListLine;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.SupplierCatalog;
import com.axelor.apps.base.db.repo.GeneralRepository;
import com.axelor.apps.base.db.repo.SupplierCatalogRepository;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.exception.IExceptionMessage;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderLineServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class PurchaseOrderLineController {

	@Inject
	private PurchaseOrderLineService purchaseOrderLineService;

	@Inject
	private PriceListService priceListService;

	@Inject
	private ProductService productService;

	public void compute(ActionRequest request, ActionResponse response) throws AxelorException{

		PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
		BigDecimal exTaxTotal = BigDecimal.ZERO;
		BigDecimal companyExTaxTotal = BigDecimal.ZERO;
		BigDecimal inTaxTotal = BigDecimal.ZERO;
		BigDecimal companyInTaxTotal = BigDecimal.ZERO;
		BigDecimal priceDiscounted = BigDecimal.ZERO;
		
		
		if(purchaseOrderLine.getTaxLine()==null){
			throw new AxelorException(String.format(I18n.get(IExceptionMessage.PURCHASE_ORDER_LINE_TAX_LINE)), IException.CONFIGURATION_ERROR);
		}
		try{
			if(!request.getContext().getParentContext().asType(PurchaseOrder.class).getInAti()){
				if (purchaseOrderLine.getPrice() != null && purchaseOrderLine.getQty() != null){

					exTaxTotal = PurchaseOrderLineServiceImpl.computeAmount(purchaseOrderLine.getQty(), purchaseOrderLineService.computeDiscount(purchaseOrderLine));
					inTaxTotal = exTaxTotal.add(exTaxTotal.multiply(purchaseOrderLine.getTaxLine().getValue()));
					priceDiscounted = purchaseOrderLineService.computeDiscount(purchaseOrderLine);
				}

				if(exTaxTotal != null) {

					PurchaseOrder purchaseOrder = purchaseOrderLine.getPurchaseOrder();

					if(purchaseOrder == null) {
						purchaseOrder = request.getContext().getParentContext().asType(PurchaseOrder.class);
					}

					if(purchaseOrder != null) {
						
						companyExTaxTotal = purchaseOrderLineService.getCompanyExTaxTotal(exTaxTotal, purchaseOrder);
						companyInTaxTotal = companyExTaxTotal.add(companyExTaxTotal.multiply(purchaseOrderLine.getTaxLine().getValue()));
						response.setValue("saleMinPrice", purchaseOrderLineService.getMinSalePrice(purchaseOrder, purchaseOrderLine));
						response.setValue("salePrice", purchaseOrderLineService.getSalePrice(purchaseOrder, purchaseOrderLine.getPrice()));
					}

				}

				response.setValue("exTaxTotal", exTaxTotal);
				response.setValue("inTaxTotal", inTaxTotal);
				response.setValue("companyExTaxTotal", companyExTaxTotal);
				response.setValue("companyInTaxTotal", companyInTaxTotal);
				response.setValue("priceDiscounted", priceDiscounted);
	
			}
			else{
				if (purchaseOrderLine.getPrice() != null && purchaseOrderLine.getQty() != null){

					inTaxTotal = PurchaseOrderLineServiceImpl.computeAmount(purchaseOrderLine.getQty(), purchaseOrderLineService.computeDiscount(purchaseOrderLine));
					exTaxTotal = inTaxTotal.divide(purchaseOrderLine.getTaxLine().getValue().add(new BigDecimal(1)), 2, BigDecimal.ROUND_HALF_UP);
					priceDiscounted = purchaseOrderLineService.computeDiscount(purchaseOrderLine);
				}

				if(inTaxTotal != null) {

					PurchaseOrder purchaseOrder = purchaseOrderLine.getPurchaseOrder();

					if(purchaseOrder == null) {
						purchaseOrder = request.getContext().getParentContext().asType(PurchaseOrder.class);
					}

					if(purchaseOrder != null) {

						companyInTaxTotal = purchaseOrderLineService.getCompanyExTaxTotal(inTaxTotal, purchaseOrder);
						companyExTaxTotal = companyInTaxTotal.divide(purchaseOrderLine.getTaxLine().getValue().add(new BigDecimal(1)), 2, BigDecimal.ROUND_HALF_UP);
						response.setValue("saleMinPrice", purchaseOrderLineService.getMinSalePrice(purchaseOrder, purchaseOrderLine));
						response.setValue("salePrice", purchaseOrderLineService.getSalePrice(purchaseOrder, purchaseOrderLine.getPrice()));
					}

				}

				response.setValue("exTaxTotal", exTaxTotal);
				response.setValue("inTaxTotal", inTaxTotal);
				response.setValue("companyInTaxTotal", companyInTaxTotal);
				response.setValue("companyExTaxTotal", companyExTaxTotal);
				response.setValue("priceDiscounted", priceDiscounted);

			}
		}
		catch(Exception e)  {
			response.setFlash(e.getMessage());
		}
	}

	public void getProductInformation(ActionRequest request, ActionResponse response){

		PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);

		PurchaseOrder purchaseOrder = purchaseOrderLine.getPurchaseOrder();

		if(purchaseOrder == null)  {
			purchaseOrder = request.getContext().getParentContext().asType(PurchaseOrder.class);
		}

		if(purchaseOrder != null && purchaseOrderLine.getProduct() != null)  {

			try  {
				BigDecimal price = purchaseOrderLineService.getUnitPrice(purchaseOrder, purchaseOrderLine);
				int discountTypeSelect = 0;
				BigDecimal discountAmount = BigDecimal.ZERO;
				
				response.setValue("taxLine", purchaseOrderLineService.getTaxLine(purchaseOrder, purchaseOrderLine));
				response.setValue("productName", purchaseOrderLine.getProduct().getName());
				response.setValue("unit", purchaseOrderLine.getProduct().getUnit());
				response.setValue("qty", purchaseOrderLineService.getQty(purchaseOrder,purchaseOrderLine));

				response.setValue("saleMinPrice", purchaseOrderLineService.getMinSalePrice(purchaseOrder, purchaseOrderLine));
				response.setValue("salePrice", purchaseOrderLineService.getSalePrice(purchaseOrder, price));
				PriceList priceList = purchaseOrder.getPriceList();
				if(priceList != null)  {
					PriceListLine priceListLine = purchaseOrderLineService.getPriceListLine(purchaseOrderLine, priceList);
					if(priceListLine!=null){
						discountTypeSelect = priceListLine.getTypeSelect();
					}
					if((GeneralService.getGeneral().getComputeMethodDiscountSelect() == GeneralRepository.INCLUDE_DISCOUNT_REPLACE_ONLY && discountTypeSelect == IPriceListLine.TYPE_REPLACE) || GeneralService.getGeneral().getComputeMethodDiscountSelect() == GeneralRepository.INCLUDE_DISCOUNT)
					{
						Map<String, Object> discounts = priceListService.getDiscounts(priceList, priceListLine, price);
						discountAmount = (BigDecimal) discounts.get("discountAmount");
						price = priceListService.computeDiscount(price, (int) discounts.get("discountTypeSelect"), discountAmount);
						
					}
					else{
						Map<String, Object> discounts = priceListService.getDiscounts(priceList, priceListLine, price);
						discountAmount = (BigDecimal) discounts.get("discountAmount");
						response.setValue("discountAmount", discountAmount);
						response.setValue("discountTypeSelect", discounts.get("discountTypeSelect"));
						if(discounts.get("price") != null)  {
							price = (BigDecimal) discounts.get("price");
						}
					}
				}

				if(discountAmount.equals(BigDecimal.ZERO)){
					List<SupplierCatalog> supplierCatalogList = purchaseOrderLine.getProduct().getSupplierCatalogList();
					if(supplierCatalogList != null && !supplierCatalogList.isEmpty()){
						SupplierCatalog supplierCatalog = Beans.get(SupplierCatalogRepository.class).all().filter("self.product = ?1 AND self.minQty <= ?2 AND self.supplierPartner = ?3 ORDER BY self.minQty DESC",purchaseOrderLine.getProduct(),purchaseOrderLine.getQty(),purchaseOrder.getSupplierPartner()).fetchOne();
						if(supplierCatalog!=null){
							if(GeneralService.getGeneral().getComputeMethodDiscountSelect() == GeneralRepository.DISCOUNT_SEPARATE){
								Map<String, Object> discounts = productService.getDiscountsFromCatalog(supplierCatalog,price);
								response.setValue("discountAmount", discounts.get("discountAmount"));
								response.setValue("discountTypeSelect", discounts.get("discountTypeSelect"));
							}
							else{
								Map<String, Object> discounts = productService.getDiscountsFromCatalog(supplierCatalog,price);
								price = priceListService.computeDiscount(price, (int) discounts.get("discountTypeSelect"), (BigDecimal) discounts.get("discountAmount"));
							}
						}
					}
				}

				response.setValue("price", price);
			}
			catch(Exception e) {
				e.printStackTrace();
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
		response.setValue("unit", null);
		response.setValue("discountAmount", null);
		response.setValue("discountTypeSelect", null);
		response.setValue("price", null);
		response.setValue("saleMinPrice", null);
		response.setValue("salePrice", null);

	}


	public void getDiscount(ActionRequest request, ActionResponse response){

		PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);

		PurchaseOrder purchaseOrder = purchaseOrderLine.getPurchaseOrder();

		if(purchaseOrder == null)  {
			purchaseOrder = request.getContext().getParentContext().asType(PurchaseOrder.class);
		}

		if(purchaseOrder != null && purchaseOrderLine.getProduct() != null)  {

			try  {
				BigDecimal price = purchaseOrderLine.getProduct().getPurchasePrice();

				PriceList priceList = purchaseOrder.getPriceList();
				int discountTypeSelect = 0;
				BigDecimal discountAmount = BigDecimal.ZERO;
				
				if(priceList != null)  {
					PriceListLine priceListLine = purchaseOrderLineService.getPriceListLine(purchaseOrderLine, priceList);
					if(priceListLine!=null){
						discountTypeSelect = priceListLine.getTypeSelect();
					}
					
					if((GeneralService.getGeneral().getComputeMethodDiscountSelect() == GeneralRepository.INCLUDE_DISCOUNT_REPLACE_ONLY && discountTypeSelect == IPriceListLine.TYPE_REPLACE) || GeneralService.getGeneral().getComputeMethodDiscountSelect() == GeneralRepository.INCLUDE_DISCOUNT)
					{
						Map<String, Object> discounts = priceListService.getDiscounts(priceList, priceListLine, price);
						discountAmount = (BigDecimal) discounts.get("discountAmount");
						price = priceListService.computeDiscount(price, (int) discounts.get("discountTypeSelect"), discountAmount);
						
					}
					else{
						Map<String, Object> discounts = priceListService.getDiscounts(priceList, priceListLine, price);
						discountAmount = (BigDecimal) discounts.get("discountAmount");
						response.setValue("discountAmount", discountAmount);
						response.setValue("discountTypeSelect", discounts.get("discountTypeSelect"));
						if(discounts.get("price") != null)  {
							price = (BigDecimal) discounts.get("price");
						}
					}
					
				}

				if(discountAmount.equals(BigDecimal.ZERO)){
					List<SupplierCatalog> supplierCatalogList = purchaseOrderLine.getProduct().getSupplierCatalogList();
					if(supplierCatalogList != null && !supplierCatalogList.isEmpty()){
						SupplierCatalog supplierCatalog = Beans.get(SupplierCatalogRepository.class).all().filter("self.product = ?1 AND self.minQty <= ?2 AND self.supplierPartner = ?3 ORDER BY self.minQty DESC",purchaseOrderLine.getProduct(),purchaseOrderLine.getQty(),purchaseOrder.getSupplierPartner()).fetchOne();
						if(supplierCatalog!=null){
							if(GeneralService.getGeneral().getComputeMethodDiscountSelect() == GeneralRepository.DISCOUNT_SEPARATE){
								Map<String, Object> discounts = productService.getDiscountsFromCatalog(supplierCatalog,price);
								response.setValue("discountAmount", discounts.get("discountAmount"));
								response.setValue("discountTypeSelect", discounts.get("discountTypeSelect"));
							}
							else{
								Map<String, Object> discounts = productService.getDiscountsFromCatalog(supplierCatalog,price);
								price = priceListService.computeDiscount(price, (int) discounts.get("discountTypeSelect"), (BigDecimal) discounts.get("discountAmount"));
							}
						}
					}
				}

				response.setValue("price", price);
			}
			catch(Exception e) {
				response.setFlash(e.getMessage());
			}
		}
	}

	public void convertUnitPrice(ActionRequest request, ActionResponse response) {

		PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);

		PurchaseOrder purchaseOrder = purchaseOrderLine.getPurchaseOrder();
		if(purchaseOrder == null)  {
			purchaseOrder = request.getContext().getParentContext().asType(PurchaseOrder.class);
		}

		if(purchaseOrder != null) {

			try  {

				BigDecimal price = purchaseOrderLineService.convertUnitPrice(purchaseOrderLine, purchaseOrder);
				BigDecimal discountAmount = purchaseOrderLineService.convertDiscountAmount(purchaseOrderLine, purchaseOrder);

				response.setValue("price", price);
				response.setValue("discountAmount",discountAmount);

			}
			catch(Exception e)  {
				response.setFlash(e.getMessage());
			}
		}
	}
}
