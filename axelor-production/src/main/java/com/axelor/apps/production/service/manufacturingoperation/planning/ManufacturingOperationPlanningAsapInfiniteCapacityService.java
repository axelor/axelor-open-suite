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
package com.axelor.apps.production.service.manufacturingoperation.planning;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.ManufacturingOperation;
import com.axelor.apps.production.db.repo.ManufacturingOperationRepository;
import com.axelor.apps.production.service.manufacturingoperation.ManufacturingOperationService;
import com.axelor.apps.production.service.manufacturingoperation.ManufacturingOperationStockMoveService;
import com.axelor.db.JPA;
import com.axelor.utils.helpers.date.DurationHelper;
import com.axelor.utils.helpers.date.LocalDateTimeHelper;
import com.google.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ManufacturingOperationPlanningAsapInfiniteCapacityService
    extends ManufacturingOperationPlanningCommonService {

  protected ManufacturingOperationPlanningInfiniteCapacityService
      manufacturingOperationPlanningInfiniteCapacityService;
  protected WeeklyPlanningService weeklyPlanningService;

  @Inject
  protected ManufacturingOperationPlanningAsapInfiniteCapacityService(
      ManufacturingOperationService manufacturingOperationService,
      ManufacturingOperationStockMoveService manufacturingOperationStockMoveService,
      ManufacturingOperationRepository manufacturingOperationRepository,
      AppBaseService appBaseService,
      ManufacturingOperationPlanningInfiniteCapacityService
          manufacturingOperationPlanningInfiniteCapacityService,
      WeeklyPlanningService weeklyPlanningService) {
    super(
        manufacturingOperationService,
        manufacturingOperationStockMoveService,
        manufacturingOperationRepository,
        appBaseService);
    this.manufacturingOperationPlanningInfiniteCapacityService =
        manufacturingOperationPlanningInfiniteCapacityService;
    this.weeklyPlanningService = weeklyPlanningService;
  }

  @Override
  protected void planWithStrategy(ManufacturingOperation manufacturingOperation)
      throws AxelorException {

    Machine machine = manufacturingOperation.getMachine();
    LocalDateTime plannedStartDate = manufacturingOperation.getPlannedStartDateT();

    LocalDateTime lastOperationDate =
        manufacturingOperationService.getLastOperationDate(manufacturingOperation);
    LocalDateTime maxDate = LocalDateTimeHelper.max(plannedStartDate, lastOperationDate);
    manufacturingOperation.setPlannedStartDateT(maxDate);

    WeeklyPlanning weeklyPlanning = null;
    if (machine != null) {
      weeklyPlanning = machine.getWeeklyPlanning();
    }
    if (weeklyPlanning != null) {
      this.planWithPlanning(manufacturingOperation, weeklyPlanning);
    }

    manufacturingOperation.setPlannedEndDateT(
        manufacturingOperationPlanningInfiniteCapacityService.computePlannedEndDateT(
            manufacturingOperation));

    Long plannedDuration =
        DurationHelper.getSecondsDuration(
            Duration.between(
                manufacturingOperation.getPlannedStartDateT(),
                manufacturingOperation.getPlannedEndDateT()));

    manufacturingOperation.setPlannedDuration(plannedDuration);
    if (weeklyPlanning != null) {
      manageDurationWithMachinePlanning(manufacturingOperation, weeklyPlanning);
    }
  }

  /**
   * Set the planned start date of the operation order according to the planning of the machine
   *
   * @param weeklyPlanning
   * @param manufacturingOperation
   */
  protected void planWithPlanning(
      ManufacturingOperation manufacturingOperation, WeeklyPlanning weeklyPlanning) {

    LocalDateTime startDate = manufacturingOperation.getPlannedStartDateT();
    DayPlanning dayPlanning =
        weeklyPlanningService.findDayPlanning(weeklyPlanning, startDate.toLocalDate());

    if (dayPlanning != null) {

      LocalTime firstPeriodFrom = dayPlanning.getMorningFrom();
      LocalTime firstPeriodTo = dayPlanning.getMorningTo();
      LocalTime secondPeriodFrom = dayPlanning.getAfternoonFrom();
      LocalTime secondPeriodTo = dayPlanning.getAfternoonTo();
      LocalTime startDateTime = startDate.toLocalTime();

      /*
       * If the start date is before the start time of the machine (or equal, then the operation
       * order will begin at the same time as the machine Example: Machine begins at 8am. We set
       * the date to 6am. Then the planned start date will be set to 8am.
       */
      if (firstPeriodFrom != null
          && (startDateTime.isBefore(firstPeriodFrom) || startDateTime.equals(firstPeriodFrom))) {
        manufacturingOperation.setPlannedStartDateT(
            startDate.toLocalDate().atTime(firstPeriodFrom));
      }
      /*
       * If the machine has two periods, with a break between them, and the operation is planned
       * inside this period of time, then we will start the operation at the beginning of the
       * machine second period. Example: Machine hours is 8am to 12 am. 2pm to 6pm. We try to begin
       * at 1pm. The operation planned start date will be set to 2pm.
       */
      else if (firstPeriodTo != null
          && secondPeriodFrom != null
          && (startDateTime.isAfter(firstPeriodTo) || startDateTime.equals(firstPeriodTo))
          && (startDateTime.isBefore(secondPeriodFrom) || startDateTime.equals(secondPeriodFrom))) {
        manufacturingOperation.setPlannedStartDateT(
            startDate.toLocalDate().atTime(secondPeriodFrom));
      }
      /*
       * If the start date is planned after working hours, or during a day off, then we will search
       * for the first period of the machine available. Example: Machine on Friday is 6am to 8 pm.
       * We set the date to 9pm. The next working day is Monday 8am. Then the planned start date
       * will be set to Monday 8am.
       */
      else if ((firstPeriodTo != null
              && secondPeriodFrom == null
              && (startDateTime.isAfter(firstPeriodTo) || startDateTime.equals(firstPeriodTo)))
          || (secondPeriodTo != null
              && (startDateTime.isAfter(secondPeriodTo) || startDateTime.equals(secondPeriodTo)))
          || (firstPeriodFrom == null && secondPeriodFrom == null)) {
        manufacturingOperationPlanningInfiniteCapacityService.searchForNextWorkingDay(
            manufacturingOperation, weeklyPlanning, startDate);
      }
    }
  }

  protected void manageDurationWithMachinePlanning(
      ManufacturingOperation manufacturingOperation, WeeklyPlanning weeklyPlanning)
      throws AxelorException {
    LocalDateTime startDate = manufacturingOperation.getPlannedStartDateT();
    LocalDateTime endDate = manufacturingOperation.getPlannedEndDateT();
    DayPlanning dayPlanning =
        weeklyPlanningService.findDayPlanning(weeklyPlanning, startDate.toLocalDate());
    if (dayPlanning != null) {
      LocalTime firstPeriodTo = dayPlanning.getMorningTo();
      LocalTime secondPeriodFrom = dayPlanning.getAfternoonFrom();
      LocalTime startDateTime = startDate.toLocalTime();
      LocalTime endDateTime = endDate.toLocalTime();

      /*
       * If operation begins inside one period of the machine but finished after that period, then
       * we split the operation
       */
      if (firstPeriodTo != null
          && startDateTime.isBefore(firstPeriodTo)
          && endDateTime.isAfter(firstPeriodTo)) {
        LocalDateTime plannedEndDate = startDate.toLocalDate().atTime(firstPeriodTo);
        Long plannedDuration =
            DurationHelper.getSecondsDuration(Duration.between(startDate, plannedEndDate));
        manufacturingOperation.setPlannedDuration(plannedDuration);
        manufacturingOperation.setPlannedEndDateT(plannedEndDate);
        manufacturingOperationRepository.save(manufacturingOperation);
        ManufacturingOperation otherManufacturingOperation = JPA.copy(manufacturingOperation, true);
        otherManufacturingOperation.setPlannedStartDateT(plannedEndDate);
        if (secondPeriodFrom != null) {
          otherManufacturingOperation.setPlannedStartDateT(
              startDate.toLocalDate().atTime(secondPeriodFrom));
        } else {
          manufacturingOperationPlanningInfiniteCapacityService.searchForNextWorkingDay(
              otherManufacturingOperation, weeklyPlanning, plannedEndDate);
        }
        manufacturingOperationRepository.save(otherManufacturingOperation);
        this.plan(otherManufacturingOperation);
      }
    }
  }
}
