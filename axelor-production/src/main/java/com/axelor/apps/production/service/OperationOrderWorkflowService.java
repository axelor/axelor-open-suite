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

import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;

import com.axelor.app.production.db.IOperationOrder;
import com.axelor.app.production.db.IWorkCenter;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdHumanResource;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.web.ManufOrderController;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class OperationOrderWorkflowService {

	@Inject
	private OperationOrderStockMoveService operationOrderStockMoveService;

	@Inject
	protected GeneralService generalService;
	
	@Inject
	protected OperationOrderRepository operationOrderRepo;

	private LocalDateTime today;
	
	@Inject
	public OperationOrderWorkflowService(GeneralService generalService) {
		this.generalService = generalService;
		today = this.generalService.getTodayDateTime().toLocalDateTime();

	}

	@Transactional
	public OperationOrder plan(OperationOrder operationOrder) throws AxelorException  {

		operationOrder.setPlannedStartDateT(this.getLastOperationOrder(operationOrder));

		operationOrder.setPlannedEndDateT(this.computePlannedEndDateT(operationOrder));

		operationOrder.setPlannedDuration(
				this.getDuration(
				this.computeDuration(operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT())));

		operationOrderStockMoveService.createToConsumeStockMove(operationOrder);

		operationOrder.setStatusSelect(IOperationOrder.STATUS_PLANNED);

		return Beans.get(OperationOrderRepository.class).save(operationOrder);

	}
	
	@Transactional
	public OperationOrder replan(OperationOrder operationOrder) throws AxelorException  {

		operationOrder.setPlannedStartDateT(this.getLastOperationOrder(operationOrder));

		operationOrder.setPlannedEndDateT(this.computePlannedEndDateT(operationOrder));

		operationOrder.setPlannedDuration(
				this.getDuration(
				this.computeDuration(operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT())));

		return Beans.get(OperationOrderRepository.class).save(operationOrder);

	}


	public LocalDateTime getLastOperationOrder(OperationOrder operationOrder)  {

		OperationOrder lastOperationOrder = operationOrderRepo.all().filter("self.manufOrder = ?1 AND self.priority <= ?2 AND self.statusSelect >= 3 AND self.statusSelect < 6 AND self.id != ?3",
				operationOrder.getManufOrder(), operationOrder.getPriority(), operationOrder.getId()).order("-self.priority").order("-self.plannedEndDateT").fetchOne();
		
		if(lastOperationOrder != null)  {
			if(lastOperationOrder.getPriority() == operationOrder.getPriority())  {
				if(lastOperationOrder.getPlannedStartDateT() != null && lastOperationOrder.getPlannedStartDateT().isAfter(operationOrder.getManufOrder().getPlannedStartDateT()))  {
					if(lastOperationOrder.getMachineWorkCenter().equals(operationOrder.getMachineWorkCenter())){
						return lastOperationOrder.getPlannedEndDateT();
					}
					return lastOperationOrder.getPlannedStartDateT();
				}
				else  {
					return operationOrder.getManufOrder().getPlannedStartDateT();
				}
			}
			else  {
				if(lastOperationOrder.getPlannedEndDateT() != null && lastOperationOrder.getPlannedEndDateT().isAfter(operationOrder.getManufOrder().getPlannedStartDateT()))  {
					return lastOperationOrder.getPlannedEndDateT();
				}
				else  {
					return operationOrder.getManufOrder().getPlannedStartDateT();
				}
			}
		}

		return operationOrder.getManufOrder().getPlannedStartDateT();
	}
	



	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void start(OperationOrder operationOrder)  {

		operationOrder.setStatusSelect(IOperationOrder.STATUS_IN_PROGRESS);

		operationOrder.setRealStartDateT(today);
		
		operationOrder.setStartedBy(AuthUtils.getUser());
		
		operationOrder.setStartingDateTime(new LocalDateTime(generalService.getTodayDateTime()));

		Beans.get(OperationOrderRepository.class).save(operationOrder);

	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void cancel(OperationOrder operationOrder) throws AxelorException  {

		operationOrderStockMoveService.cancel(operationOrder);

		operationOrder.setStatusSelect(IOperationOrder.STATUS_CANCELED);

		Beans.get(OperationOrderRepository.class).save(operationOrder);

	}

	@Transactional
	public OperationOrder finish(OperationOrder operationOrder) throws AxelorException  {

		operationOrderStockMoveService.finish(operationOrder);

		operationOrder.setRealEndDateT(today);

		operationOrder.setStatusSelect(IOperationOrder.STATUS_FINISHED);

		return Beans.get(OperationOrderRepository.class).save(operationOrder);

	}


	public Duration computeDuration(LocalDateTime startDateTime, LocalDateTime endDateTime)  {

		return new Interval(startDateTime.toDateTime(), endDateTime.toDateTime()).toDuration();

	}

	public long getDuration(Duration duration)  {

		return duration.toStandardSeconds().getSeconds();

	}


	public LocalDateTime computePlannedEndDateT(OperationOrder operationOrder)  {

		if(operationOrder.getWorkCenter() != null)  {
			return operationOrder.getPlannedStartDateT()
					.plusSeconds((int)this.computeEntireCycleDuration(operationOrder.getWorkCenter(), operationOrder.getManufOrder().getQty()));
		}

		return operationOrder.getPlannedStartDateT();
	}


	public long computeEntireCycleDuration(WorkCenter workCenter, BigDecimal qty)  {

		long machineDuration = this.computeMachineDuration(workCenter, qty);

		long humanDuration = this.computeHumanDuration(workCenter, qty);

		if(machineDuration >= humanDuration)  {
			return machineDuration;
		}
		else  {
			return humanDuration;
		}

	}


	public long computeMachineDuration(WorkCenter workCenter, BigDecimal qty)  {

		long duration = 0;

		int workCenterTypeSelect = workCenter.getWorkCenterTypeSelect();

		if(workCenterTypeSelect == IWorkCenter.WORK_CENTER_MACHINE || workCenterTypeSelect == IWorkCenter.WORK_CENTER_BOTH)  {
			Machine machine = workCenter.getMachine();
			duration += machine.getStartingDuration();

			BigDecimal durationPerCycle = new BigDecimal(workCenter.getDurationPerCycle());

			duration += (qty.divide(workCenter.getCapacityPerCycle())).multiply(durationPerCycle).longValue();

			duration += machine.getEndingDuration();

		}

		return duration;
	}


	public long computeHumanDuration(WorkCenter workCenter, BigDecimal qty)  {

		long duration = 0;

		int workCenterTypeSelect = workCenter.getWorkCenterTypeSelect();

		if(workCenterTypeSelect == IWorkCenter.WORK_CENTER_HUMAN || workCenterTypeSelect == IWorkCenter.WORK_CENTER_BOTH)  {

			if(workCenter.getProdHumanResourceList() != null)  {

				for(ProdHumanResource prodHumanResource : workCenter.getProdHumanResourceList())  {

					duration += prodHumanResource.getDuration();

				}

			}

		}

		return qty.multiply(new BigDecimal(duration)).longValue();
	}






}

