/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
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
 * Software distributed under the License is distributed on an "AS IS"
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
package com.axelor.apps.production.service;
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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.service.config.ProductionConfigService;
import com.axelor.apps.supplychain.db.Location;
import com.axelor.apps.supplychain.db.StockMove;
import com.axelor.apps.supplychain.db.StockMoveLine;
import com.axelor.apps.supplychain.service.StockMoveLineService;
import com.axelor.apps.supplychain.service.StockMoveService;
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
				
				stockMove.addStockMoveLineListItem(this._createStockMoveLine(prodProduct));
				
			}
			
			if(stockMove.getStockMoveLineList() != null && !stockMove.getStockMoveLineList().isEmpty()){
				stockMoveService.plan(stockMove);
				operationOrder.setInStockMove(stockMove);
			}
		}
		
	}
	
	
	private StockMove _createToConsumeStockMove(OperationOrder operationOrder, Company company) throws AxelorException  {
		
		Location virtualLocation = productionConfigService.getProductionVirtualLocation(productionConfigService.getProductionConfig(company));
		
		StockMove stockMove = stockMoveService.createStockMove(
				null, 
				null, 
				company, 
				null, 
				operationOrder.getProdProcessLine().getProdProcess().getLocation(), 
				virtualLocation, 
				operationOrder.getPlannedStartDateT().toLocalDate());
		
		return stockMove;
	}
	
	
	
	public void createToProduceStockMove(OperationOrder operationOrder) throws AxelorException {
		
		Company company = operationOrder.getManufOrder().getCompany();
		
		if(operationOrder.getToProduceProdProductList() != null && company != null) {
			
			StockMove stockMove = this._createToProduceStockMove(operationOrder, company);
			
			for(ProdProduct prodProduct: operationOrder.getToProduceProdProductList()) {
				
				stockMove.addStockMoveLineListItem(this._createStockMoveLine(prodProduct));
				
			}
			
			if(stockMove.getStockMoveLineList() != null && !stockMove.getStockMoveLineList().isEmpty()){
				stockMoveService.plan(stockMove);
				operationOrder.setOutStockMove(stockMove);
			}
		}
		
	}
	
	
	private StockMove _createToProduceStockMove(OperationOrder operationOrder, Company company) throws AxelorException  {
		
		Location virtualLocation = productionConfigService.getProductionVirtualLocation(productionConfigService.getProductionConfig(company));
		
		StockMove stockMove = stockMoveService.createStockMove(
				null, 
				null, 
				company, 
				null, 
				virtualLocation, 
				operationOrder.getProdProcessLine().getProdProcess().getLocation(), 
				operationOrder.getPlannedEndDateT().toLocalDate());
		
		return stockMove;
	}
	
	
	
	
	private StockMoveLine _createStockMoveLine(ProdProduct prodProduct) throws AxelorException  {
		
		return stockMoveLineService.createStockMoveLine(
				prodProduct.getProduct(), 
				prodProduct.getQty(), 
				prodProduct.getUnit(), 
				null, 
				null,
				null, 
				StockMoveLineService.TYPE_PRODUCTIONS);
			
	}
	
	
	
}

