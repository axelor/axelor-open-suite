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
package com.axelor.apps.supplychain.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.SupplierCatalog;
import com.axelor.apps.base.db.repo.SupplierCatalogRepository;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.service.StockMoveLineServiceImpl;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class StockMoveLineSupplychainServiceImpl extends StockMoveLineServiceImpl{
	
	@Inject
	protected AccountManagementService accountManagementService;
	
	@Inject
	protected PriceListService priceListService;
	
	@Inject
	private ProductService productService;
	
	@Override
	public StockMoveLine compute(StockMoveLine stockMoveLine, StockMove stockMove) throws AxelorException{
		BigDecimal unitPriceUntaxed = BigDecimal.ZERO;
		BigDecimal unitPriceTaxed = BigDecimal.ZERO;
		TaxLine taxLine = null;
		BigDecimal discountAmount = BigDecimal.ZERO;
		if(stockMove == null || (stockMove.getSaleOrder() == null && stockMove.getPurchaseOrder() == null)){
			return super.compute(stockMoveLine, stockMove);
		}
		else{
			if(stockMove.getSaleOrder() != null){
				taxLine = accountManagementService.getTaxLine(
						generalService.getTodayDate(), stockMoveLine.getProduct(), stockMove.getCompany(),
						stockMove.getSaleOrder().getClientPartner().getFiscalPosition(), false);
				unitPriceUntaxed = stockMoveLine.getProduct().getSalePrice();
				PriceList priceList = stockMove.getSaleOrder().getPriceList();
				if(priceList != null)  {
					PriceListLine priceListLine = priceListService.getPriceListLine(stockMoveLine.getProduct(), stockMoveLine.getQty(), priceList);
					Map<String, Object> discounts = priceListService.getDiscounts(priceList, priceListLine, unitPriceUntaxed);
					if(discounts != null){
						discountAmount = (BigDecimal) discounts.get("discountAmount");
						unitPriceUntaxed = priceListService.computeDiscount(unitPriceUntaxed, (int) discounts.get("discountTypeSelect"), discountAmount);
					}
				}
			}
			else{
				taxLine = accountManagementService.getTaxLine(
						generalService.getTodayDate(), stockMoveLine.getProduct(), stockMove.getCompany(),
						stockMove.getPurchaseOrder().getSupplierPartner().getFiscalPosition(), true);
				unitPriceUntaxed = stockMoveLine.getProduct().getPurchasePrice();
				PriceList priceList = stockMove.getPurchaseOrder().getPriceList();
				if(priceList != null)  {
					PriceListLine priceListLine = priceListService.getPriceListLine(stockMoveLine.getProduct(), stockMoveLine.getQty(), priceList);
					Map<String, Object> discounts = priceListService.getDiscounts(priceList, priceListLine, unitPriceUntaxed);
					if(discounts != null){
						discountAmount = (BigDecimal) discounts.get("discountAmount");
						unitPriceUntaxed = priceListService.computeDiscount(unitPriceUntaxed, (int) discounts.get("discountTypeSelect"), discountAmount);
					}
				}
				if (discountAmount.equals(BigDecimal.ZERO)){
					List<SupplierCatalog> supplierCatalogList = stockMoveLine.getProduct().getSupplierCatalogList();
					if(supplierCatalogList != null && !supplierCatalogList.isEmpty()){
						SupplierCatalog supplierCatalog = Beans.get(SupplierCatalogRepository.class).all().filter("self.product = ?1 AND self.minQty <= ?2 AND self.supplierPartner = ?3 ORDER BY self.minQty DESC",stockMoveLine.getProduct(),unitPriceUntaxed,stockMove.getPurchaseOrder().getSupplierPartner()).fetchOne();
						if(supplierCatalog!=null){
							Map<String, Object> discounts = productService.getDiscountsFromCatalog(supplierCatalog,unitPriceUntaxed);
							if(discounts != null){
								unitPriceUntaxed = priceListService.computeDiscount(unitPriceUntaxed, (int) discounts.get("discountTypeSelect"), (BigDecimal) discounts.get("discountAmount"));
							}
						}
					}
				}
			}
			unitPriceTaxed = unitPriceUntaxed.multiply(taxLine.getValue().add(BigDecimal.ONE));
		}
		stockMoveLine.setUnitPriceUntaxed(unitPriceUntaxed);
		stockMoveLine.setUnitPriceTaxed(unitPriceTaxed);
		return stockMoveLine;
	}
}
