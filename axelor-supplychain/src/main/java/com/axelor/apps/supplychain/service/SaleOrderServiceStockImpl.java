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
package com.axelor.apps.supplychain.service;

import java.util.ArrayList;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IProduct;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.ISaleOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.exception.IExceptionMessage;
import com.axelor.apps.sale.service.SaleOrderLineService;
import com.axelor.apps.sale.service.SaleOrderServiceImpl;
import com.axelor.apps.stock.db.ILocation;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.LocationRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;

public class SaleOrderServiceStockImpl extends SaleOrderServiceImpl {

	@Inject
	private SaleOrderLineService saleOrderLineService;

	@Inject
	private StockMoveService stockMoveService;
	
	@Inject
	private StockMoveLineService stockMoveLineService;

	@Inject
	private StockConfigService stockConfigService;

	@Inject
	private LocationRepository locationRepo;


	public Location getLocation(Company company)  {
		
		return locationRepo.all().filter("self.company = ?1 and self.isDefaultLocation = ?2 and self.typeSelect = ?3", 
				company, true, ILocation.INTERNAL).fetchOne();
	}
	

	/**
	 * Méthode permettant de créer un StockMove à partir d'un SaleOrder.
	 * @param saleOrder l'objet saleOrder
	 * @throws AxelorException Aucune séquence de StockMove (Livraison) n'a été configurée
	 */
	public void createStocksMovesFromSaleOrder(SaleOrder saleOrder) throws AxelorException {
		
		Company company = saleOrder.getCompany();
		
		if(saleOrder.getSaleOrderLineList() != null && company != null) {
			
			this.checkStockMoveProduct(saleOrder);
			
			StockMove stockMove = this.createStockMove(saleOrder, company);
			
			for(SaleOrderLine saleOrderLine: saleOrder.getSaleOrderLineList()) {
				
				this.createStockMoveLine(stockMove, saleOrderLine, company);
				
			}
			
			if(stockMove.getStockMoveLineList() != null && !stockMove.getStockMoveLineList().isEmpty()){
				stockMoveService.plan(stockMove);
			}
		}
	}
	
	
	public StockMove createStockMove(SaleOrder saleOrder, Company company) throws AxelorException  {
		
		Location toLocation = locationRepo.all().filter("self.isDefaultLocation = true and self.company = ?1 and self.typeSelect = ?2", company, ILocation.EXTERNAL).fetchOne();
		
		if(toLocation == null)  {
			
			toLocation = stockConfigService.getCustomerVirtualLocation(stockConfigService.getStockConfig(company));
		}
		
		StockMove stockMove = stockMoveService.createStockMove(
				null,
				saleOrder.getDeliveryAddress(), 
				company, 
				saleOrder.getClientPartner(), 
				saleOrder.getLocation(), 
				toLocation, 
				saleOrder.getShipmentDate());
		
		stockMove.setSaleOrder(saleOrder);
		stockMove.setStockMoveLineList(new ArrayList<StockMoveLine>());
		
		return stockMove;
	}
	
	
	public void createStockMoveLine(StockMove stockMove, SaleOrderLine saleOrderLine, Company company) throws AxelorException  {
		
		Product product = saleOrderLine.getProduct();
		
		if(this.isStockMoveProduct(saleOrderLine)) {
			
			StockMoveLine stockMoveLine = stockMoveLineService.createStockMoveLine(
					product, 
					saleOrderLine.getProductName(),
					saleOrderLine.getDescription(),
					saleOrderLine.getQty(), 
					saleOrderLine.getUnit(), 
					saleOrderLineService.computeDiscount(saleOrderLine), 
					stockMove,
					1);
			
			stockMoveLine.setSaleOrderLine(saleOrderLine);
			
			if(stockMoveLine != null) {
				stockMove.getStockMoveLineList().add(stockMoveLine);
			}
		}	
	}
		
	
	public void checkStockMoveProduct(SaleOrder saleOrder) throws AxelorException  {

		if(saleOrder.getSaleOrderLineList() != null && this.isSaleOrderInvoicingMethod(saleOrder))  {
			for(SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList())  {
				
				this.checkStockMoveProduct(saleOrderLine);
				
			}
		}
	}
	
	
	public void checkStockMoveProduct(SaleOrderLine saleOrderLine) throws AxelorException  {
		
		if(!this.isStockMoveProduct(saleOrderLine))  {
			throw new AxelorException(I18n.get(IExceptionMessage.SALES_ORDER_STOCK_MOVE_1), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	
	public boolean isSaleOrderInvoicingMethod(SaleOrder saleOrder)  {
		
		return saleOrder.getInvoicingTypeSelect() == ISaleOrder.INVOICING_TYPE_PER_SHIPMENT;
		
	}
	
	
	public boolean isStockMoveProduct(SaleOrderLine saleOrderLine) throws AxelorException  {
		
		Company company = saleOrderLine.getSaleOrder().getCompany();
		
		StockConfig stockConfig = stockConfigService.getStockConfig(company);
		
		Product product = saleOrderLine.getProduct();
		
//		if(product != null
//				&& ((product.getProductTypeSelect().equals(IProduct.PRODUCT_TYPE_SERVICE) && supplychainConfig.getHasOutSmForNonStorableProduct())
//						|| (product.getProductTypeSelect().equals(IProduct.PRODUCT_TYPE_STORABLE) && supplychainConfig.getHasOutSmForStorableProduct())) 
//				&& saleOrderLine.getSaleSupplySelect() == IProduct.SALE_SUPPLY_FROM_STOCK)  {
			
		if(product != null
				&& ((product.getProductTypeSelect().equals(IProduct.PRODUCT_TYPE_SERVICE) && stockConfig.getHasOutSmForNonStorableProduct())
						|| (product.getProductTypeSelect().equals(IProduct.PRODUCT_TYPE_STORABLE) && stockConfig.getHasOutSmForStorableProduct())) )  {
			
			return true;
		}
		
		return false;
	}
}



