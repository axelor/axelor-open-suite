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

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.production.db.IOperationOrder;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdHumanResource;
//import com.axelor.apps.production.db.ProdHumanResource;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;

public class OperationOrderServiceImpl implements OperationOrderService  {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public OperationOrder createOperationOrder(ManufOrder manufOrder, ProdProcessLine prodProcessLine) throws AxelorException  {
		
		OperationOrder operationOrder = this.createOperationOrder(
				manufOrder,
				prodProcessLine.getPriority(), 
				prodProcessLine.getWorkCenter(), 
				prodProcessLine.getWorkCenter(), 
				prodProcessLine);
		
		return Beans.get(OperationOrderRepository.class).save(operationOrder);
	}
	
	
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public OperationOrder createOperationOrder(ManufOrder manufOrder, int priority, WorkCenter workCenter, WorkCenter machineWorkCenter,
			ProdProcessLine prodProcessLine) throws AxelorException  {
		
		logger.debug("Création d'une opération {} pour l'OF {}", priority, manufOrder.getManufOrderSeq());
		
		String operationName = prodProcessLine.getName();
		
		OperationOrder operationOrder = new OperationOrder(
				priority, 
				this.computeName(manufOrder, priority, operationName), 
				operationName,
				manufOrder, 
				workCenter, 
				machineWorkCenter, 
				IOperationOrder.STATUS_DRAFT, 
				prodProcessLine);
		
		this._createToConsumeProdProductList(operationOrder, prodProcessLine);
		
		this._createHumanResourceList(operationOrder, machineWorkCenter);
		
		return Beans.get(OperationOrderRepository.class).save(operationOrder);
	}
	
	
	protected void _createHumanResourceList(OperationOrder operationOrder, WorkCenter workCenter)  {
		
		if(workCenter != null && workCenter.getProdHumanResourceList() != null)  {
			
			for(ProdHumanResource prodHumanResource : workCenter.getProdHumanResourceList())  {
				
				operationOrder.addProdHumanResourceListItem(this.copyProdHumanResource(prodHumanResource));
				
			}
			
		}
		
	}
	
	protected ProdHumanResource copyProdHumanResource(ProdHumanResource prodHumanResource)  {
		
		return new ProdHumanResource(prodHumanResource.getProduct(), prodHumanResource.getDuration());
	}

	
	public String computeName(ManufOrder manufOrder, int priority, String operationName)  {
		
		String name = "";
		if(manufOrder != null)  {
			
			if(manufOrder.getManufOrderSeq() != null)  {
				name += manufOrder.getManufOrderSeq();
			}
			else  {
				name += manufOrder.getId();
			}
			
		}
		
		name += "-" + priority + "-" + operationName;
		
		return name;
	}
	
	
	
	protected void _createToConsumeProdProductList(OperationOrder operationOrder, ProdProcessLine prodProcessLine)  {
		
		BigDecimal manufOrderQty = operationOrder.getManufOrder().getQty();
		
		if(prodProcessLine.getToConsumeProdProductList() != null)  {
			
			for(ProdProduct prodProduct : prodProcessLine.getToConsumeProdProductList())  {
				
				operationOrder.addToConsumeProdProductListItem(
						new ProdProduct(prodProduct.getProduct(), prodProduct.getQty().multiply(manufOrderQty), prodProduct.getUnit()));
				
			}
			
		}
		
	}
	
	
	
	
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

