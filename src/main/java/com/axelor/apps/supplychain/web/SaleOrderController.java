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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.supplychain.service.SaleOrderPurchaseService;
import com.axelor.apps.supplychain.service.SaleOrderServiceStockImpl;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class SaleOrderController {

	@Inject
	private Provider<SaleOrderServiceStockImpl> saleOrderStockProvider;
	
	@Inject
	private Provider<SaleOrderPurchaseService> saleOrderPurchaseProvider;
	
	@Inject
	private SaleOrderRepository saleOrderRepo;
	
	public void createStockMoves(ActionRequest request, ActionResponse response) throws AxelorException {
		
		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
		
		if(saleOrder.getId() != null) {
			
			saleOrderStockProvider.get().createStocksMovesFromSaleOrder(saleOrderRepo.find(saleOrder.getId()));
		}
	}
	
	public void getLocation(ActionRequest request, ActionResponse response) {
		
		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
		
		if(saleOrder != null) {
			
			Location location = saleOrderStockProvider.get().getLocation(saleOrder.getCompany());
			
			if(location != null) {
				response.setValue("location", location);
			}
		}
	}
	
	
	public void createPurchaseOrders(ActionRequest request, ActionResponse response) throws AxelorException {
		
		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
		
		if(saleOrder.getId() != null) {
			
			saleOrderPurchaseProvider.get().createPurchaseOrders(saleOrderRepo.find(saleOrder.getId()));
		}
	}
	
}
