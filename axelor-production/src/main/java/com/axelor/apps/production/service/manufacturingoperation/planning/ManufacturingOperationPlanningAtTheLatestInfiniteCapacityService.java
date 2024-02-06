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

public class ManufacturingOperationPlanningAtTheLatestInfiniteCapacityService
    extends ManufacturingOperationPlanningCommonService {

  protected ManufacturingOperationPlanningInfiniteCapacityService
      manufacturingOperationPlanningInfiniteCapacityService;
  protected WeeklyPlanningService weeklyPlanningService;

  @Inject
  protected ManufacturingOperationPlanningAtTheLatestInfiniteCapacityService(
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
    WeeklyPlanning weeklyPlanning = null;
    LocalDateTime plannedEndDate = manufacturingOperation.getPlannedEndDateT();

    LocalDateTime nextOperationDate =
        manufacturingOperationService.getNextOperationDate(manufacturingOperation);
    LocalDateTime minDate = LocalDateTimeHelper.min(plannedEndDate, nextOperationDate);
    manufacturingOperation.setPlannedEndDateT(minDate);

    if (machine != null) {
      weeklyPlanning = machine.getWeeklyPlanning();
    }
    if (weeklyPlanning != null) {
      this.planWithPlanning(manufacturingOperation, weeklyPlanning);
    }
    manufacturingOperation.setPlannedStartDateT(
        this.computePlannedStartDateT(manufacturingOperation));

    checkIfPlannedStartDateTimeIsBeforeCurrentDateTime(manufacturingOperation);

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

  public LocalDateTime computePlannedStartDateT(ManufacturingOperation manufacturingOperation)
      throws AxelorException {

    if (manufacturingOperation.getWorkCenter() != null) {
      return manufacturingOperation
          .getPlannedEndDateT()
          .minusSeconds(
              (int)
                  manufacturingOperationService.computeEntireCycleDuration(
                      manufacturingOperation, manufacturingOperation.getManufOrder().getQty()));
    }

    return manufacturingOperation.getPlannedEndDateT();
  }

  /**
   * Set the planned end date of the operation order according to the planning of the machine
   *
   * @param weeklyPlanning
   * @param manufacturingOperation
   */
  protected void planWithPlanning(
      ManufacturingOperation manufacturingOperation, WeeklyPlanning weeklyPlanning) {

    LocalDateTime endDate = manufacturingOperation.getPlannedEndDateT();
    DayPlanning dayPlanning =
        weeklyPlanningService.findDayPlanning(weeklyPlanning, endDate.toLocalDate());

    if (dayPlanning != null) {
      LocalTime firstPeriodFrom = dayPlanning.getMorningFrom();
      LocalTime firstPeriodTo = dayPlanning.getMorningTo();
      LocalTime secondPeriodTo = dayPlanning.getAfternoonTo();
      LocalTime endDateTime = endDate.toLocalTime();

      /*
       * If the end date is after the end time of the second period (or equal, then the operation
       * order will end at the same time as the machine Example: Machine ends at 8pm. We set the
       * date to 9pm. Then the planned end date will be set to 8pm).
       */
      if (secondPeriodTo != null
          && (endDateTime.isAfter(secondPeriodTo) || endDateTime.equals(secondPeriodTo))) {
        manufacturingOperation.setPlannedEndDateT(endDate.toLocalDate().atTime(secondPeriodTo));
      }
      /*
       * If the end date is after the end time of the first period (or equal, then the operation
       * order will end at the same time as the machine Example: Machine ends at 1pm. We set the
       * date to 2pm. Then the planned end date will be set to 1pm).
       */
      else if (firstPeriodTo != null
          && (endDateTime.isAfter(firstPeriodTo) || endDateTime.equals(firstPeriodTo))) {
        manufacturingOperation.setPlannedEndDateT(endDate.toLocalDate().atTime(firstPeriodTo));
      }
      /*
       * If the end date is planned before working hours, or during a day off, then we will search
       * for the first period of the machine available on the days previous to the current end
       * date. Example: Machine on Friday is 6am to 8 pm. We set the date to 5am. The previous
       * working day is Thursday, and it ends at 8pm. Then the planned end date will be set to
       * Thursday 8pm.
       */
      else if (firstPeriodFrom != null
          && (endDateTime.isBefore(firstPeriodFrom) || endDateTime.equals(firstPeriodFrom))) {
        manufacturingOperationPlanningInfiniteCapacityService.searchForPreviousWorkingDay(
            manufacturingOperation, weeklyPlanning, endDate);
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
       * If operation ends inside one period of the machine but starts before that period, then
       * we split the operation
       */
      if (secondPeriodFrom != null
          && startDateTime.isBefore(secondPeriodFrom)
          && endDateTime.isAfter(secondPeriodFrom)) {
        LocalDateTime plannedStartDate = startDate.toLocalDate().atTime(secondPeriodFrom);
        Long plannedDuration =
            DurationHelper.getSecondsDuration(Duration.between(plannedStartDate, endDateTime));
        manufacturingOperation.setPlannedDuration(plannedDuration);
        manufacturingOperation.setPlannedStartDateT(plannedStartDate);
        manufacturingOperationRepository.save(manufacturingOperation);
        ManufacturingOperation otherManufacturingOperation = JPA.copy(manufacturingOperation, true);
        otherManufacturingOperation.setPlannedEndDateT(plannedStartDate);
        if (firstPeriodTo != null) {
          otherManufacturingOperation.setPlannedEndDateT(
              startDate.toLocalDate().atTime(firstPeriodTo));
        } else {
          manufacturingOperationPlanningInfiniteCapacityService.searchForPreviousWorkingDay(
              otherManufacturingOperation, weeklyPlanning, plannedStartDate);
        }
        manufacturingOperationRepository.save(otherManufacturingOperation);
        this.plan(otherManufacturingOperation);
      }
    }
  }
}
