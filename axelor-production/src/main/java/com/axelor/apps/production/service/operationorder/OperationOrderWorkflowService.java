/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service.operationorder;

import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.MachineTool;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.OperationOrderDuration;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.WorkCenterGroup;
import com.axelor.apps.production.db.repo.MachineToolRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderDurationRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.db.repo.ProductionConfigRepository;
import com.axelor.apps.production.db.repo.WorkCenterRepository;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderStockMoveService;
import com.axelor.apps.production.service.manuforder.ManufOrderWorkflowService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.tool.date.DateTool;
import com.axelor.apps.tool.date.DurationTool;
import com.axelor.auth.AuthUtils;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class OperationOrderWorkflowService {
  protected OperationOrderStockMoveService operationOrderStockMoveService;
  protected OperationOrderRepository operationOrderRepo;
  protected OperationOrderDurationRepository operationOrderDurationRepo;
  protected AppProductionService appProductionService;
  protected MachineToolRepository machineToolRepo;
  @Inject protected WeeklyPlanningService weeklyPlanningService;

  @Inject
  public OperationOrderWorkflowService(
      OperationOrderStockMoveService operationOrderStockMoveService,
      OperationOrderRepository operationOrderRepo,
      OperationOrderDurationRepository operationOrderDurationRepo,
      AppProductionService appProductionService,
      MachineToolRepository machineToolRepo) {
    this.operationOrderStockMoveService = operationOrderStockMoveService;
    this.operationOrderRepo = operationOrderRepo;
    this.operationOrderDurationRepo = operationOrderDurationRepo;
    this.appProductionService = appProductionService;
    this.machineToolRepo = machineToolRepo;
  }

  @Transactional
  public void manageDurationWithMachinePlanning(
      OperationOrder operationOrder, WeeklyPlanning weeklyPlanning, Long duration)
      throws AxelorException {
    LocalDateTime startDate = operationOrder.getPlannedStartDateT();
    LocalDateTime endDate = operationOrder.getPlannedEndDateT();
    DayPlanning dayPlanning =
        weeklyPlanningService.findDayPlanning(weeklyPlanning, startDate.toLocalDate());
    if (dayPlanning != null) {
      LocalTime firstPeriodFrom = dayPlanning.getMorningFrom();
      LocalTime firstPeriodTo = dayPlanning.getMorningTo();
      LocalTime secondPeriodFrom = dayPlanning.getAfternoonFrom();
      LocalTime secondPeriodTo = dayPlanning.getAfternoonTo();
      LocalTime startDateTime = startDate.toLocalTime();
      LocalTime endDateTime = endDate.toLocalTime();

      /**
       * If operation begins inside one period of the machine but finished after that period, then
       * we split the operation
       */
      if (firstPeriodTo != null
          && startDateTime.isBefore(firstPeriodTo)
          && endDateTime.isAfter(firstPeriodTo)) {
        LocalDateTime plannedEndDate = startDate.toLocalDate().atTime(firstPeriodTo);
        Long plannedDuration =
            DurationTool.getSecondsDuration(Duration.between(startDate, plannedEndDate));
        operationOrder.setPlannedDuration(plannedDuration);
        operationOrder.setPlannedEndDateT(plannedEndDate);
        operationOrderRepo.save(operationOrder);
        OperationOrder otherOperationOrder = JPA.copy(operationOrder, true);
        otherOperationOrder.setPlannedStartDateT(plannedEndDate);
        if (secondPeriodFrom != null) {
          otherOperationOrder.setPlannedStartDateT(
              startDate.toLocalDate().atTime(secondPeriodFrom));
        } else {
          this.searchForNextWorkingDay(otherOperationOrder, weeklyPlanning, plannedEndDate);
        }
        operationOrderRepo.save(otherOperationOrder);
        this.plan(otherOperationOrder, operationOrder.getPlannedDuration());
      }
    }
  }

  /**
   * Set the planned start date of the operation order according to the planning of the machine
   *
   * @param weeklyPlanning
   * @param operationOrder
   */
  public void planWithPlanning(OperationOrder operationOrder, WeeklyPlanning weeklyPlanning) {
    LocalDateTime startDate = operationOrder.getPlannedStartDateT();
    DayPlanning dayPlanning =
        weeklyPlanningService.findDayPlanning(weeklyPlanning, startDate.toLocalDate());

    if (dayPlanning != null) {
      LocalTime firstPeriodFrom = dayPlanning.getMorningFrom();
      LocalTime firstPeriodTo = dayPlanning.getMorningTo();
      LocalTime secondPeriodFrom = dayPlanning.getAfternoonFrom();
      LocalTime secondPeriodTo = dayPlanning.getAfternoonTo();
      LocalTime startDateTime = startDate.toLocalTime();

      /**
       * If the start date is before the start time of the machine (or equal, then the operation
       * order will begins at the same time than the machine Example: Machine begins at 8am. We set
       * the date to 6am. Then the planned start date will be set to 8am.
       */
      if (firstPeriodFrom != null
          && (startDateTime.isBefore(firstPeriodFrom) || startDateTime.equals(firstPeriodFrom))) {
        operationOrder.setPlannedStartDateT(startDate.toLocalDate().atTime(firstPeriodFrom));
      }
      /**
       * If the machine has two periods, with a break between them, and the operation is planned
       * inside this period of time, then we will start the operation at the beginning of the
       * machine second period. Example: Machine hours is 8am to 12 am. 2pm to 6pm. We try to begins
       * at 1pm. The operation planned start date will be set to 2pm.
       */
      else if (firstPeriodTo != null
          && secondPeriodFrom != null
          && (startDateTime.isAfter(firstPeriodTo) || startDateTime.equals(firstPeriodTo))
          && (startDateTime.isBefore(secondPeriodFrom) || startDateTime.equals(secondPeriodFrom))) {
        operationOrder.setPlannedStartDateT(startDate.toLocalDate().atTime(secondPeriodFrom));
      }
      /**
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
        this.searchForNextWorkingDay(operationOrder, weeklyPlanning, startDate);
      }
    }
  }

  public void searchForNextWorkingDay(
      OperationOrder operationOrder, WeeklyPlanning weeklyPlanning, LocalDateTime startDate) {
    int daysToAddNbr = 0;
    DayPlanning nextDayPlanning = null;
    /** We will find the next DayPlanning with at least one working period. */
    do {

      daysToAddNbr++;
      nextDayPlanning =
          weeklyPlanningService.findDayPlanning(
              weeklyPlanning, startDate.toLocalDate().plusDays(daysToAddNbr));
    } while (nextDayPlanning.getAfternoonFrom() == null
        && nextDayPlanning.getMorningFrom() == null);

    /**
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

  /**
   * Plan an operation order. For successive calls, must be called by order of operation order
   * priority.
   *
   * @param operationOrder
   * @return
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  public OperationOrder plan(OperationOrder operationOrder, Long cumulatedDuration)
      throws AxelorException {

    if (CollectionUtils.isEmpty(operationOrder.getToConsumeProdProductList())) {
      Beans.get(OperationOrderService.class).createToConsumeProdProductList(operationOrder);
    }

    LocalDateTime plannedStartDate = operationOrder.getPlannedStartDateT();

    LocalDateTime lastOPerationDate = this.getLastOperationOrder(operationOrder);
    LocalDateTime maxDate = DateTool.max(plannedStartDate, lastOPerationDate);
    operationOrder.setPlannedStartDateT(maxDate);

    Machine machine = operationOrder.getMachineWorkCenter();
    WeeklyPlanning weeklyPlanning = null;
    if (machine != null) {
      weeklyPlanning = machine.getWeeklyPlanning();
    }
    if (weeklyPlanning != null) {
      this.planWithPlanning(operationOrder, weeklyPlanning);
    }

    operationOrder.setPlannedEndDateT(this.computePlannedEndDateT(operationOrder));
    if (cumulatedDuration != null) {
      operationOrder.setPlannedEndDateT(
          operationOrder
              .getPlannedEndDateT()
              .minusSeconds(cumulatedDuration)
              .plusSeconds(this.getMachineSetupDuration(operationOrder)));
    }
    Long plannedDuration =
        DurationTool.getSecondsDuration(
            Duration.between(
                operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT()));

    operationOrder.setPlannedDuration(plannedDuration);

    if (weeklyPlanning != null) {
      this.manageDurationWithMachinePlanning(operationOrder, weeklyPlanning, plannedDuration);
    }

    ManufOrder manufOrder = operationOrder.getManufOrder();
    if (manufOrder == null || manufOrder.getIsConsProOnOperation()) {
      operationOrderStockMoveService.createToConsumeStockMove(operationOrder);
    }

    operationOrder.setStatusSelect(OperationOrderRepository.STATUS_PLANNED);

    return operationOrderRepo.save(operationOrder);
  }

  public long getMachineSetupDuration(OperationOrder operationOrder) throws AxelorException {
    ProdProcessLine prodProcessLine = operationOrder.getProdProcessLine();

    long duration = 0;

    WorkCenterGroup workCenterGroup = prodProcessLine.getWorkCenterGroup();

    WorkCenter workCenter = null;

    if (workCenterGroup != null
        && workCenterGroup.getWorkCenterSet() != null
        && !workCenterGroup.getWorkCenterSet().isEmpty()) {
      workCenter =
          workCenterGroup.getWorkCenterSet().stream()
              .min(Comparator.comparing(WorkCenter::getSequence))
              .get();
    }

    if (workCenter == null) {
      return 0;
    }

    int workCenterTypeSelect = workCenter.getWorkCenterTypeSelect();

    if (workCenterTypeSelect == WorkCenterRepository.WORK_CENTER_TYPE_MACHINE
        || workCenterTypeSelect == WorkCenterRepository.WORK_CENTER_TYPE_BOTH) {
      Machine machine = workCenter.getMachine();
      if (machine == null) {
        throw new AxelorException(
            workCenter,
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get(IExceptionMessage.WORKCENTER_NO_MACHINE),
            workCenter.getName());
      }
      duration += machine.getStartingDuration();
      duration += machine.getEndingDuration();
      duration += machine.getSetupDuration();
    }

    return duration;
  }

  /**
   * Replan an operation order. For successive calls, must reset planned dates first, then call by
   * order of operation order priority.
   *
   * @param operationOrder
   * @return
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  public OperationOrder replan(OperationOrder operationOrder) throws AxelorException {

    operationOrder.setPlannedStartDateT(this.getLastOperationOrder(operationOrder));

    operationOrder.setPlannedEndDateT(this.computePlannedEndDateT(operationOrder));

    operationOrder.setPlannedDuration(
        DurationTool.getSecondsDuration(
            Duration.between(
                operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT())));

    return operationOrderRepo.save(operationOrder);
  }

  /**
   * Reset the planned dates from the specified operation order list.
   *
   * @param operationOrderList
   * @return
   */
  @Transactional
  public List<OperationOrder> resetPlannedDates(List<OperationOrder> operationOrderList) {
    for (OperationOrder operationOrder : operationOrderList) {
      operationOrder.setPlannedStartDateT(null);
      operationOrder.setPlannedEndDateT(null);
      operationOrder.setPlannedDuration(null);
    }

    return operationOrderList;
  }

  public LocalDateTime getLastOperationOrder(OperationOrder operationOrder) {

    OperationOrder lastOperationOrder =
        operationOrderRepo
            .all()
            .filter(
                "self.manufOrder = ?1 AND self.priority <= ?2 AND self.statusSelect >= 3 AND self.statusSelect < 6 AND self.id != ?3",
                operationOrder.getManufOrder(),
                operationOrder.getPriority(),
                operationOrder.getId())
            .order("-priority")
            .order("-plannedEndDateT")
            .fetchOne();

    if (lastOperationOrder != null) {
      if (lastOperationOrder.getPriority() == operationOrder.getPriority()) {
        if (lastOperationOrder.getPlannedStartDateT() != null
            && lastOperationOrder
                .getPlannedStartDateT()
                .isAfter(operationOrder.getManufOrder().getPlannedStartDateT())) {
          if (lastOperationOrder
              .getMachineWorkCenter()
              .equals(operationOrder.getMachineWorkCenter())) {
            return lastOperationOrder.getPlannedEndDateT();
          }
          return lastOperationOrder.getPlannedStartDateT();
        } else {
          return operationOrder.getManufOrder().getPlannedStartDateT();
        }
      } else {
        if (lastOperationOrder.getPlannedEndDateT() != null
            && lastOperationOrder
                .getPlannedEndDateT()
                .isAfter(operationOrder.getManufOrder().getPlannedStartDateT())) {
          return lastOperationOrder.getPlannedEndDateT();
        } else {
          return operationOrder.getManufOrder().getPlannedStartDateT();
        }
      }
    }

    return operationOrder.getManufOrder().getPlannedStartDateT();
  }

  /**
   * Starts the given {@link OperationOrder} and sets its starting time
   *
   * @param operationOrder An operation order
   */
  @Transactional(rollbackOn = {Exception.class})
  public void start(OperationOrder operationOrder) throws AxelorException {
    if (operationOrder.getStatusSelect() != OperationOrderRepository.STATUS_IN_PROGRESS) {
      operationOrder.setStatusSelect(OperationOrderRepository.STATUS_IN_PROGRESS);
      operationOrder.setRealStartDateT(appProductionService.getTodayDateTime().toLocalDateTime());

      startOperationOrderDuration(operationOrder);

      if (operationOrder.getManufOrder() != null) {
        int beforeOrAfterConfig =
            operationOrder.getManufOrder().getProdProcess().getStockMoveRealizeOrderSelect();
        if (beforeOrAfterConfig == ProductionConfigRepository.REALIZE_START) {
          for (StockMove stockMove : operationOrder.getInStockMoveList()) {
            Beans.get(ManufOrderStockMoveService.class).finishStockMove(stockMove);
          }

          StockMove newStockMove =
              operationOrderStockMoveService._createToConsumeStockMove(
                  operationOrder, operationOrder.getManufOrder().getCompany());
          newStockMove.setStockMoveLineList(new ArrayList<>());
          Beans.get(StockMoveService.class).plan(newStockMove);
          operationOrder.addInStockMoveListItem(newStockMove);
        }
      }
      operationOrderRepo.save(operationOrder);
    }

    if (operationOrder.getManufOrder().getStatusSelect()
        != ManufOrderRepository.STATUS_IN_PROGRESS) {
      Beans.get(ManufOrderWorkflowService.class).start(operationOrder.getManufOrder());
    }
  }

  /**
   * Pauses the given {@link OperationOrder} and sets its pausing time
   *
   * @param operationOrder An operation order
   */
  @Transactional
  public void pause(OperationOrder operationOrder) {
    operationOrder.setStatusSelect(OperationOrderRepository.STATUS_STANDBY);

    stopOperationOrderDuration(operationOrder);

    operationOrderRepo.save(operationOrder);
  }

  /**
   * Resumes the given {@link OperationOrder} and sets its resuming time
   *
   * @param operationOrder An operation order
   */
  @Transactional
  public void resume(OperationOrder operationOrder) {
    operationOrder.setStatusSelect(OperationOrderRepository.STATUS_IN_PROGRESS);

    startOperationOrderDuration(operationOrder);

    operationOrderRepo.save(operationOrder);
  }

  /**
   * Ends the given {@link OperationOrder} and sets its stopping time<br>
   * Realizes the linked stock moves
   *
   * @param operationOrder An operation order
   */
  @Transactional(rollbackOn = {Exception.class})
  public void finish(OperationOrder operationOrder) throws AxelorException {
    operationOrder.setStatusSelect(OperationOrderRepository.STATUS_FINISHED);
    operationOrder.setRealEndDateT(appProductionService.getTodayDateTime().toLocalDateTime());

    stopOperationOrderDuration(operationOrder);

    operationOrderStockMoveService.finish(operationOrder);
    operationOrderRepo.save(operationOrder);
    calculateHoursOfUse(operationOrder);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void finishAndAllOpFinished(OperationOrder operationOrder) throws AxelorException {
    finish(operationOrder);
    Beans.get(ManufOrderWorkflowService.class).allOpFinished(operationOrder.getManufOrder());
  }

  /**
   * Cancels the given {@link OperationOrder} and its linked stock moves And sets its stopping time
   *
   * @param operationOrder An operation order
   */
  @Transactional
  public void cancel(OperationOrder operationOrder) throws AxelorException {
    int oldStatus = operationOrder.getStatusSelect();
    operationOrder.setStatusSelect(OperationOrderRepository.STATUS_CANCELED);

    if (oldStatus == OperationOrderRepository.STATUS_IN_PROGRESS) {
      stopOperationOrderDuration(operationOrder);
    }
    if (operationOrder.getConsumedStockMoveLineList() != null) {
      operationOrder
          .getConsumedStockMoveLineList()
          .forEach(stockMoveLine -> stockMoveLine.setConsumedOperationOrder(null));
    }
    operationOrderStockMoveService.cancel(operationOrder);

    operationOrderRepo.save(operationOrder);
  }

  /**
   * Starts an {@link OperationOrderDuration} and links it to the given {@link OperationOrder}
   *
   * @param operationOrder An operation order
   */
  public void startOperationOrderDuration(OperationOrder operationOrder) {
    OperationOrderDuration duration = new OperationOrderDuration();
    duration.setStartedBy(AuthUtils.getUser());
    duration.setStartingDateTime(appProductionService.getTodayDateTime().toLocalDateTime());
    operationOrder.addOperationOrderDurationListItem(duration);
  }

  /**
   * Ends the last {@link OperationOrderDuration} and sets the real duration of {@code
   * operationOrder}<br>
   * Adds the real duration to the {@link Machine} linked to {@code operationOrder}
   *
   * @param operationOrder An operation order
   */
  public void stopOperationOrderDuration(OperationOrder operationOrder) {
    OperationOrderDuration duration =
        operationOrderDurationRepo
            .all()
            .filter(
                "self.operationOrder.id = ? AND self.stoppedBy IS NULL AND self.stoppingDateTime IS NULL",
                operationOrder.getId())
            .fetchOne();
    duration.setStoppedBy(AuthUtils.getUser());
    duration.setStoppingDateTime(appProductionService.getTodayDateTime().toLocalDateTime());

    if (operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_FINISHED) {
      long durationLong = DurationTool.getSecondsDuration(computeRealDuration(operationOrder));
      operationOrder.setRealDuration(durationLong);
      Machine machine = operationOrder.getMachineWorkCenter();
      if (machine != null) {
        machine.setOperatingDuration(machine.getOperatingDuration() + durationLong);
      }
    }

    operationOrderDurationRepo.save(duration);
  }

  /**
   * Compute the duration of operation order, then fill {@link OperationOrder#realDuration} with the
   * computed value.
   *
   * @param operationOrder
   */
  public void updateRealDuration(OperationOrder operationOrder) {
    long durationLong = DurationTool.getSecondsDuration(computeRealDuration(operationOrder));
    operationOrder.setRealDuration(durationLong);
  }

  /**
   * Computes the duration of all the {@link OperationOrderDuration} of {@code operationOrder}
   *
   * @param operationOrder An operation order
   * @return Real duration of {@code operationOrder}
   */
  public Duration computeRealDuration(OperationOrder operationOrder) {
    Duration totalDuration = Duration.ZERO;

    List<OperationOrderDuration> operationOrderDurations =
        operationOrder.getOperationOrderDurationList();
    if (operationOrderDurations != null) {
      for (OperationOrderDuration operationOrderDuration : operationOrderDurations) {
        if (operationOrderDuration.getStartingDateTime() != null
            && operationOrderDuration.getStoppingDateTime() != null) {
          totalDuration =
              totalDuration.plus(
                  Duration.between(
                      operationOrderDuration.getStartingDateTime(),
                      operationOrderDuration.getStoppingDateTime()));
        }
      }
    }

    return totalDuration;
  }

  /**
   * Set planned start and end dates.
   *
   * @param operationOrder
   * @param plannedStartDateT
   * @param plannedEndDateT
   * @return
   */
  @Transactional
  public OperationOrder setPlannedDates(
      OperationOrder operationOrder,
      LocalDateTime plannedStartDateT,
      LocalDateTime plannedEndDateT) {

    operationOrder.setPlannedStartDateT(plannedStartDateT);
    operationOrder.setPlannedEndDateT(plannedEndDateT);
    return computeDuration(operationOrder);
  }

  /**
   * Set real start and end dates.
   *
   * @param operationOrder
   * @param realStartDateT
   * @param realEndDateT
   * @return
   */
  @Transactional
  public OperationOrder setRealDates(
      OperationOrder operationOrder, LocalDateTime realStartDateT, LocalDateTime realEndDateT) {

    operationOrder.setRealStartDateT(realStartDateT);
    operationOrder.setRealEndDateT(realEndDateT);
    return computeDuration(operationOrder);
  }

  @Transactional
  public OperationOrder computeDuration(OperationOrder operationOrder) {
    Long duration;

    if (operationOrder.getPlannedStartDateT() != null
        && operationOrder.getPlannedEndDateT() != null) {
      duration =
          DurationTool.getSecondsDuration(
              Duration.between(
                  operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT()));
      operationOrder.setPlannedDuration(duration);
    }

    updateRealDuration(operationOrder);

    return operationOrder;
  }

  public LocalDateTime computePlannedEndDateT(OperationOrder operationOrder)
      throws AxelorException {

    if (operationOrder.getWorkCenter() != null) {
      return operationOrder
          .getPlannedStartDateT()
          .plusSeconds(
              (int)
                  this.computeEntireCycleDuration(
                      operationOrder, operationOrder.getManufOrder().getQty()));
    }

    return operationOrder.getPlannedStartDateT();
  }

  public long computeEntireCycleDuration(OperationOrder operationOrder, BigDecimal qty)
      throws AxelorException {
    ProdProcessLine prodProcessLine = operationOrder.getProdProcessLine();
    WorkCenterGroup workCenterGroup = prodProcessLine.getWorkCenterGroup();

    WorkCenter workCenter = null;

    if (workCenterGroup != null
        && workCenterGroup.getWorkCenterSet() != null
        && !workCenterGroup.getWorkCenterSet().isEmpty()) {
      workCenter =
          workCenterGroup.getWorkCenterSet().stream()
              .min(Comparator.comparing(WorkCenter::getSequence))
              .get();
    }

    long duration = 0;

    if (workCenter != null) {
      BigDecimal maxCapacityPerCycle = workCenter.getMaxCapacityPerCycle();

      BigDecimal nbCycles;
      if (maxCapacityPerCycle.compareTo(BigDecimal.ZERO) == 0) {
        nbCycles = qty;
      } else {
        nbCycles = qty.divide(maxCapacityPerCycle, 0, RoundingMode.UP);
      }

      int workCenterTypeSelect = workCenter.getWorkCenterTypeSelect();

      if (workCenterTypeSelect == WorkCenterRepository.WORK_CENTER_TYPE_MACHINE
          || workCenterTypeSelect == WorkCenterRepository.WORK_CENTER_TYPE_BOTH) {
        Machine machine = workCenter.getMachine();
        if (machine == null) {
          throw new AxelorException(
              workCenter,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(IExceptionMessage.WORKCENTER_NO_MACHINE),
              workCenter.getName());
        }
        duration += machine.getStartingDuration();
        duration += machine.getEndingDuration();
        duration +=
            nbCycles
                .subtract(new BigDecimal(1))
                .multiply(new BigDecimal(machine.getSetupDuration()))
                .longValue();
      }

      BigDecimal durationPerCycle = new BigDecimal(workCenter.getDurationPerCycle());
      duration += nbCycles.multiply(durationPerCycle).longValue();
    }

    return duration;
  }

  private void calculateHoursOfUse(OperationOrder operationOrder) {

    if (operationOrder.getMachineTool() == null) {
      return;
    }

    Double hoursOfUse =
        operationOrderRepo
            .all()
            .filter("self.machineTool.id = :id AND self.statusSelect = 6")
            .bind("id", operationOrder.getMachineTool().getId())
            .fetchStream()
            .mapToDouble(list -> list.getRealDuration())
            .sum();

    MachineTool machineTool = machineToolRepo.find(operationOrder.getMachineTool().getId());
    machineTool.setHoursOfUse(Double.valueOf(hoursOfUse).longValue());
    machineToolRepo.save(machineTool);
  }
}
