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
package com.axelor.apps.production.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.service.config.ProductionConfigService;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.supplychain.db.ILocation;
import com.axelor.apps.supplychain.db.IStockMove;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.supplychain.db.StockMove;
import com.axelor.apps.supplychain.db.StockMoveLine;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class OperationOrderStockMoveService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject
	private StockMoveService stockMoveService;
	
	@Inject
	private StockMoveLineService stockMoveLineService;
	
	@Inject
	private ProductionConfigService productionConfigService;
	
	
	public void createToConsumeStockMove(OperationOrder operationOrder) throws AxelorException {
		
		Company company = operationOrder.getManufOrder().getCompany();
		
		if(operationOrder.getToConsumeProdProductList() != null && company != null) {
			
			StockMove stockMove = this._createToConsumeStockMove(operationOrder, company);
			
			for(ProdProduct prodProduct: operationOrder.getToConsumeProdProductList()) {
				
				StockMoveLine stockMoveLine = this._createStockMoveLine(prodProduct);
				stockMove.addStockMoveLineListItem(stockMoveLine);
				operationOrder.addConsumedStockMoveLineListItem(stockMoveLine);
				
			}
			
			if(stockMove.getStockMoveLineList() != null && !stockMove.getStockMoveLineList().isEmpty()){
				stockMoveService.plan(stockMove);
				operationOrder.setInStockMove(stockMove);
			}
		}
		
	}
	
	
	private StockMove _createToConsumeStockMove(OperationOrder operationOrder, Company company) throws AxelorException  {
		
		Location virtualLocation = productionConfigService.getProductionVirtualLocation(productionConfigService.getProductionConfig(company));
		
		Location fromLocation = null;
		
		if(operationOrder.getProdProcessLine() != null && operationOrder.getProdProcessLine().getProdProcess() != null 
				&& operationOrder.getProdProcessLine().getProdProcess().getLocation() != null)  {
			
			fromLocation = operationOrder.getProdProcessLine().getProdProcess().getLocation();
		}
		else  {
			fromLocation = Location.filter("self.company = ?1 and self.isDefaultLocation = ?2 and self.typeSelect = ?3", 
					company, true, ILocation.INTERNAL).fetchOne();
		}
		
		StockMove stockMove = stockMoveService.createStockMove(
				null, 
				null, 
				company, 
				null, 
				fromLocation, 
				virtualLocation, 
				operationOrder.getPlannedStartDateT().toLocalDate());
		
		return stockMove;
	}
	
	
	
	
	private StockMoveLine _createStockMoveLine(ProdProduct prodProduct) throws AxelorException  {
		
		return stockMoveLineService.createStockMoveLine(
				prodProduct.getProduct(), 
				prodProduct.getQty(), 
				prodProduct.getUnit(), 
				null, 
				null, 
				StockMoveLineService.TYPE_PRODUCTIONS);
			
	}
	
	
	public void finish(OperationOrder operationOrder) throws AxelorException  {
		
		StockMove stockMove = operationOrder.getInStockMove();
		
		if(stockMove != null && stockMove.getStatusSelect() == IStockMove.STATUS_PLANNED && stockMove.getStockMoveLineList() != null)  {
			
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

