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
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.supplychain.service.PurchaseOrderServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.TimetableService;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class PurchaseOrderController {

	@Inject
	private PurchaseOrderServiceSupplychainImpl purchaseOrderServiceSupplychain;

	@Inject
	protected GeneralService generalService;

	public void createStockMove(ActionRequest request, ActionResponse response) throws AxelorException {

		PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);

		if(purchaseOrder.getId() != null) {
			if (purchaseOrderServiceSupplychain.existActiveStockMoveForPurchaseOrder(purchaseOrder.getId())){
				if(!generalService.getGeneral().getSupplierStockMoveGenerationAuto()){
					response.setFlash(I18n.get("An active stockMove already exists for this purchaseOrder"));
				}
			}else{
				Long stockMoveId = purchaseOrderServiceSupplychain.createStocksMove(Beans.get(PurchaseOrderRepository.class).find(purchaseOrder.getId()));
				if (!generalService.getGeneral().getSupplierStockMoveGenerationAuto()){
					response.setView(ActionView
							.define(I18n.get("Stock move"))
							.model(StockMove.class.getName())
							.add("grid", "stock-move-grid")
							.add("form", "stock-move-form")
							.param("forceEdit", "true")
							.context("_showRecord", String.valueOf(stockMoveId)).map());
				}
			}

		}
	}

	public void getLocation(ActionRequest request, ActionResponse response) {

		PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);

		if(purchaseOrder.getCompany() != null) {

			response.setValue("location", purchaseOrderServiceSupplychain.getLocation(purchaseOrder.getCompany()));
		}
	}


	public void clearPurchaseOrder(ActionRequest request, ActionResponse response) throws AxelorException {

		PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
		purchaseOrder = Beans.get(PurchaseOrderRepository.class).find(purchaseOrder.getId());
		purchaseOrderServiceSupplychain.clearPurchaseOrder(purchaseOrder);

	}

	public void updateTimetable(ActionRequest request, ActionResponse response){
		PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
		if(purchaseOrder.getId() != null && purchaseOrder.getId() > 0){
			purchaseOrder = Beans.get(PurchaseOrderRepository.class).find(purchaseOrder.getId());
		}
		Beans.get(TimetableService.class).updateTimetable(purchaseOrder);
		response.setValues(purchaseOrder);
	}
	
	public void generateBudgetDistribution(ActionRequest request, ActionResponse response){
		PurchaseOrder purchaseOrder = request.getContext().asType(PurchaseOrder.class);
		if(generalService.getGeneral().getManageBudget() && !generalService.getGeneral().getManageMultiBudget()){
			purchaseOrder = Beans.get(PurchaseOrderRepository.class).find(purchaseOrder.getId());
			purchaseOrderServiceSupplychain.generateBudgetDistribution(purchaseOrder);
			response.setValues(purchaseOrder);
		}
	}
}
