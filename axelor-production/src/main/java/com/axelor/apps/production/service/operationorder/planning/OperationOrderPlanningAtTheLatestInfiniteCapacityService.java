package com.axelor.apps.production.service.operationorder.planning;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.service.operationorder.OperationOrderService;
import com.axelor.apps.production.service.operationorder.OperationOrderStockMoveService;
import com.axelor.db.JPA;
import com.axelor.utils.date.DurationTool;
import com.axelor.utils.date.LocalDateTimeUtils;
import com.google.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class OperationOrderPlanningAtTheLatestInfiniteCapacityService
    extends OperationOrderPlanningCommonService {

  OperationOrderPlanningInfiniteCapacityService operationOrderPlanningInfiniteCapacityService;
  WeeklyPlanningService weeklyPlanningService;

  @Inject
  protected OperationOrderPlanningAtTheLatestInfiniteCapacityService(
      OperationOrderService operationOrderService,
      OperationOrderStockMoveService operationOrderStockMoveService,
      OperationOrderRepository operationOrderRepository,
      OperationOrderPlanningInfiniteCapacityService operationOrderPlanningInfiniteCapacityService,
      WeeklyPlanningService weeklyPlanningService) {
    super(operationOrderService, operationOrderStockMoveService, operationOrderRepository);
    this.operationOrderPlanningInfiniteCapacityService =
        operationOrderPlanningInfiniteCapacityService;
    this.weeklyPlanningService = weeklyPlanningService;
  }

  @Override
  protected void planWithStrategy(OperationOrder operationOrder) throws AxelorException {

    Machine machine = operationOrder.getMachine();
    WeeklyPlanning weeklyPlanning = null;
    LocalDateTime plannedEndDate = operationOrder.getPlannedEndDateT();

    LocalDateTime nextOperationDate = operationOrderService.getNextOperationDate(operationOrder);
    LocalDateTime minDate = LocalDateTimeUtils.min(plannedEndDate, nextOperationDate);
    operationOrder.setPlannedEndDateT(minDate);

    if (machine != null) {
      weeklyPlanning = machine.getWeeklyPlanning();
    }
    if (weeklyPlanning != null) {
      this.planWithPlanning(operationOrder, weeklyPlanning);
    }
    operationOrder.setPlannedStartDateT(this.computePlannedStartDateT(operationOrder));

    Long plannedDuration =
        DurationTool.getSecondsDuration(
            Duration.between(
                operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT()));

    operationOrder.setPlannedDuration(plannedDuration);
    if (weeklyPlanning != null) {
      manageDurationWithMachinePlanning(operationOrder, weeklyPlanning);
    }
  }

  public LocalDateTime computePlannedStartDateT(OperationOrder operationOrder)
      throws AxelorException {

    if (operationOrder.getWorkCenter() != null) {
      return operationOrder
          .getPlannedEndDateT()
          .minusSeconds(
              (int)
                  operationOrderService.computeEntireCycleDuration(
                      operationOrder, operationOrder.getManufOrder().getQty()));
    }

    return operationOrder.getPlannedEndDateT();
  }

  /**
   * Set the planned end date of the operation order according to the planning of the machine
   *
   * @param weeklyPlanning
   * @param operationOrder
   */
  protected void planWithPlanning(OperationOrder operationOrder, WeeklyPlanning weeklyPlanning) {

    LocalDateTime endDate = operationOrder.getPlannedEndDateT();
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
        operationOrder.setPlannedEndDateT(endDate.toLocalDate().atTime(secondPeriodTo));
      }
      /*
       * If the end date is after the end time of the first period (or equal, then the operation
       * order will end at the same time as the machine Example: Machine ends at 1pm. We set the
       * date to 2pm. Then the planned end date will be set to 1pm).
       */
      else if (firstPeriodTo != null
          && (endDateTime.isAfter(firstPeriodTo) || endDateTime.equals(firstPeriodTo))) {
        operationOrder.setPlannedEndDateT(endDate.toLocalDate().atTime(firstPeriodTo));
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
        operationOrderPlanningInfiniteCapacityService.searchForPreviousWorkingDay(
            operationOrder, weeklyPlanning, endDate);
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
       * If operation ends inside one period of the machine but starts before that period, then
       * we split the operation
       */
      if (secondPeriodFrom != null
          && startDateTime.isBefore(secondPeriodFrom)
          && endDateTime.isAfter(secondPeriodFrom)) {
        LocalDateTime plannedStartDate = startDate.toLocalDate().atTime(secondPeriodFrom);
        Long plannedDuration =
            DurationTool.getSecondsDuration(Duration.between(plannedStartDate, endDateTime));
        operationOrder.setPlannedDuration(plannedDuration);
        operationOrder.setPlannedStartDateT(plannedStartDate);
        operationOrderRepository.save(operationOrder);
        OperationOrder otherOperationOrder = JPA.copy(operationOrder, true);
        otherOperationOrder.setPlannedEndDateT(plannedStartDate);
        if (firstPeriodTo != null) {
          otherOperationOrder.setPlannedEndDateT(startDate.toLocalDate().atTime(firstPeriodTo));
        } else {
          operationOrderPlanningInfiniteCapacityService.searchForPreviousWorkingDay(
              otherOperationOrder, weeklyPlanning, plannedStartDate);
        }
        operationOrderRepository.save(otherOperationOrder);
        this.plan(otherOperationOrder);
      }
    }
  }
}
