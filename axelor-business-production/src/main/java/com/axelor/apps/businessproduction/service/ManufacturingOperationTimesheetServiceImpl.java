/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.businessproduction.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetLineRepository;
import com.axelor.apps.hr.service.timesheet.TimesheetLineService;
import com.axelor.apps.production.db.ManufacturingOperation;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.manufacturingoperation.ManufacturingOperationPlanningService;
import com.axelor.inject.Beans;
import com.axelor.utils.helpers.date.DurationHelper;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ManufacturingOperationTimesheetServiceImpl
    implements ManufacturingOperationTimesheetService {

  @Override
  @Transactional
  public void updateManufacturingOperation(
      ManufacturingOperation manufacturingOperation,
      List<TimesheetLine> oldTimesheetLineList,
      List<TimesheetLine> newTimesheetLineList) {
    List<TimesheetLine> manufacturingOperationTsLineList =
        new ArrayList<>(manufacturingOperation.getTimesheetLineList());

    manufacturingOperationTsLineList.removeAll(oldTimesheetLineList);
    manufacturingOperationTsLineList.addAll(
        newTimesheetLineList.stream()
            .filter(
                timesheetLine ->
                    manufacturingOperation.equals(timesheetLine.getManufacturingOperation()))
            .collect(Collectors.toList()));
    long durationLong =
        DurationHelper.getSecondsDuration(
            Beans.get(TimesheetLineService.class)
                .computeTotalDuration(manufacturingOperationTsLineList));
    manufacturingOperation.setRealDuration(durationLong);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateManufacturingOperations(Timesheet timesheet) throws AxelorException {
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

    List<ManufacturingOperation> manufacturingOperationsToUpdate =
        allTimesheetLineList.stream()
            .map(TimesheetLine::getManufacturingOperation)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());

    manufacturingOperationsToUpdate.forEach(
        manufacturingOperation ->
            updateManufacturingOperation(
                manufacturingOperation, oldTimesheetLineList, newTimesheetLineList));
  }

  @Override
  public void updateAllRealDuration(List<TimesheetLine> timesheetLineList) {
    if (timesheetLineList == null) {
      return;
    }
    List<ManufacturingOperation> manufacturingOperationList =
        timesheetLineList.stream()
            .map(TimesheetLine::getManufacturingOperation)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
    ManufacturingOperationPlanningService manufacturingOperationPlanningService =
        Beans.get(ManufacturingOperationPlanningService.class);
    manufacturingOperationList.forEach(manufacturingOperationPlanningService::updateRealDuration);
  }
}
