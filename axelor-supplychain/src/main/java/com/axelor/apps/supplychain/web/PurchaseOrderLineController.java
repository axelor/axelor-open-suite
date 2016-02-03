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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.supplychain.service.PurchaseOrderLineServiceSupplychainImpl;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class PurchaseOrderLineController {
	
	@Inject
	protected PurchaseOrderLineServiceSupplychainImpl purchaseOrderLineServiceSupplychainImpl;
	
	public void computeAnalyticDistribution(ActionRequest request, ActionResponse response) throws AxelorException{
		PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
		PurchaseOrder purchaseOrder = purchaseOrderLine.getPurchaseOrder();
		if(purchaseOrder == null){
			purchaseOrder = request.getContext().getParentContext().asType(PurchaseOrder.class);
			purchaseOrderLine.setPurchaseOrder(purchaseOrder);
		}
		if(Beans.get(GeneralService.class).getGeneral().getManageAnalyticAccounting()){
			purchaseOrderLine = purchaseOrderLineServiceSupplychainImpl.computeAnalyticDistribution(purchaseOrderLine);
			response.setValue("analyticDistributionLineList", purchaseOrderLine.getAnalyticDistributionLineList());
		}
	}
	
	public void createAnalyticDistributionWithTemplate(ActionRequest request, ActionResponse response) throws AxelorException{
		PurchaseOrderLine purchaseOrderLine = request.getContext().asType(PurchaseOrderLine.class);
		PurchaseOrder purchaseOrder = purchaseOrderLine.getPurchaseOrder();
		if(purchaseOrder == null){
			purchaseOrder = request.getContext().getParentContext().asType(PurchaseOrder.class);
			purchaseOrderLine.setPurchaseOrder(purchaseOrder);
		}
		if(purchaseOrderLine.getAnalyticDistributionTemplate() != null){
			purchaseOrderLine = purchaseOrderLineServiceSupplychainImpl.createAnalyticDistributionWithTemplate(purchaseOrderLine);
			response.setValue("analyticDistributionLineList", purchaseOrderLine.getAnalyticDistributionLineList());
		}
		else{
			throw new AxelorException(I18n.get("No template selected"), IException.CONFIGURATION_ERROR);
		}
	}
}
