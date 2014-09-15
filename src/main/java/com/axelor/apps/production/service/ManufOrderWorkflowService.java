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

import java.util.List;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.production.db.IManufOrder;
import com.axelor.app.production.db.IOperationOrder;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ManufOrderWorkflowService extends ManufOrderRepository{

	@Inject
	private OperationOrderWorkflowService operationOrderWorkflowService;
	
	@Inject
	private ManufOrderStockMoveService manufOrderStockMoveService;
	
	

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void start(ManufOrder manufOrder)  {
		
		if(manufOrder.getOperationOrderList() != null)  {
			
			OperationOrder operationOrderPriority = operationOrderWorkflowService.all().filter("self.manufOrder = ?1", manufOrder).order("priority").fetchOne();
			
			List<OperationOrder> operationOrderList = (List<OperationOrder>)operationOrderWorkflowService.all().filter("self.manufOrder = ?1 AND self.priority = ?2", manufOrder, operationOrderPriority.getPriority()).fetch();
			
			for(OperationOrder operationOrder : operationOrderList)  {
				
				operationOrderWorkflowService.start(operationOrder);
				
			}
			
		}
		
		manufOrder.setStatusSelect(IManufOrder.STATUS_IN_PROGRESS);
		
		save(manufOrder);
		
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
		
		save(manufOrder);
		
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
		
		save(manufOrder);
		
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
		
		save(manufOrder);
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void cancel(ManufOrder manufOrder) throws AxelorException  {
		
		if(manufOrder.getOperationOrderList() != null)  {
			
			for(OperationOrder operationOrder : manufOrder.getOperationOrderList())  {
				
				if(operationOrder.getStatusSelect() != IOperationOrder.STATUS_CANCELED)  {
					operationOrderWorkflowService.cancel(operationOrder);
				}
			}
			
		}
		
		manufOrderStockMoveService.cancel(manufOrder);
		
		manufOrder.setStatusSelect(IManufOrder.STATUS_CANCELED);
		
		save(manufOrder);
		
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
		
		return save(manufOrder);
	}
	
	
	public LocalDateTime computePlannedEndDateT(ManufOrder manufOrder)  {
		
		OperationOrder lastOperationOrder = operationOrderWorkflowService.all().filter("self.manufOrder = ?1 ORDER BY self.plannedEndDateT DESC", manufOrder).fetchOne();
		
		if(lastOperationOrder != null)  {
			
			return lastOperationOrder.getPlannedEndDateT();
			
		}
		
		return manufOrder.getPlannedStartDateT();
		
	}
}
