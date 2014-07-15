/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.supplychain.service.PurchaseOrderServiceSupplychainImpl;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class PurchaseOrderController {

	@Inject
	private SequenceService sequenceService;
	
	@Inject
	private Provider<PurchaseOrderServiceSupplychainImpl> purchaseOrderServiceSupplychainProvider;
	
	
	private static final Logger LOG = LoggerFactory.getLogger(PurchaseOrderController.class);

	
	public void createStockMoves(ActionRequest request, ActionResponse response) throws AxelorException {
		
		PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
		
		if(purchaseOrder.getId() != null) {

			purchaseOrderServiceSupplychainProvider.get().createStocksMoves(PurchaseOrder.find(purchaseOrder.getId()));
		}
	}
	
	public void getLocation(ActionRequest request, ActionResponse response) {
		
		PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
		
		if(purchaseOrder.getCompany() != null) {
			
			response.setValue("location", purchaseOrderServiceSupplychainProvider.get().getLocation(purchaseOrder.getCompany()));
		}
	}
	
	
	public void clearPurchaseOrder(ActionRequest request, ActionResponse response) throws AxelorException {
		
		PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
			
		purchaseOrderServiceSupplychainProvider.get().clearPurchaseOrder(purchaseOrder);
		
	}
	
	
	
}
