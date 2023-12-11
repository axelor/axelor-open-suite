/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service.operationorder.planning;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.service.operationorder.OperationOrderService;
import com.axelor.apps.production.service.operationorder.OperationOrderStockMoveService;
import com.axelor.db.JPA;
import com.axelor.utils.helpers.date.DurationHelper;
import com.axelor.utils.helpers.date.LocalDateTimeHelper;
import com.google.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class OperationOrderPlanningAsapInfiniteCapacityService
    extends OperationOrderPlanningCommonService {

  protected OperationOrderPlanningInfiniteCapacityService
      operationOrderPlanningInfiniteCapacityService;
  protected WeeklyPlanningService weeklyPlanningService;

  @Inject
  protected OperationOrderPlanningAsapInfiniteCapacityService(
      OperationOrderService operationOrderService,
      OperationOrderStockMoveService operationOrderStockMoveService,
      OperationOrderRepository operationOrderRepository,
      AppBaseService appBaseService,
      OperationOrderPlanningInfiniteCapacityService operationOrderPlanningInfiniteCapacityService,
      WeeklyPlanningService weeklyPlanningService) {
    super(
        operationOrderService,
        operationOrderStockMoveService,
        operationOrderRepository,
        appBaseService);
    this.operationOrderPlanningInfiniteCapacityService =
        operationOrderPlanningInfiniteCapacityService;
    this.weeklyPlanningService = weeklyPlanningService;
  }

  @Override
  protected void planWithStrategy(OperationOrder operationOrder) throws AxelorException {

    Machine machine = operationOrder.getMachine();
    LocalDateTime plannedStartDate = operationOrder.getPlannedStartDateT();

    LocalDateTime lastOperationDate = operationOrderService.getLastOperationDate(operationOrder);
    LocalDateTime maxDate = LocalDateTimeHelper.max(plannedStartDate, lastOperationDate);
    operationOrder.setPlannedStartDateT(maxDate);

    WeeklyPlanning weeklyPlanning = null;
    if (machine != null) {
      weeklyPlanning = machine.getWeeklyPlanning();
    }
    if (weeklyPlanning != null) {
      this.planWithPlanning(operationOrder, weeklyPlanning);
    }

    operationOrder.setPlannedEndDateT(
        operationOrderPlanningInfiniteCapacityService.computePlannedEndDateT(operationOrder));

    Long plannedDuration =
        DurationHelper.getSecondsDuration(
            Duration.between(
                operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT()));

    operationOrder.setPlannedDuration(plannedDuration);
    if (weeklyPlanning != null) {
      manageDurationWithMachinePlanning(operationOrder, weeklyPlanning);
    }
  }

  /**
   * Set the planned start date of the operation order according to the planning of the machine
   *
   * @param weeklyPlanning
   * @param operationOrder
   */
  protected void planWithPlanning(OperationOrder operationOrder, WeeklyPlanning weeklyPlanning) {

    LocalDateTime startDate = operationOrder.getPlannedStartDateT();
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
        operationOrder.setPlannedStartDateT(startDate.toLocalDate().atTime(firstPeriodFrom));
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
        operationOrder.setPlannedStartDateT(startDate.toLocalDate().atTime(secondPeriodFrom));
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
        operationOrderPlanningInfiniteCapacityService.searchForNextWorkingDay(
            operationOrder, weeklyPlanning, startDate);
      }
    }
  }

  protected void manageDurationWithMachinePlanning(
      OperationOrder operationOrder, WeeklyPlanning weeklyPlanning) throws AxelorException {
    LocalDateTime startDate = operationOrder.getPlannedStartDateT();
    LocalDateTime endDate = operationOrder.getPlannedEndDateT();
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
        operationOrder.setPlannedDuration(plannedDuration);
        operationOrder.setPlannedEndDateT(plannedEndDate);
        operationOrderRepository.save(operationOrder);
        OperationOrder otherOperationOrder = JPA.copy(operationOrder, true);
        otherOperationOrder.setPlannedStartDateT(plannedEndDate);
        if (secondPeriodFrom != null) {
          otherOperationOrder.setPlannedStartDateT(
              startDate.toLocalDate().atTime(secondPeriodFrom));
        } else {
          operationOrderPlanningInfiniteCapacityService.searchForNextWorkingDay(
              otherOperationOrder, weeklyPlanning, plannedEndDate);
        }
        operationOrderRepository.save(otherOperationOrder);
        this.plan(otherOperationOrder);
      }
    }
  }
}
