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
package com.axelor.apps.production.service;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
//import com.axelor.apps.production.db.ProdHumanResource;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface OperationOrderService {

	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public OperationOrder createOperationOrder(ManufOrder manufOrder, ProdProcessLine prodProcessLine) throws AxelorException;
	
	
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public OperationOrder createOperationOrder(ManufOrder manufOrder, int priority, WorkCenter workCenter, WorkCenter machineWorkCenter,
			ProdProcessLine prodProcessLine) throws AxelorException;
	
	
	
	public String computeName(ManufOrder manufOrder, int priority, String operationName);
	
	
//	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
//	public void generateWaste(OperationOrder operationOrder)  {
//		
//		if(operationOrder.getToProduceProdProductList() != null)  {
//			
//			for(ProdProduct prodProduct : operationOrder.getToProduceProdProductList())  {
//				
//				BigDecimal producedQty = prodProductService.computeQuantity(ProdProduct.filter("self.producedOperationOrder = ?1 AND self.product = ?2", operationOrder, prodProduct.getProduct()).fetch());
//			
//				if(producedQty.compareTo(prodProduct))
//			}
//			
//		}
//		
//	}
	
	
	
}

