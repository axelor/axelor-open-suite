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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.PriceList;
import com.axelor.apps.base.db.PriceListLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.PriceListService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.purchase.db.SupplierCatalog;
import com.axelor.apps.purchase.db.repo.SupplierCatalogRepository;
import com.axelor.apps.purchase.service.PurchaseProductService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveLineServiceImpl;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.TrackingNumberService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;

@RequestScoped
public class StockMoveLineSupplychainServiceImpl extends StockMoveLineServiceImpl{
	
	protected AccountManagementService accountManagementService;
	
	protected PriceListService priceListService;
	
	private PurchaseProductService productService;

	private UnitConversionService unitConversionService;

	@Inject
	public StockMoveLineSupplychainServiceImpl(TrackingNumberService trackingNumberService,
											   AppBaseService appBaseService, StockMoveService stockMoveService,
											   AccountManagementService accountManagementService,
											   PriceListService priceListService,
											   PurchaseProductService productService,
											   UnitConversionService unitConversionService) {
		super(trackingNumberService, appBaseService, stockMoveService);
		this.accountManagementService = accountManagementService;
		this.priceListService = priceListService;
		this.productService = productService;
		this.unitConversionService = unitConversionService;
	}

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
			if(stockMoveLine.getProduct() != null) {
				if(stockMove.getSaleOrder() != null){
					taxLine = accountManagementService.getTaxLine(
							appBaseService.getTodayDate(), stockMoveLine.getProduct(), stockMove.getCompany(),
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
							appBaseService.getTodayDate(), stockMoveLine.getProduct(), stockMove.getCompany(),
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
					if (discountAmount.compareTo(BigDecimal.ZERO) == 0){
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
		}
		stockMoveLine.setUnitPriceUntaxed(unitPriceUntaxed);
		stockMoveLine.setUnitPriceTaxed(unitPriceTaxed);
		return stockMoveLine;
	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void updateReservedQty(StockMoveLine stockMoveLine, BigDecimal reservedQty) throws AxelorException {
	    StockMove stockMove = stockMoveLine.getStockMove();
	    int statusSelect = stockMove.getStatusSelect();
	    if (statusSelect == StockMoveRepository.STATUS_PLANNED
				|| statusSelect == StockMoveRepository.STATUS_REALIZED) {
			StockMoveService stockMoveService = Beans.get(StockMoveService.class);
			stockMoveService.cancel(stockMoveLine.getStockMove());
			stockMoveLine.setReservedQty(reservedQty);
			stockMove.setStatusSelect(StockMoveRepository.STATUS_DRAFT);
			stockMoveService.plan(stockMove);
			if (statusSelect == StockMoveRepository.STATUS_REALIZED) {
			    stockMoveService.realize(stockMove);
			}
		} else {
	    	stockMoveLine.setReservedQty(stockMoveLine.getReservedQty());
		}
	}

	@Override
	public void updateLocations(StockMoveLine stockMoveLine, StockLocation fromStockLocation, StockLocation toStockLocation, Product product, BigDecimal qty, int fromStatus, int toStatus, LocalDate
			lastFutureStockMoveDate, TrackingNumber trackingNumber, BigDecimal reservedQty) throws AxelorException  {
		BigDecimal realReservedQty =  stockMoveLine.getReservedQty();
		Unit productUnit = product.getUnit();
		Unit stockMoveLineUnit = stockMoveLine.getUnit();
		if (productUnit != null && !productUnit.equals(stockMoveLineUnit)) {
			qty = unitConversionService.convertWithProduct(stockMoveLineUnit, productUnit, qty, stockMoveLine.getProduct());
			realReservedQty = unitConversionService.convertWithProduct(stockMoveLineUnit, productUnit, realReservedQty, stockMoveLine.getProduct());
		}
	    super.updateLocations(stockMoveLine, fromStockLocation, toStockLocation, product, qty, fromStatus, toStatus, lastFutureStockMoveDate, trackingNumber, realReservedQty);
    }
}
