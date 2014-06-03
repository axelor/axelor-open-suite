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

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.production.db.IOperationOrder;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdHumanResource;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.db.ProdResource;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class OperationOrderService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject
	private OperationOrderStockMoveService operationOrderStockMoveService;
	
	@Inject
	private ProdProductService prodProductService;
	
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public OperationOrder createOperationOrder(ManufOrder manufOrder, ProdProcessLine prodProcessLine, boolean isToInvoice) throws AxelorException  {
		
		OperationOrder operationOrder = this.createOperationOrder(
				manufOrder,
				prodProcessLine.getPriority(), 
				isToInvoice, 
				prodProcessLine.getProdResource(), 
				prodProcessLine.getProdResource(), 
				prodProcessLine);
		
		return operationOrder.save();
	}
	
	
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public OperationOrder createOperationOrder(ManufOrder manufOrder, int priority, boolean isToInvoice, ProdResource prodResource, ProdResource machineProdResource,
			ProdProcessLine prodProcessLine) throws AxelorException  {
		
		logger.debug("Création d'une opération {} pour l'OF {}", priority, manufOrder.getManufOrderSeq());
		
		String operationName = prodProcessLine.getName();
		
		OperationOrder operationOrder = new OperationOrder(
				priority, 
				this.computeName(manufOrder, priority, operationName), 
				operationName,
				isToInvoice, 
				manufOrder, 
				prodResource, 
				machineProdResource, 
				IOperationOrder.STATUS_DRAFT, 
				prodProcessLine);
		
		this._createToConsumeProdProductList(operationOrder, prodProcessLine);
		
		this._createHumanResourceList(operationOrder, machineProdResource);
		
		return operationOrder.save();
	}
	
	
	
	private void _createHumanResourceList(OperationOrder operationOrder, ProdResource prodResource)  {
		
		if(prodResource != null && prodResource.getProdHumanResourceList() != null)  {
			
			for(ProdHumanResource prodHumanResource : prodResource.getProdHumanResourceList())  {
				
				operationOrder.addProdHumanResourceListItem(
						new ProdHumanResource(prodHumanResource.getProduct(), prodHumanResource.getEmployee(), prodHumanResource.getDuration()));
				
			}
			
		}
		
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
	
	
	
	private void _createToConsumeProdProductList(OperationOrder operationOrder, ProdProcessLine prodProcessLine)  {
		
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

