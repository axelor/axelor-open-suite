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
package com.axelor.apps.production.service;

import java.math.BigDecimal;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.service.config.StockConfigProductionService;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class OperationOrderStockMoveService {

	protected StockMoveService stockMoveService;
	protected StockMoveLineService stockMoveLineService;
	protected StockLocationRepository stockLocationRepo;
	
	@Inject
	public OperationOrderStockMoveService(StockMoveService stockMoveService, StockMoveLineService stockMoveLineService,
			StockLocationRepository stockLocationRepo)  {
		this.stockMoveService = stockMoveService;
		this.stockMoveLineService = stockMoveLineService;
		this.stockLocationRepo = stockLocationRepo;
	}

	public void createToConsumeStockMove(OperationOrder operationOrder) throws AxelorException {

		Company company = operationOrder.getManufOrder().getCompany();

		if(operationOrder.getToConsumeProdProductList() != null && company != null) {

			StockMove stockMove = this._createToConsumeStockMove(operationOrder, company);

			for(ProdProduct prodProduct: operationOrder.getToConsumeProdProductList()) {

				StockMoveLine stockMoveLine = this._createStockMoveLine(prodProduct, stockMove);
				stockMove.addStockMoveLineListItem(stockMoveLine);

			}

			if(stockMove.getStockMoveLineList() != null && !stockMove.getStockMoveLineList().isEmpty()){
				stockMoveService.plan(stockMove);
				operationOrder.setInStockMove(stockMove);
			}

			//fill here the consumed stock move line list item to manage the
			//case where we had to split tracked stock move lines
			if (stockMove.getStockMoveLineList() != null) {
			    for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
					operationOrder.addConsumedStockMoveLineListItem(stockMoveLine);
				}
			}
		}

	}


	protected StockMove _createToConsumeStockMove(OperationOrder operationOrder, Company company) throws AxelorException  {

		StockConfigProductionService stockConfigService = Beans.get(StockConfigProductionService.class);
		StockConfig stockConfig = stockConfigService.getStockConfig(company);
		StockLocation virtualStockLocation = stockConfigService.getProductionVirtualStockLocation(stockConfig);

		StockLocation fromStockLocation;

		ProdProcessLine prodProcessLine = operationOrder.getProdProcessLine();
		if (operationOrder.getManufOrder().getIsConsProOnOperation() && prodProcessLine != null && prodProcessLine.getStockLocation() != null) {
			fromStockLocation = prodProcessLine.getStockLocation();
		} else if (!operationOrder.getManufOrder().getIsConsProOnOperation() && prodProcessLine != null && prodProcessLine.getProdProcess() != null && prodProcessLine.getProdProcess().getStockLocation() != null) {
			fromStockLocation = prodProcessLine.getProdProcess().getStockLocation();
		} else {
			fromStockLocation = stockConfigService.getDefaultStockLocation(stockConfig);
		}

		return stockMoveService.createStockMove(null, null, company, null, fromStockLocation, virtualStockLocation,
				null, operationOrder.getPlannedStartDateT().toLocalDate(), null, null, null);

	}




	protected StockMoveLine _createStockMoveLine(ProdProduct prodProduct, StockMove stockMove) throws AxelorException  {

		return stockMoveLineService.createStockMoveLine(
				prodProduct.getProduct(),
				prodProduct.getProduct().getName(),
				prodProduct.getProduct().getDescription(),
				prodProduct.getQty(),
				prodProduct.getProduct().getCostPrice(),
				prodProduct.getUnit(),
				stockMove,
				StockMoveLineService.TYPE_IN_PRODUCTIONS, false, BigDecimal.ZERO);

	}


	public void finish(OperationOrder operationOrder) throws AxelorException  {

		StockMove stockMove = operationOrder.getInStockMove();

		if(stockMove != null && stockMove.getStatusSelect() == StockMoveRepository.STATUS_PLANNED && stockMove.getStockMoveLineList() != null)  {

			stockMoveService.realize(stockMove);

		}

	}


	public void cancel(OperationOrder operationOrder) throws AxelorException  {

		StockMove stockMove = operationOrder.getInStockMove();

		if(stockMove != null && stockMove.getStockMoveLineList() != null)  {

			stockMoveService.cancel(stockMove);

			for(StockMoveLine stockMoveLine : stockMove.getStockMoveLineList())  {

				stockMoveLine.setConsumedOperationOrder(null);

			}

		}

	}
}

