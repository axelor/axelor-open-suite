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
package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceLineServiceImpl;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.AppBaseRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.purchase.db.SupplierCatalog;
import com.axelor.apps.purchase.db.repo.SupplierCatalogRepository;
import com.axelor.apps.purchase.service.PurchaseProductService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;


public class InvoiceLineSupplychainService extends InvoiceLineServiceImpl {
	
	protected PurchaseProductService purchaseProductService;

	@Inject
	public InvoiceLineSupplychainService(AccountManagementService accountManagementService, CurrencyService currencyService, PriceListService priceListService,
										 AppAccountService appAccountService, AnalyticMoveLineService analyticMoveLineService, AccountManagementAccountService accountManagementAccountService)  {
		
		super(accountManagementService, currencyService, priceListService, appAccountService, analyticMoveLineService, accountManagementAccountService);
		
	}
	
	@Override
	public Unit getUnit(Product product, boolean isPurchase){
		if(isPurchase){
			if(product.getPurchasesUnit() != null){
				return product.getPurchasesUnit();
			}
			else{
				return product.getUnit();
			}
		}
		else{
			if(product.getSalesUnit() != null){
				return product.getPurchasesUnit();
			}
			else{
				return product.getUnit();
			}
		}
	}
	
	@Override
	public Map<String,Object> getDiscount(Invoice invoice, InvoiceLine invoiceLine, BigDecimal price)  {
		
		PriceList priceList = invoice.getPriceList();
		BigDecimal discountAmount = BigDecimal.ZERO;
		int computeMethodDiscountSelect = appAccountService.getAppBase().getComputeMethodDiscountSelect();
		
		Map<String, Object> discounts = super.getDiscount(invoice, invoiceLine, price);
		
		if(priceList != null){
			discountAmount = (BigDecimal) discounts.get("discountAmount");
		}
		
		if (invoice.getOperationTypeSelect() < InvoiceRepository.OPERATION_TYPE_CLIENT_SALE && discountAmount.compareTo(BigDecimal.ZERO) == 0){
			List<SupplierCatalog> supplierCatalogList = invoiceLine.getProduct().getSupplierCatalogList();
			if(supplierCatalogList != null && !supplierCatalogList.isEmpty()){
				SupplierCatalog supplierCatalog = Beans.get(SupplierCatalogRepository.class).all().filter("self.product = ?1 AND self.minQty <= ?2 AND self.supplierPartner = ?3 ORDER BY self.minQty DESC",invoiceLine.getProduct(),invoiceLine.getQty(),invoice.getPartner()).fetchOne();
				if(supplierCatalog != null){
					
					discounts = purchaseProductService.getDiscountsFromCatalog(supplierCatalog,price);

					if(computeMethodDiscountSelect != AppBaseRepository.DISCOUNT_SEPARATE){
						discounts.put("price", priceListService.computeDiscount(price, (int) discounts.get("discountTypeSelect"), (BigDecimal) discounts.get("discountAmount")));
					}
				}
			}
		}
		return discounts;
	}
}
