/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.supplychain.service;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IProduct;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.supplychain.db.ILocation;
import com.axelor.apps.supplychain.db.ISalesOrder;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.supplychain.db.SalesOrder;
import com.axelor.apps.supplychain.db.SalesOrderLine;
import com.axelor.apps.supplychain.db.StockMove;
import com.axelor.apps.supplychain.db.StockMoveLine;
import com.axelor.apps.supplychain.db.SupplychainConfig;
import com.axelor.apps.supplychain.exceptions.IExceptionMessage;
import com.axelor.apps.supplychain.service.config.SupplychainConfigService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.meta.service.MetaTranslations;
import com.google.inject.Inject;

public class SalesOrderStockMoveService {

	private static final Logger LOG = LoggerFactory.getLogger(SalesOrderStockMoveService.class); 

	@Inject
	private SalesOrderLineService salesOrderLineService;

	@Inject
	private StockMoveService stockMoveService;
	
	@Inject
	private StockMoveLineService stockMoveLineService;

	@Inject
	private SupplychainConfigService supplychainConfigService;


	/**
	 * Méthode permettant de créer un StockMove à partir d'un SalesOrder.
	 * @param salesOrder l'objet salesOrder
	 * @throws AxelorException Aucune séquence de StockMove (Livraison) n'a été configurée
	 */
	public StockMove createStocksMovesFromSalesOrder(SalesOrder salesOrder) throws AxelorException {
		
		Company company = salesOrder.getCompany();
		
		if(salesOrder.getSalesOrderLineList() != null && company != null) {
			
			this.checkStockMoveProduct(salesOrder);
			
			StockMove stockMove = this.createStockMove(salesOrder, company);
			
			for(SalesOrderLine salesOrderLine: salesOrder.getSalesOrderLineList()) {
				
				this.createStockMoveLine(stockMove, salesOrderLine, company);
				
			}
			
			if(stockMove.getStockMoveLineList() != null && !stockMove.getStockMoveLineList().isEmpty()){
				stockMoveService.plan(stockMove);
			}
			return stockMove;
		}
		return null;
	}
	
	
	public StockMove createStockMove(SalesOrder salesOrder, Company company) throws AxelorException  {
		
		Location toLocation = Location.all().filter("self.isDefaultLocation = true and self.company = ?1 and self.typeSelect = ?2", company, ILocation.EXTERNAL).fetchOne();
		
		if(toLocation == null)  {
			
			toLocation = supplychainConfigService.getCustomerVirtualLocation(supplychainConfigService.getSupplychainConfig(company));
		}
		
		StockMove stockMove = stockMoveService.createStockMove(
				salesOrder.getDeliveryAddress(), 
				company, 
				salesOrder.getClientPartner(), 
				salesOrder.getLocation(), 
				toLocation, 
				salesOrder.getShipmentDate());
		
		stockMove.setSalesOrder(salesOrder);
		stockMove.setStockMoveLineList(new ArrayList<StockMoveLine>());
		
		return stockMove;
	}
	
	
	public void createStockMoveLine(StockMove stockMove, SalesOrderLine salesOrderLine, Company company) throws AxelorException  {
		
		Product product = salesOrderLine.getProduct();
		
		if(this.isStockMoveProduct(salesOrderLine)) {
			
			StockMoveLine stockMoveLine = stockMoveLineService.createStockMoveLine(
					product, 
					salesOrderLine.getQty(), 
					salesOrderLine.getUnit(), 
					salesOrderLineService.computeDiscount(salesOrderLine), 
					stockMove,
					salesOrderLine.getProductVariant(), 
					1);
			
			if(stockMoveLine != null) {
				stockMove.getStockMoveLineList().add(stockMoveLine);
			}
		}	
	}
		
	
	public void checkStockMoveProduct(SalesOrder salesOrder) throws AxelorException  {

		if(salesOrder.getSalesOrderLineList() != null && this.isSalesOrderInvoicingMethod(salesOrder))  {
			for(SalesOrderLine salesOrderLine : salesOrder.getSalesOrderLineList())  {
				
				this.checkStockMoveProduct(salesOrderLine);
				
			}
		}
	}
	
	
	public void checkStockMoveProduct(SalesOrderLine salesOrderLine) throws AxelorException  {
		
		if(!this.isStockMoveProduct(salesOrderLine))  {
			throw new AxelorException(new MetaTranslations().get(IExceptionMessage.SALES_ORDER_STOCK_MOVE_1), IException.CONFIGURATION_ERROR);
		}
		
	}
	
	
	public boolean isSalesOrderInvoicingMethod(SalesOrder salesOrder)  {
		
		return salesOrder.getInvoicingTypeSelect() == ISalesOrder.INVOICING_TYPE_PER_SHIPMENT;
		
	}
	
	
	public boolean isStockMoveProduct(SalesOrderLine salesOrderLine) throws AxelorException  {
		
		Company company = salesOrderLine.getSalesOrder().getCompany();
		
		SupplychainConfig supplychainConfig = supplychainConfigService.getSupplychainConfig(company);
		
		Product product = salesOrderLine.getProduct();
		
		if(product != null
				&& ((product.getProductTypeSelect().equals(IProduct.PRODUCT_TYPE_SERVICE) && supplychainConfig.getHasOutSmForNonStorableProduct())
						|| (product.getProductTypeSelect().equals(IProduct.PRODUCT_TYPE_STORABLE) && supplychainConfig.getHasOutSmForStorableProduct())) 
				&& salesOrderLine.getSaleSupplySelect() == IProduct.SALE_SUPPLY_FROM_STOCK)  {
			
			return true;
		}
		
		return false;
	}
	
}

