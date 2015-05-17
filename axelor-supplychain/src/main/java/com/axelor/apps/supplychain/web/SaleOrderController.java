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

import java.util.Map;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.axelor.apps.supplychain.service.SaleOrderPurchaseService;
import com.axelor.apps.supplychain.service.SaleOrderServiceStockImpl;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.beust.jcommander.internal.Maps;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class SaleOrderController {

	@Inject
	private SaleOrderRepository saleOrderRepo;
	
	public void createStockMoves(ActionRequest request, ActionResponse response) throws AxelorException {
		
		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
		
		if(saleOrder.getId() != null) {
			
			Beans.get(SaleOrderServiceStockImpl.class).createStocksMovesFromSaleOrder(saleOrderRepo.find(saleOrder.getId()));
		}
	}
	
	public void getLocation(ActionRequest request, ActionResponse response) {
		
		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
		
		if(saleOrder != null) {
			
			Location location = Beans.get(SaleOrderServiceStockImpl.class).getLocation(saleOrder.getCompany());
			
			if(location != null) {
				response.setValue("location", location);
			}
		}
	}
	
	
	public void createPurchaseOrders(ActionRequest request, ActionResponse response) throws AxelorException {
		
		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
		
		if(saleOrder.getId() != null) {
			
			Beans.get(SaleOrderPurchaseService.class).createPurchaseOrders(saleOrderRepo.find(saleOrder.getId()));
		}
	}

	public void alertGenerateInvoice( ActionRequest request, ActionResponse response ) throws AxelorException  {

    	Context context = request.getContext();
    	SaleOrder saleOrder = context.asType(SaleOrder.class);

		if ( !Beans.get(SaleOrderInvoiceService.class).checkIfSaleOrderIsCompletelyInvoiced(saleOrder) ){ return; };
		
		Map<String,String> alert = Maps.newHashMap();
		alert.put("alert", I18n.get(IExceptionMessage.SO_INVOICE_5));
		response.setData( Lists.newArrayList( alert ) );
		
	}
}
