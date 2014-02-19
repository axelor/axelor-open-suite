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
	
	
//	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
//	public void copyToProduce(OperationOrder operationOrder)  {
//		
//		if(operationOrder.getToProduceProdProductList() != null)  {
//			
//			for(ProdProduct prodProduct : operationOrder.getToProduceProdProductList())  {
//				
//				operationOrder.addProducedProdProductListItem(new ProdProduct(prodProduct.getProduct(), prodProduct.getQty(), prodProduct.getUnit()));
//
//			}
//			
//		}
//		
//		operationOrder.save();
//		
//	}
//	
//	
//	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
//	public void copyToConsume(OperationOrder operationOrder)  {
//		
//		if(operationOrder.getToConsumeProdProductList() != null)  {
//			
//			for(ProdProduct prodProduct : operationOrder.getToConsumeProdProductList())  {
//				
//				operationOrder.addConsumedProdProductListItem(new ProdProduct(prodProduct.getProduct(), prodProduct.getQty(), prodProduct.getUnit()));
//
//			}
//			
//		}
//		
//		operationOrder.save();
//		
//	}
	
	
	
}

