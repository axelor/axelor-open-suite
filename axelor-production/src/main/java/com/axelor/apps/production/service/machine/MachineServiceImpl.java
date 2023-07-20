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
package com.axelor.apps.production.service.machine;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.EventsPlanning;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.dayplanning.DayPlanningService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.model.machine.MachineTimeSlot;
import com.axelor.i18n.I18n;
import com.axelor.utils.date.DurationTool;
import com.google.inject.Inject;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class MachineServiceImpl implements MachineService {

  protected OperationOrderRepository operationOrderRepository;
  protected WeeklyPlanningService weeklyPlanningService;
  protected DayPlanningService dayPlanningService;

  @Inject
  public MachineServiceImpl(
      OperationOrderRepository operationOrderRepository,
      WeeklyPlanningService weeklyPlanningService,
      DayPlanningService dayPlanningService) {
    this.operationOrderRepository = operationOrderRepository;
    this.weeklyPlanningService = weeklyPlanningService;
    this.dayPlanningService = dayPlanningService;
  }

  @Override
  public MachineTimeSlot getClosestAvailableTimeSlotFrom(
      Machine machine,
      LocalDateTime startDateT,
      LocalDateTime endDateT,
      OperationOrder operationOrder)
      throws AxelorException {

    return getClosestAvailableTimeSlotFrom(
        machine,
        startDateT,
        endDateT,
        operationOrder,
        DurationTool.getSecondsDuration(Duration.between(startDateT, endDateT)));
  }

  @SuppressWarnings("unchecked")
  protected MachineTimeSlot getClosestAvailableTimeSlotFrom(
      Machine machine,
      LocalDateTime startDateT,
      LocalDateTime endDateT,
      OperationOrder operationOrder,
      long initialDuration)
      throws AxelorException {

    EventsPlanning planning = machine.getPublicHolidayEventsPlanning();

    // If startDate is not available because of planning
    // Then we try for the next day
    LocalDateTime nextDayDateT = startDateT.plusDays(1).with(LocalTime.MIN);
    LocalDateTime plannedStartDateT = null;
    LocalDateTime plannedEndDateT = null;

    if (planning != null
        && planning.getEventsPlanningLineList() != null
        && planning.getEventsPlanningLineList().stream()
            .anyMatch(epl -> epl.getDate().equals(startDateT.toLocalDate()))) {

      return getClosestAvailableTimeSlotFrom(
          machine,
          nextDayDateT,
          nextDayDateT.plusSeconds(initialDuration),
          operationOrder,
          initialDuration);
    }

    if (machine.getWeeklyPlanning() != null) {
      // Planning on date at startDateT
      DayPlanning dayPlanning =
          weeklyPlanningService.findDayPlanning(
              machine.getWeeklyPlanning(), startDateT.toLocalDate());
      Optional<LocalDateTime> allowedStartDateTPeriodAt =
          dayPlanningService.getAllowedStartDateTPeriodAt(dayPlanning, startDateT);

      if (allowedStartDateTPeriodAt.isEmpty()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(ProductionExceptionMessage.OPERATION_ORDER_NO_PERIOD_FOUND_FOR_PLAN_DATES),
            operationOrder.getName());
      }

      plannedStartDateT = allowedStartDateTPeriodAt.get();
      plannedEndDateT = plannedStartDateT.plusSeconds(initialDuration);

      // Must end in a existing period.
      plannedEndDateT =
          dayPlanningService.getAllowedStartDateTPeriodAt(dayPlanning, plannedEndDateT).get();
      // Void duration is time where machine is not used (not in any period)
      long voidDuration =
          dayPlanningService.computeVoidDurationBetween(
              dayPlanning, plannedStartDateT, plannedEndDateT);

      long remainingTime =
          initialDuration
              - DurationTool.getSecondsDuration(
                  Duration.between(plannedStartDateT, plannedEndDateT).minusSeconds(voidDuration));
      // So the time 'spent' must be reported
      plannedEndDateT = plannedEndDateT.plusSeconds(remainingTime);

      // And of course it must end in a existing period.
      plannedEndDateT =
          dayPlanningService.getAllowedStartDateTPeriodAt(dayPlanning, plannedEndDateT).get();

    } else {
      // The machine does not have weekly planning so dates are ok for now.
      plannedStartDateT = startDateT;
      plannedEndDateT = endDateT;
    }

    long timeBeforeNextOperation =
        Optional.ofNullable(operationOrder.getWorkCenter())
            .map(WorkCenter::getTimeBeforeNextOperation)
            .orElse(0l);
    // Must check if dates are occupied by other operation orders
    // The first one of the list will be the last to finish

    List<OperationOrder> concurrentOperationOrders =
        operationOrderRepository
            .all()
            .filter(
                "self.machine = :machine"
                    + " AND ((self.plannedStartDateT <= :startDate AND self.plannedEndDateT > :startDateWithTime)"
                    + " OR (self.plannedStartDateT <= :endDate AND self.plannedEndDateT > :endDateWithTime))"
                    + " AND (self.manufOrder.statusSelect != :cancelled AND self.manufOrder.statusSelect != :finished)"
                    + " AND self.id != :operationOrderId")
            .bind("startDate", plannedStartDateT)
            .bind("endDate", plannedEndDateT)
            .bind("startDateWithTime", plannedStartDateT.minusSeconds(timeBeforeNextOperation))
            .bind("endDateWithTime", plannedEndDateT.minusSeconds(timeBeforeNextOperation))
            .bind("machine", machine)
            .bind("cancelled", ManufOrderRepository.STATUS_CANCELED)
            .bind("finished", ManufOrderRepository.STATUS_FINISHED)
            .bind("operationOrderId", operationOrder.getId())
            .order("-plannedEndDateT")
            .fetch();

    if (concurrentOperationOrders.isEmpty()) {
      MachineTimeSlot timeSlot = new MachineTimeSlot(plannedStartDateT, plannedEndDateT);
      return timeSlot;
    } else {
      OperationOrder lastOperationOrder = concurrentOperationOrders.get(0);

      return getClosestAvailableTimeSlotFrom(
          machine,
          lastOperationOrder.getPlannedEndDateT().plusSeconds(timeBeforeNextOperation),
          lastOperationOrder
              .getPlannedEndDateT()
              .plusSeconds(timeBeforeNextOperation + initialDuration),
          operationOrder,
          initialDuration);
    }
  }
}
