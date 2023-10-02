package com.axelor.apps.production.service.operationorder.planning;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.service.operationorder.OperationOrderService;
import com.google.inject.Inject;
import java.time.LocalDateTime;

public class OperationOrderPlanningInfiniteCapacity {

  protected OperationOrderService operationOrderService;
  protected WeeklyPlanningService weeklyPlanningService;

  @Inject
  public OperationOrderPlanningInfiniteCapacity(
      OperationOrderService operationOrderService, WeeklyPlanningService weeklyPlanningService) {
    this.operationOrderService = operationOrderService;
    this.weeklyPlanningService = weeklyPlanningService;
  }

  protected void searchForNextWorkingDay(
      OperationOrder operationOrder, WeeklyPlanning weeklyPlanning, LocalDateTime startDate) {
    int daysToAddNbr = 0;
    DayPlanning nextDayPlanning;
    /* We will find the next DayPlanning with at least one working period. */
    do {

      daysToAddNbr++;
      nextDayPlanning =
          weeklyPlanningService.findDayPlanning(
              weeklyPlanning, startDate.toLocalDate().plusDays(daysToAddNbr));
    } while (nextDayPlanning.getAfternoonFrom() == null
        && nextDayPlanning.getMorningFrom() == null);

    /*
     * We will add the nbr of days to retrieve the working day, and set the time to either the first
     * morning period or the first afternoon period.
     */
    if (nextDayPlanning.getMorningFrom() != null) {
      operationOrder.setPlannedStartDateT(
          startDate.toLocalDate().plusDays(daysToAddNbr).atTime(nextDayPlanning.getMorningFrom()));
    } else if (nextDayPlanning.getAfternoonFrom() != null) {
      operationOrder.setPlannedStartDateT(
          startDate
              .toLocalDate()
              .plusDays(daysToAddNbr)
              .atTime(nextDayPlanning.getAfternoonFrom()));
    }
  }

  public void searchForPreviousWorkingDay(
      OperationOrder operationOrder, WeeklyPlanning weeklyPlanning, LocalDateTime endDate) {
    int daysToSubstractNbr = 0;
    DayPlanning previousDayPlanning;
    /* change comment . We will find the previous DayPlanning with at least one working period. */
    do {

      daysToSubstractNbr++;
      previousDayPlanning =
          weeklyPlanningService.findDayPlanning(
              weeklyPlanning, endDate.toLocalDate().minusDays(daysToSubstractNbr));
    } while (previousDayPlanning.getAfternoonFrom() == null
        && previousDayPlanning.getMorningFrom() == null);

    /*
     * We will subtract the nbr of days to retrieve the working day, and set the time to either the ending of the first
     * morning period or the first afternoon period.
     */
    if (previousDayPlanning.getAfternoonTo() != null) {
      operationOrder.setPlannedEndDateT(
          endDate
              .toLocalDate()
              .minusDays(daysToSubstractNbr)
              .atTime(previousDayPlanning.getAfternoonTo()));
    } else if (previousDayPlanning.getMorningTo() != null) {
      operationOrder.setPlannedEndDateT(
          endDate
              .toLocalDate()
              .minusDays(daysToSubstractNbr)
              .atTime(previousDayPlanning.getMorningTo()));
    }
  }

  public LocalDateTime computePlannedEndDateT(OperationOrder operationOrder)
      throws AxelorException {

    if (operationOrder.getWorkCenter() != null) {
      return operationOrder
          .getPlannedStartDateT()
          .plusSeconds(
              (int)
                  operationOrderService.computeEntireCycleDuration(
                      operationOrder, operationOrder.getManufOrder().getQty()));
    }

    return operationOrder.getPlannedStartDateT();
  }
}
