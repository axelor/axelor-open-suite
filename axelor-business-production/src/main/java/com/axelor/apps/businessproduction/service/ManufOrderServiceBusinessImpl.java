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
package com.axelor.apps.businessproduction.service;

import java.math.BigDecimal;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.production.db.IManufOrder;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.service.ManufOrderServiceImpl;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ManufOrderServiceBusinessImpl extends ManufOrderServiceImpl  {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject
	protected OperationOrderServiceBusinessImpl operationOrderServiceBusinessImpl;
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void propagateIsToInvoice(ManufOrder manufOrder) {

		logger.debug("{} is to invoice ? {}", manufOrder.getManufOrderSeq(), manufOrder.getIsToInvoice());
		
		boolean isToInvoice = manufOrder.getIsToInvoice();
		
		if(manufOrder.getOperationOrderList() != null)  {
			for(OperationOrder operationOrder : manufOrder.getOperationOrderList())  {
				
				operationOrder.setIsToInvoice(isToInvoice);
				
			}
		}
		
		manufOrderRepo.save(manufOrder);
		
	}

	
	
	@Override
	public ManufOrder createManufOrder(Product product, BigDecimal qty, int priority, boolean isToInvoice, Company company,
			BillOfMaterial billOfMaterial, LocalDateTime plannedStartDateT) throws AxelorException  {
		
		logger.debug("Création d'un OF {}", priority);
		
		ProdProcess prodProcess = billOfMaterial.getProdProcess();
		
		ManufOrder manufOrder = new ManufOrder(
				qty,
				company, 
				null, 
				priority, 
				this.isManagedConsumedProduct(billOfMaterial), 
				billOfMaterial, 
				product,
				prodProcess, 
				plannedStartDateT, 
				IManufOrder.STATUS_DRAFT);
		
		manufOrder.setIsToInvoice(isToInvoice);
			
		if(prodProcess != null && prodProcess.getProdProcessLineList() != null)  {
			for(ProdProcessLine prodProcessLine : this._sortProdProcessLineByPriority(prodProcess.getProdProcessLineList()))  {
				
				manufOrder.addOperationOrderListItem(
						operationOrderServiceBusinessImpl.createOperationOrder(manufOrder, prodProcessLine, isToInvoice));
				
			}
		}	
			
		if(!manufOrder.getIsConsProOnOperation())  {
			this.createToConsumeProdProductList(manufOrder);
		}
		
		this.createToProduceProdProductList(manufOrder);
		
		return manufOrder; 
		
	}
	
	@Override
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void preFillOperations(ManufOrder manufOrder) throws AxelorException{
		
		BillOfMaterial billOfMaterial = manufOrder.getBillOfMaterial();
		
		manufOrder.setIsConsProOnOperation(this.isManagedConsumedProduct(billOfMaterial));
		
		if(manufOrder.getProdProcess() == null){
			manufOrder.setProdProcess(billOfMaterial.getProdProcess());
		}
		ProdProcess prodProcess = manufOrder.getProdProcess();
		
		if(manufOrder.getPlannedStartDateT() == null){
			manufOrder.setPlannedStartDateT(generalService.getTodayDateTime().toLocalDateTime());
		}
		
		if(prodProcess != null && prodProcess.getProdProcessLineList() != null)  {
			
			for(ProdProcessLine prodProcessLine : this._sortProdProcessLineByPriority(prodProcess.getProdProcessLineList()))  {
				manufOrder.addOperationOrderListItem(operationOrderServiceBusinessImpl.createOperationOrder(manufOrder, prodProcessLine, manufOrder.getIsToInvoice()));
			}
			
		}
		
		manufOrderRepo.save(manufOrder);
		
		manufOrder.setPlannedEndDateT(manufOrderWorkflowService.computePlannedEndDateT(manufOrder));
		
		if(!manufOrder.getIsConsProOnOperation())  {
			this.createToConsumeProdProductList(manufOrder);
		}
		
		this.createToProduceProdProductList(manufOrder);
		
		manufOrderRepo.save(manufOrder);
	}
	
}
