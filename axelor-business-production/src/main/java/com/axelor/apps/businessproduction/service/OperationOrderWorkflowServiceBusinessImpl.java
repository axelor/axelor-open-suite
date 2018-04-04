/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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

import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.OperationOrderDuration;
import com.axelor.apps.production.db.repo.OperationOrderDurationRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.service.OperationOrderStockMoveService;
import com.axelor.apps.production.service.OperationOrderWorkflowService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.google.inject.Inject;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

public class OperationOrderWorkflowServiceBusinessImpl extends OperationOrderWorkflowService {

    @Inject
	public OperationOrderWorkflowServiceBusinessImpl(OperationOrderStockMoveService operationOrderStockMoveService, OperationOrderRepository operationOrderRepo,
                                         OperationOrderDurationRepository operationOrderDurationRepo, AppProductionService appProductionService) {
        super(operationOrderStockMoveService, operationOrderRepo, operationOrderDurationRepo, appProductionService);
	}

    /**
     * Computes the duration of all the {@link OperationOrderDuration} of {@code operationOrder}
     * If we manage timesheet with manuf order, we get the duration with the timesheet lines.
     *
     * @param operationOrder An operation order
     * @return Real duration of {@code operationOrder}
     */
    @Override
    public Duration computeRealDuration(OperationOrder operationOrder) {
        if(appProductionService.getAppProduction().getEnableTimesheetOnManufOrder()) {
            List<TimesheetLine> timesheetLineList = operationOrder.getTimesheetLineList();
            if (timesheetLineList == null || timesheetLineList.isEmpty()) {
                return Duration.ZERO;
            }
            long totalSecDuration = 0L;
            List<TimesheetLine> timesheetLineDurationList = timesheetLineList
                    .stream()
                    .filter(timesheetLine -> timesheetLine.getTimesheet() != null)
                    .filter(timesheetLine ->
                            timesheetLine.getTimesheet().getStatusSelect() == TimesheetRepository.STATUS_VALIDATED
                                    || timesheetLine.getTimesheet().getStatusSelect() == TimesheetRepository.STATUS_CONFIRMED)
                    .collect(Collectors.toList());
            for (TimesheetLine timesheetLine : timesheetLineDurationList) {
                totalSecDuration += timesheetLine.getHoursDuration().multiply(new BigDecimal("3600")).longValue();
            }
            return Duration.ofSeconds(totalSecDuration);
        } else {
            return super.computeRealDuration(operationOrder);
        }

    }
}
