/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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

import com.axelor.app.production.db.IManufOrder;
import com.axelor.app.production.db.IOperationOrder;
import com.axelor.apps.production.db.CostSheet;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public class ManufOrderWorkflowService {
	protected OperationOrderWorkflowService operationOrderWorkflowService;
	protected OperationOrderRepository operationOrderRepo;
	protected ManufOrderStockMoveService manufOrderStockMoveService;
	protected ManufOrderRepository manufOrderRepo;

	protected LocalDateTime now;

	@Inject
	public ManufOrderWorkflowService(OperationOrderWorkflowService operationOrderWorkflowService, OperationOrderRepository operationOrderRepo,
									 ManufOrderStockMoveService manufOrderStockMoveService, ManufOrderRepository manufOrderRepo,
									 AppProductionService appProductionService) {
		this.operationOrderWorkflowService = operationOrderWorkflowService;
		this.operationOrderRepo = operationOrderRepo;
		this.manufOrderStockMoveService = manufOrderStockMoveService;
		this.manufOrderRepo = manufOrderRepo;

		now = appProductionService.getTodayDateTime().toLocalDateTime();
	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public ManufOrder plan(ManufOrder manufOrder) throws AxelorException {
		if (manufOrder.getOperationOrderList() != null) {
			for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
				operationOrderWorkflowService.plan(operationOrder);
			}
		}

		manufOrder.setPlannedEndDateT(this.computePlannedEndDateT(manufOrder));

		if (!manufOrder.getIsConsProOnOperation()) {
			manufOrderStockMoveService.createToConsumeStockMove(manufOrder);
		}

		manufOrderStockMoveService.createToProduceStockMove(manufOrder);
		manufOrder.setStatusSelect(IManufOrder.STATUS_PLANNED);
		manufOrder.setManufOrderSeq(Beans.get(ManufOrderService.class).getManufOrderSeq());

		return manufOrderRepo.save(manufOrder);
	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void start(ManufOrder manufOrder) {
		if (manufOrder.getOperationOrderList() != null) {
			OperationOrder operationOrderPriority = operationOrderRepo.all().filter("self.manufOrder = ? AND self.statusSelect >= ? AND self.statusSelect <= ?", manufOrder, IOperationOrder.STATUS_PLANNED, IOperationOrder.STATUS_STANDBY).order("priority").fetchOne();

			List<OperationOrder> operationOrderList = operationOrderRepo.all().filter("self.manufOrder = ? AND self.priority = ? AND self.statusSelect <> ?", manufOrder, operationOrderPriority.getPriority(), IOperationOrder.STATUS_FINISHED).fetch();
			for (OperationOrder operationOrder : operationOrderList) {
				operationOrderWorkflowService.start(operationOrder);
			}
		}

		manufOrder.setRealStartDateT(Beans.get(AppProductionService.class).getTodayDateTime().toLocalDateTime());
		
		manufOrder.setStatusSelect(IManufOrder.STATUS_IN_PROGRESS);
		manufOrderRepo.save(manufOrder);
	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void pause(ManufOrder manufOrder) {
		if (manufOrder.getOperationOrderList() != null) {
			for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
				if (operationOrder.getStatusSelect() == IOperationOrder.STATUS_IN_PROGRESS) {
					operationOrderWorkflowService.pause(operationOrder);
				}
			}
		}

		manufOrder.setStatusSelect(IManufOrder.STATUS_STANDBY);
		manufOrderRepo.save(manufOrder);
	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void resume(ManufOrder manufOrder) {
		if (manufOrder.getOperationOrderList() != null) {
			for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
				if (operationOrder.getStatusSelect() == IOperationOrder.STATUS_STANDBY) {
					operationOrderWorkflowService.resume(operationOrder);
				}
			}
		}

		manufOrder.setStatusSelect(IManufOrder.STATUS_IN_PROGRESS);
		manufOrderRepo.save(manufOrder);
	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void finish(ManufOrder manufOrder) throws AxelorException {
		if (manufOrder.getOperationOrderList() != null) {
			for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
				if (operationOrder.getStatusSelect() != IOperationOrder.STATUS_FINISHED) {
					if (operationOrder.getStatusSelect() != IOperationOrder.STATUS_IN_PROGRESS && operationOrder.getStatusSelect() != IOperationOrder.STATUS_STANDBY) {
						operationOrderWorkflowService.start(operationOrder);
					}

					operationOrderWorkflowService.finish(operationOrder);
				}
			}
		}

		manufOrderStockMoveService.finish(manufOrder);
		//create cost sheet
		CostSheet costSheet = Beans.get(CostSheetService.class).computeCostPrice(manufOrder);

		//update price in product
		manufOrder.getProduct().setLastProductionPrice(costSheet.getCostPrice());

		manufOrder.setRealEndDateT(Beans.get(AppProductionService.class).getTodayDateTime().toLocalDateTime());
		manufOrder.setStatusSelect(IManufOrder.STATUS_FINISHED);
		manufOrderRepo.save(manufOrder);
	}

	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void cancel(ManufOrder manufOrder) throws AxelorException {
		if (manufOrder.getOperationOrderList() != null) {
			for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
				if (operationOrder.getStatusSelect() != IOperationOrder.STATUS_CANCELED) {
					operationOrderWorkflowService.cancel(operationOrder);
				}
			}
		}

		manufOrderStockMoveService.cancel(manufOrder);
		manufOrder.setStatusSelect(IManufOrder.STATUS_CANCELED);
		manufOrderRepo.save(manufOrder);
	}
	
	
	public LocalDateTime computePlannedEndDateT(ManufOrder manufOrder)  {
		
		OperationOrder lastOperationOrder = getLastOperationOrder(manufOrder);
		
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
			this.finish(manufOrder);
		}
	}

	/**
	 * Returns last operation order (highest priority) of given {@link ManufOrder}
	 *
	 * @param manufOrder A manufacturing order
	 * @return Last operation order of {@code manufOrder}
	 */
	public OperationOrder getLastOperationOrder(ManufOrder manufOrder) {
		return operationOrderRepo.all().filter("self.manufOrder = ?", manufOrder).order("-plannedEndDateT").fetchOne();
	}
}
