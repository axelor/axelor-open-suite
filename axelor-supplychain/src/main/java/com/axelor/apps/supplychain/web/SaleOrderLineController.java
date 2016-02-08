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
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.service.SaleOrderLineServiceSupplyChainImpl;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class SaleOrderLineController {
	
	@Inject
	protected SaleOrderLineServiceSupplyChainImpl saleOrderLineServiceSupplyChainImpl;
	
	public void computeAnalyticDistribution(ActionRequest request, ActionResponse response) throws AxelorException{
		SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
		SaleOrder saleOrder = saleOrderLine.getSaleOrder();
		if(saleOrder == null){
			saleOrder = request.getContext().getParentContext().asType(SaleOrder.class);
			saleOrderLine.setSaleOrder(saleOrder);
		}
		if(Beans.get(GeneralService.class).getGeneral().getManageAnalyticAccounting()){
			saleOrderLine = saleOrderLineServiceSupplyChainImpl.computeAnalyticDistribution(saleOrderLine);
			response.setValue("analyticDistributionLineList", saleOrderLine.getAnalyticDistributionLineList());
		}
	}
	
	public void createAnalyticDistributionWithTemplate(ActionRequest request, ActionResponse response) throws AxelorException{
		SaleOrderLine saleOrderLine = request.getContext().asType(SaleOrderLine.class);
		SaleOrder saleOrder = saleOrderLine.getSaleOrder();
		if(saleOrder == null){
			saleOrder = request.getContext().getParentContext().asType(SaleOrder.class);
			saleOrderLine.setSaleOrder(saleOrder);
		}
		if(saleOrderLine.getAnalyticDistributionTemplate() != null){
			saleOrderLine = saleOrderLineServiceSupplyChainImpl.createAnalyticDistributionWithTemplate(saleOrderLine);
			response.setValue("analyticDistributionLineList", saleOrderLine.getAnalyticDistributionLineList());
		}
		else{
			throw new AxelorException(I18n.get("No template selected"), IException.CONFIGURATION_ERROR);
		}
	}
}
