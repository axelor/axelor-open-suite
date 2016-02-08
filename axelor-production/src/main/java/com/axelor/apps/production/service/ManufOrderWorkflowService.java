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

import java.util.List;

import org.joda.time.LocalDateTime;

import com.axelor.app.production.db.IManufOrder;
import com.axelor.app.production.db.IOperationOrder;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ManufOrderWorkflowService {

	@Inject
	private OperationOrderWorkflowService operationOrderWorkflowService;
	
	@Inject
	private OperationOrderRepository operationOrderRepo;
	
	@Inject
	private ManufOrderStockMoveService manufOrderStockMoveService;
	
	@Inject
	protected GeneralService generalService;
	
	@Inject
	protected ManufOrderRepository manufOrderRepo;

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void start(ManufOrder manufOrder)  {
		
		if(manufOrder.getOperationOrderList() != null)  {
			
			OperationOrder operationOrderPriority = operationOrderRepo.all().filter("self.manufOrder = ?1", manufOrder).order("priority").fetchOne();
			
			List<OperationOrder> operationOrderList = (List<OperationOrder>)operationOrderRepo.all().filter("self.manufOrder = ?1 AND self.priority = ?2", manufOrder, operationOrderPriority.getPriority()).fetch();
			
			for(OperationOrder operationOrder : operationOrderList)  {
				
				operationOrderWorkflowService.start(operationOrder);
				
			}
			
		}
		
		manufOrder.setStatusSelect(IManufOrder.STATUS_IN_PROGRESS);
		
		manufOrderRepo.save(manufOrder);
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void pause(ManufOrder manufOrder)  {
		
		if(manufOrder.getOperationOrderList() != null)  {
			
			for(OperationOrder operationOrder : manufOrder.getOperationOrderList())  {
				
				if(operationOrder.getStatusSelect() == IOperationOrder.STATUS_IN_PROGRESS)  {
					
					operationOrder.setStatusSelect(IOperationOrder.STATUS_STANDBY);
					
					operationOrder.setStoppedBy(AuthUtils.getUser());
					
					operationOrder.setStoppingDateTime(new LocalDateTime(generalService.getTodayDateTime()));
					
				}
				
			}
			
		}
		
		manufOrder.setStatusSelect(IManufOrder.STATUS_STANDBY);
		
		manufOrderRepo.save(manufOrder);
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void resume(ManufOrder manufOrder)  {
		
		if(manufOrder.getOperationOrderList() != null)  {
			
			for(OperationOrder operationOrder : manufOrder.getOperationOrderList())  {
				
				if(operationOrder.getStatusSelect() == IOperationOrder.STATUS_STANDBY)  {
					
					operationOrder.setStatusSelect(IOperationOrder.STATUS_IN_PROGRESS);
					
					operationOrder.setStartedBy(AuthUtils.getUser());
					
					operationOrder.setStartingDateTime(new LocalDateTime(generalService.getTodayDateTime()));
					
				}
				
			}
			
		}
		
		manufOrder.setStatusSelect(IManufOrder.STATUS_IN_PROGRESS);
		
		manufOrderRepo.save(manufOrder);
		
	}
	
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void finish(ManufOrder manufOrder) throws AxelorException  {
		
		if(manufOrder.getOperationOrderList() != null)  {
			
			for(OperationOrder operationOrder : manufOrder.getOperationOrderList())  {
				
				if(operationOrder.getStatusSelect() != IManufOrder.STATUS_FINISHED)  {
					
					if (operationOrder.getStatusSelect() != IManufOrder.STATUS_IN_PROGRESS && operationOrder.getStatusSelect() != IManufOrder.STATUS_STANDBY) {
						operationOrderWorkflowService.start(operationOrder);
					}
					
					operationOrderWorkflowService.finish(operationOrder);
				}
				
			}
			
		}
		
		manufOrderStockMoveService.finish(manufOrder);
		
		manufOrder.setStatusSelect(IManufOrder.STATUS_FINISHED);
		
		manufOrderRepo.save(manufOrder);
		
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
		
		manufOrderRepo.save(manufOrder);
		
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
		
		ManufOrderService mfService = Beans.get(ManufOrderService.class);
		
		manufOrder.setManufOrderSeq(mfService.getManufOrderSeq());
		
		return manufOrderRepo.save(manufOrder);
	}
	
	
	public LocalDateTime computePlannedEndDateT(ManufOrder manufOrder)  {
		
		OperationOrder lastOperationOrder = operationOrderRepo.all().filter("self.manufOrder = ?1 ORDER BY self.plannedEndDateT DESC", manufOrder).fetchOne();
		
		if(lastOperationOrder != null)  {
			
			return lastOperationOrder.getPlannedEndDateT();
			
		}
		
		return manufOrder.getPlannedStartDateT();
		
	}
	
	@Transactional
	public void allOpFinished(ManufOrder manufOrder) throws AxelorException  {
		int count = 0;
		List<OperationOrder> operationOrderList = manufOrder.getOperationOrderList();
		for (OperationOrder operationOrderIt : operationOrderList) {
			if(operationOrderIt.getStatusSelect() == IOperationOrder.STATUS_FINISHED){
				count++;
			}
		}
		if(count == operationOrderList.size()){
			Beans.get(ManufOrderStockMoveService.class).finish(manufOrder);
			
			manufOrder.setStatusSelect(IManufOrder.STATUS_FINISHED);
			
			manufOrderRepo.save(manufOrder);
		}
	}
}
