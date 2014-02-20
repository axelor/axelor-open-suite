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

import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.production.db.IOperationOrder;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.production.db.OperationOrder;
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
		
		long entireCycleDuration = 0;
		
		//TODO a faire en fonction de human, machine...
		
		entireCycleDuration += prodResource.getStartingDuration();
		
		BigDecimal durationPerCycle = new BigDecimal(prodResource.getDurationPerCycle());
		
		entireCycleDuration += (qty.divide(prodResource.getCapacityPerCycle())).multiply(durationPerCycle).longValue();
		
		entireCycleDuration += prodResource.getEndingDuration();
		
		return entireCycleDuration;
	}
}

