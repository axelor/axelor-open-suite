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

import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.production.db.IOperationOrder;
import com.axelor.app.production.db.IProdResource;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdHumanResource;
import com.axelor.apps.production.db.ProdResource;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class OperationOrderWorkflowService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject
	private OperationOrderStockMoveService operationOrderStockMoveService;
	
	private LocalDateTime today;
	
	@Inject
	public OperationOrderWorkflowService() {
		
		today = GeneralService.getTodayDateTime().toLocalDateTime();
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public OperationOrder plan(OperationOrder operationOrder) throws AxelorException  {
		
		operationOrder.setPlannedStartDateT(this.getLastOperationOrder(operationOrder));
		
		operationOrder.setPlannedEndDateT(this.computePlannedEndDateT(operationOrder));
		
		operationOrder.setPlannedDuration(
				this.getDuration(
				this.computeDuration(operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT())));
		
		operationOrderStockMoveService.createToConsumeStockMove(operationOrder);
		
		operationOrder.setStatusSelect(IOperationOrder.STATUS_PLANNED);
		
		return operationOrder.save();
		
	}
	
	
	public LocalDateTime getLastOperationOrder(OperationOrder operationOrder)  {
		
		OperationOrder lastOperationOrder = OperationOrder.filter("self.manufOrder = ?1 AND self.priority <= ?2 AND self.statusSelect >= 3 AND self.statusSelect < 6", 
				operationOrder.getManufOrder(), operationOrder.getPriority()).order("-self.priority").order("-self.plannedEndDateT").fetchOne();
		
		if(lastOperationOrder != null)  {
			
			if(lastOperationOrder.getPriority() == operationOrder.getPriority())  {
				if(lastOperationOrder.getPlannedStartDateT() != null && lastOperationOrder.getPlannedStartDateT().isAfter(today))  {
					return lastOperationOrder.getPlannedStartDateT();
				}
				else  {
					return today;
				}
			}
			else  {
				if(lastOperationOrder.getPlannedEndDateT() != null && lastOperationOrder.getPlannedEndDateT().isAfter(today))  {
					return lastOperationOrder.getPlannedEndDateT();
				}
				else  {
					return today;
				}
			}
		}
		
		return today;
		
	}
	

	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void start(OperationOrder operationOrder)  {
		
		operationOrder.setStatusSelect(IOperationOrder.STATUS_IN_PROGRESS);
		
		operationOrder.setRealStartDateT(today);
		
		operationOrder.save();
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void cancel(OperationOrder operationOrder) throws AxelorException  {

		operationOrderStockMoveService.cancel(operationOrder);
		
		operationOrder.setStatusSelect(IOperationOrder.STATUS_CANCELED);
		
		operationOrder.save();
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void finish(OperationOrder operationOrder) throws AxelorException  {
		
		operationOrderStockMoveService.finish(operationOrder);
		
		operationOrder.setRealEndDateT(today);
			
		operationOrder.setStatusSelect(IOperationOrder.STATUS_FINISHED);
			
		operationOrder.save();
		
	}
	
	
	public Duration computeDuration(LocalDateTime startDateTime, LocalDateTime endDateTime)  {
		
		return new Interval(startDateTime.toDateTime(), endDateTime.toDateTime()).toDuration();
		
	}
	
	public long getDuration(Duration duration)  {
		
		return duration.toStandardSeconds().getSeconds();
		
	}
	
	
	public LocalDateTime computePlannedEndDateT(OperationOrder operationOrder)  {
		
		if(operationOrder.getProdResource() != null)  {
			return operationOrder.getPlannedStartDateT()
					.plusSeconds((int)this.computeEntireCycleDuration(operationOrder.getProdResource(), operationOrder.getManufOrder().getQty()));
		}
		
		return operationOrder.getPlannedStartDateT();
	}
	

	public long computeEntireCycleDuration(ProdResource prodResource, BigDecimal qty)  {
		
		long machineDuration = this.computeMachineDuration(prodResource, qty);
		
		long humanDuration = this.computeHumanDuration(prodResource, qty);
		
		if(machineDuration >= humanDuration)  {
			return machineDuration;
		}
		else  {
			return humanDuration;
		}
		
	}
	
	
	public long computeMachineDuration(ProdResource prodResource, BigDecimal qty)  {
		
		long duration = 0;
		
		int resourceType = prodResource.getResourceTypeSelect();
		
		if(resourceType == IProdResource.RESOURCE_MACHINE || resourceType == IProdResource.RESOURCE_BOTH)  {
			
			duration += prodResource.getStartingDuration();
			
			BigDecimal durationPerCycle = new BigDecimal(prodResource.getDurationPerCycle());
			
			duration += (qty.divide(prodResource.getCapacityPerCycle())).multiply(durationPerCycle).longValue();
			
			duration += prodResource.getEndingDuration();
			
		}
		
		return duration;
	}
	
	
	public long computeHumanDuration(ProdResource prodResource, BigDecimal qty)  {
		
		long duration = 0;
		
		int resourceType = prodResource.getResourceTypeSelect();
		
		if(resourceType == IProdResource.RESOURCE_HUMAN || resourceType == IProdResource.RESOURCE_BOTH)  {
			
			if(prodResource.getProdHumanResourceList() != null)  {
				
				for(ProdHumanResource prodHumanResource : prodResource.getProdHumanResourceList())  {
					
					duration += prodHumanResource.getDuration();
					
				}
				
			}
			
		}
		
		return qty.multiply(new BigDecimal(duration)).longValue();
	}
	
	
	
	
	
	
}

