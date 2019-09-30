/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.service.timesheet.TimesheetLineService;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.service.OperationOrderWorkflowService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.tool.date.DurationTool;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class OperationOrderTimesheetServiceImpl implements OperationOrderTimesheetService {

  @Override
  @Transactional
  public void updateOperationOrder(
      OperationOrder operationOrder,
      List<TimesheetLine> oldTimesheetLineList,
      List<TimesheetLine> newTimesheetLineList) {
    List<TimesheetLine> operationOrderTsLineList =
        new ArrayList<>(operationOrder.getTimesheetLineList());

    operationOrderTsLineList.removeAll(oldTimesheetLineList);
    operationOrderTsLineList.addAll(
        newTimesheetLineList
            .stream()
            .filter(timesheetLine -> operationOrder.equals(timesheetLine.getOperationOrder()))
            .collect(Collectors.toList()));
    long durationLong =
        DurationTool.getSecondsDuration(
            Beans.get(TimesheetLineService.class).computeTotalDuration(operationOrderTsLineList));
    operationOrder.setRealDuration(durationLong);
  }

  @Override
  @Transactional
  public void updateOperationOrders(Timesheet timesheet) throws AxelorException {
    if (timesheet.getTimesheetLineList() == null) {
      return;
    }

    // ensure that correct hoursDuration is filled
    TimesheetLineService timesheetLineService = Beans.get(TimesheetLineService.class);
    for (TimesheetLine timesheetLine : timesheet.getTimesheetLineList()) {
      BigDecimal hoursDuration =
          timesheetLineService.computeHoursDuration(timesheet, timesheetLine.getDuration(), true);
      timesheetLine.setHoursDuration(hoursDuration);
    }

    if (!Beans.get(AppProductionService.class)
        .getAppProduction()
        .getEnableTimesheetOnManufOrder()) {
      return;
    }
    List<TimesheetLine> oldTimesheetLineList =
        Beans.get(TimesheetLineRepository.class)
            .all()
            .filter("self.timesheet.id = :timesheetId")
            .bind("timesheetId", timesheet.getId())
            .fetch();
    List<TimesheetLine> newTimesheetLineList = timesheet.getTimesheetLineList();

    List<TimesheetLine> allTimesheetLineList = new ArrayList<>(oldTimesheetLineList);
    allTimesheetLineList.addAll(newTimesheetLineList);

    List<OperationOrder> operationOrdersToUpdate =
        allTimesheetLineList
            .stream()
            .map(TimesheetLine::getOperationOrder)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());

    operationOrdersToUpdate.forEach(
        operationOrder ->
            updateOperationOrder(operationOrder, oldTimesheetLineList, newTimesheetLineList));
  }

  @Override
  public void updateAllRealDuration(List<TimesheetLine> timesheetLineList) {
    if (timesheetLineList == null) {
      return;
    }
    List<OperationOrder> operationOrderList =
        timesheetLineList
            .stream()
            .map(TimesheetLine::getOperationOrder)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
    OperationOrderWorkflowService operationOrderWorkflowService =
        Beans.get(OperationOrderWorkflowService.class);
    operationOrderList.forEach(operationOrderWorkflowService::updateRealDuration);
  }
}
