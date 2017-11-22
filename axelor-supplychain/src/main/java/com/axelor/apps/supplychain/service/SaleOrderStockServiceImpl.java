/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.LocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderStockServiceImpl implements SaleOrderStockService  {

	protected StockMoveService stockMoveService;
	protected StockMoveLineService stockMoveLineService;
	protected StockConfigService stockConfigService;
	protected LocationRepository locationRepo;
	protected StockMoveRepository stockMoveRepo;
	protected UnitConversionService unitConversionService;
	
	@Inject
	public SaleOrderStockServiceImpl(StockMoveService stockMoveService, StockMoveLineService stockMoveLineService, StockConfigService stockConfigService,
			LocationRepository locationRepo, StockMoveRepository stockMoveRepo, UnitConversionService unitConversionService)  {
		
		this.stockMoveService = stockMoveService;
		this.stockMoveLineService = stockMoveLineService;
		this.stockConfigService = stockConfigService;
		this.locationRepo = locationRepo;
		this.stockMoveRepo = stockMoveRepo;
		this.unitConversionService = unitConversionService;
		
	}

	@Override
	public StockMove createStocksMovesFromSaleOrder(SaleOrder saleOrder) throws AxelorException {

		if (this.existActiveStockMoveForSaleOrder(saleOrder)) {
			throw new AxelorException(saleOrder, IException.CONFIGURATION_ERROR, I18n.get(IExceptionMessage.SO_ACTIVE_DELIVERY_STOCK_MOVE_ALREADY_EXIST), saleOrder.getSaleOrderSeq()); 
		}
		
		Company company = saleOrder.getCompany();

		if(saleOrder.getSaleOrderLineList() != null && company != null) {

			StockMove stockMove = this.createStockMove(saleOrder, company);

			for(SaleOrderLine saleOrderLine: saleOrder.getSaleOrderLineList()) {
				if(saleOrderLine.getProduct() != null || saleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_PACK){
					this.createStockMoveLine(stockMove, saleOrderLine, company);
				}
			}

			if(stockMove.getStockMoveLineList() != null && !stockMove.getStockMoveLineList().isEmpty()){
				stockMoveService.plan(stockMove);
				return stockMove;
			}
		}
		
		return null;
		
	}

	@Override
	public StockMove createStockMove(SaleOrder saleOrder, Company company) throws AxelorException  {
	    StockConfig stockConfig = stockConfigService.getStockConfig(company);
	    Location toLocation = stockConfig.getDefaultLocation();

		if (toLocation == null) {
			toLocation = stockConfigService.getCustomerVirtualLocation(stockConfig);
		}

		StockMove stockMove = stockMoveService.createStockMove(null, saleOrder.getDeliveryAddress(), company,
				saleOrder.getClientPartner(), saleOrder.getLocation(), toLocation, null, saleOrder.getShipmentDate(),
				saleOrder.getDescription(), saleOrder.getShipmentMode(), saleOrder.getFreightCarrierMode());

		stockMove.setToAddressStr(saleOrder.getDeliveryAddressStr());
		stockMove.setSaleOrder(saleOrder);
		stockMove.setStockMoveLineList(new ArrayList<StockMoveLine>());
		return stockMove;
	}


	public StockMoveLine createStockMoveLine(StockMove stockMove, SaleOrderLine saleOrderLine, Company company) throws AxelorException  {

		Product product = saleOrderLine.getProduct();

		if(product != null && this.isStockMoveProduct(saleOrderLine)
				&& !ProductRepository.PRODUCT_TYPE_SUBSCRIPTABLE.equals(product.getProductTypeSelect())) {
			
			Unit unit = saleOrderLine.getProduct().getUnit();
			BigDecimal qty = saleOrderLine.getQty();
			BigDecimal priceDiscounted = saleOrderLine.getPriceDiscounted();
			if(!unit.equals(saleOrderLine.getUnit())){
				qty = unitConversionService.convertWithProduct(saleOrderLine.getUnit(), unit, qty, saleOrderLine.getProduct());
				priceDiscounted = unitConversionService.convertWithProduct(saleOrderLine.getUnit(), unit, priceDiscounted, saleOrderLine.getProduct());
			}
			
			BigDecimal taxRate = BigDecimal.ZERO;
			TaxLine taxLine = saleOrderLine.getTaxLine();
			if(taxLine != null)  {
				taxRate = taxLine.getValue();
			}
			
			StockMoveLine stockMoveLine = stockMoveLineService.createStockMoveLine(
					product,
					saleOrderLine.getProductName(),
					saleOrderLine.getDescription(),
					qty,
					priceDiscounted,
					unit,
					stockMove,
					StockMoveLineService.TYPE_SALES, saleOrderLine.getSaleOrder().getInAti(), taxRate);

			stockMoveLine.setSaleOrderLine(saleOrderLine);
			stockMoveLine.setReservedQty(saleOrderLine.getReservedQty());
			if(stockMoveLine != null) {
				stockMove.addStockMoveLineListItem(stockMoveLine);
			}
			return stockMoveLine;
		}
		else if(saleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_PACK){
			StockMoveLine stockMoveLine = stockMoveLineService.createStockMoveLine(
					null,
					saleOrderLine.getProductName(),
					saleOrderLine.getDescription(),
					BigDecimal.ZERO,
					BigDecimal.ZERO,
					null,
					stockMove,
					StockMoveLineService.TYPE_SALES, saleOrderLine.getSaleOrder().getInAti(), null);

			stockMoveLine.setSaleOrderLine(saleOrderLine);

			if(stockMoveLine != null) {
				stockMove.addStockMoveLineListItem(stockMoveLine);
			}
			return stockMoveLine;
		}
		return null;
	}



	public boolean isStockMoveProduct(SaleOrderLine saleOrderLine) throws AxelorException  {

		Company company = saleOrderLine.getSaleOrder().getCompany();

		StockConfig stockConfig = stockConfigService.getStockConfig(company);

		Product product = saleOrderLine.getProduct();

		if(product != null
				&& ((ProductRepository.PRODUCT_TYPE_SERVICE.equals(product.getProductTypeSelect()) && stockConfig.getHasOutSmForNonStorableProduct())
						|| (ProductRepository.PRODUCT_TYPE_STORABLE.equals(product.getProductTypeSelect()) && stockConfig.getHasOutSmForStorableProduct())) )  {

			return true;
		}

		return false;
	}

	public boolean existActiveStockMoveForSaleOrder(SaleOrder saleOrder){
		long nbStockMove = stockMoveRepo.all().filter("self.saleOrder = ? AND self.statusSelect <> ?", saleOrder, StockMoveRepository.STATUS_CANCELED).count();
		return nbStockMove > 0;
	}

	@Override
	public StockMoveDeliveryStatus checkAllSaleOrderLineIsDelivery(SaleOrder saleOrder) {
		Map<Product, BigDecimal> saleOrderLineProducts = new HashMap<>();
		SaleOrderLineRepository saleOrderLineRepository = Beans.get(SaleOrderLineRepository.class);
		for (SaleOrderLine saleOrderLine: saleOrderLineRepository.findAllBySaleOrder(saleOrder).fetch()) {
			if (!saleOrderLineProducts.containsKey(saleOrderLine.getProduct())) {
				saleOrderLineProducts.put(saleOrderLine.getProduct(), saleOrderLine.getQty());
			}
			else {
				saleOrderLineProducts.replace(saleOrderLine.getProduct(),
						saleOrderLineProducts.get(saleOrderLine.getProduct()).add(saleOrderLine.getQty()));
			}
		}
		if (saleOrderLineProducts.isEmpty()) {
			return StockMoveDeliveryStatus.NOT_DELIVERY;
		}

		Map<Product, BigDecimal> stockMoveLineProducts = new HashMap<>();
		StockMoveLineRepository stockMoveLineRepository = Beans.get(StockMoveLineRepository.class);
		for (StockMoveLine stockMoveLine: stockMoveLineRepository.findAllBySaleOrderAndStatusSelect(saleOrder, StockMoveRepository.STATUS_REALIZED).fetch()) {
			if (!stockMoveLineProducts.containsKey(stockMoveLine.getProduct())) {
				stockMoveLineProducts.put(stockMoveLine.getProduct(), stockMoveLine.getQty());
			}
			else {
				stockMoveLineProducts.replace(stockMoveLine.getProduct(),
						stockMoveLineProducts.get(stockMoveLine.getProduct()).add(stockMoveLine.getQty()));
			}
		}
		if (stockMoveLineProducts.isEmpty()) {
			return StockMoveDeliveryStatus.NOT_DELIVERY;
		}

		for (Map.Entry<Product, BigDecimal> product: stockMoveLineProducts.entrySet()) {
			if (!saleOrderLineProducts.get(product.getKey()).equals(product.getValue())) {
				return StockMoveDeliveryStatus.PARTIAL_DELIVERY;
			}
		}
		return StockMoveDeliveryStatus.ALL_DELIVERY;
	}
}



