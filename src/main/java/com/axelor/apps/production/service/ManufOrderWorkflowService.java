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


import java.util.List;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.production.db.IManufOrder;
import com.axelor.app.production.db.IOperationOrder;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ManufOrderWorkflowService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject
	private OperationOrderWorkflowService operationOrderWorkflowService;
	
	@Inject
	private ManufOrderStockMoveService manufOrderStockMoveService;
	
	
	

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void start(ManufOrder manufOrder)  {
		
		if(manufOrder.getOperationOrderList() != null)  {
			
			OperationOrder operationOrderPriority = OperationOrder.filter("self.manufOrder = ?1", manufOrder).order("priority").fetchOne();
			
			List<OperationOrder> operationOrderList = OperationOrder.filter("self.manufOrder = ?1 AND self.priority = ?2", manufOrder, operationOrderPriority.getPriority()).fetch();
			
			for(OperationOrder operationOrder : operationOrderList)  {
				
				operationOrderWorkflowService.start(operationOrder);
				
			}
			
		}
		
		manufOrder.setStatusSelect(IManufOrder.STATUS_IN_PROGRESS);
		
		manufOrder.save();
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void pause(ManufOrder manufOrder)  {
		
		if(manufOrder.getOperationOrderList() != null)  {
			
			for(OperationOrder operationOrder : manufOrder.getOperationOrderList())  {
				
				if(operationOrder.getStatusSelect() == IOperationOrder.STATUS_IN_PROGRESS)  {
					
					operationOrder.setStatusSelect(IOperationOrder.STATUS_STANDBY);
					
				}
				
			}
			
		}
		
		manufOrder.setStatusSelect(IManufOrder.STATUS_STANDBY);
		
		manufOrder.save();
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void resume(ManufOrder manufOrder)  {
		
		if(manufOrder.getOperationOrderList() != null)  {
			
			for(OperationOrder operationOrder : manufOrder.getOperationOrderList())  {
				
				if(operationOrder.getStatusSelect() == IOperationOrder.STATUS_STANDBY)  {
					
					operationOrder.setStatusSelect(IOperationOrder.STATUS_IN_PROGRESS);
					
				}
				
			}
			
		}
		
		manufOrder.setStatusSelect(IManufOrder.STATUS_IN_PROGRESS);
		
		manufOrder.save();
		
	}
	
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void finish(ManufOrder manufOrder) throws AxelorException  {
		
		if(manufOrder.getOperationOrderList() != null)  {
			
			for(OperationOrder operationOrder : manufOrder.getOperationOrderList())  {
				
				if(operationOrder.getStatusSelect() == IOperationOrder.STATUS_IN_PROGRESS)  {
					
					operationOrderWorkflowService.finish(operationOrder);
					
				}
				
			}
			
		}
		
		manufOrderStockMoveService.finish(manufOrder);
		
		manufOrder.setStatusSelect(IManufOrder.STATUS_FINISHED);
		
		manufOrder.save();
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void cancel(ManufOrder manufOrder)  {
		
		if(manufOrder.getOperationOrderList() != null)  {
			
			for(OperationOrder operationOrder : manufOrder.getOperationOrderList())  {
				
				
				
				operationOrder.setStatusSelect(IOperationOrder.STATUS_CANCELED);
				
			}
			
		}
		
		manufOrder.setStatusSelect(IManufOrder.STATUS_CANCELED);
		
		manufOrder.save();
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public ManufOrder plan(ManufOrder manufOrder) throws AxelorException  {
		
		if(manufOrder.getOperationOrderList() != null)  {
			
			for(OperationOrder operationOrder : manufOrder.getOperationOrderList())  {
				
				operationOrderWorkflowService.plan(operationOrder);
				
			}
			
		}
		
		manufOrder.setPlannedEndDateT(this.computePlannedEndDateT(manufOrder));
		
		if(!manufOrder.getIsConsProOnOperation())  {
			manufOrderStockMoveService.createToConsumeStockMove(manufOrder);
		}

		manufOrderStockMoveService.createToProduceStockMove(manufOrder);
		
		manufOrder.setStatusSelect(IManufOrder.STATUS_PLANNED);
		
		return manufOrder.save();
	}
	
	
	public LocalDateTime computePlannedEndDateT(ManufOrder manufOrder)  {
		
		OperationOrder lastOperationOrder = OperationOrder.filter("self.manufOrder = ?1 ORDER BY self.plannedEndDateT DESC", manufOrder).fetchOne();
		
		if(lastOperationOrder != null)  {
			
			return lastOperationOrder.getPlannedEndDateT();
			
		}
		
		return manufOrder.getPlannedStartDateT();
		
	}
}
