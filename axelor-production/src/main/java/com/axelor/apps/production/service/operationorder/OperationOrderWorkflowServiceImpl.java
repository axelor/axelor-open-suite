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
package com.axelor.apps.production.service.operationorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.MachineTool;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.OperationOrderDuration;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.MachineToolRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderDurationRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.db.repo.ProductionConfigRepository;
import com.axelor.apps.production.db.repo.WorkCenterRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.model.machine.MachineTimeSlot;
import com.axelor.apps.production.service.ProdProcessLineService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.machine.MachineService;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.production.service.manuforder.ManufOrderStockMoveService;
import com.axelor.apps.production.service.manuforder.ManufOrderWorkflowService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.date.DateTool;
import com.axelor.utils.date.DurationTool;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class OperationOrderWorkflowServiceImpl implements OperationOrderWorkflowService {
  protected OperationOrderStockMoveService operationOrderStockMoveService;
  protected OperationOrderRepository operationOrderRepo;
  protected OperationOrderDurationRepository operationOrderDurationRepo;
  protected AppProductionService appProductionService;
  protected MachineToolRepository machineToolRepo;
  protected WeeklyPlanningService weeklyPlanningService;
  protected ProdProcessLineService prodProcessLineService;
  protected MachineService machineService;
  protected ManufOrderService manufOrderService;
  protected ManufOrderWorkflowService manufOrderWorkflowService;
  protected ManufOrderStockMoveService manufOrderStockMoveService;

  @Inject
  public OperationOrderWorkflowServiceImpl(
      OperationOrderStockMoveService operationOrderStockMoveService,
      OperationOrderRepository operationOrderRepo,
      OperationOrderDurationRepository operationOrderDurationRepo,
      AppProductionService appProductionService,
      MachineToolRepository machineToolRepo,
      WeeklyPlanningService weeklyPlanningService,
      ProdProcessLineService prodProcessLineService,
      MachineService machineService,
      ManufOrderService manufOrderService,
      ManufOrderWorkflowService manufOrderWorkflowService,
      ManufOrderStockMoveService manufOrderStockMoveService) {
    this.operationOrderStockMoveService = operationOrderStockMoveService;
    this.operationOrderRepo = operationOrderRepo;
    this.operationOrderDurationRepo = operationOrderDurationRepo;
    this.appProductionService = appProductionService;
    this.machineToolRepo = machineToolRepo;
    this.weeklyPlanningService = weeklyPlanningService;
    this.prodProcessLineService = prodProcessLineService;
    this.machineService = machineService;
    this.manufOrderService = manufOrderService;
    this.manufOrderWorkflowService = manufOrderWorkflowService;
    this.manufOrderStockMoveService = manufOrderStockMoveService;
  }

  /**
   * Plan an operation order. For successive calls, must be called by order of operation order
   * priority.
   *
   * @param operationOrder
   * @return
   * @throws AxelorException
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public OperationOrder plan(OperationOrder operationOrder, Long cumulatedDuration)
      throws AxelorException {

    if (CollectionUtils.isEmpty(operationOrder.getToConsumeProdProductList())) {
      Beans.get(OperationOrderService.class).createToConsumeProdProductList(operationOrder);
    }

    planPlannedDates(operationOrder);

    ManufOrder manufOrder = operationOrder.getManufOrder();
    if (manufOrder == null || manufOrder.getIsConsProOnOperation()) {
      operationOrderStockMoveService.createToConsumeStockMove(operationOrder);
    }

    operationOrder.setStatusSelect(OperationOrderRepository.STATUS_PLANNED);
    operationOrder.setRealStartDateT(null);
    operationOrder.setRealEndDateT(null);
    operationOrder.setRealDuration(null);

    return operationOrderRepo.save(operationOrder);
  }

  protected void planPlannedDates(OperationOrder operationOrder) throws AxelorException {
    Machine machine = operationOrder.getMachine();
    if (machine != null) {
      planDatesWithMachine(operationOrder, machine);
    } else {
      planDatesWithoutMachine(operationOrder);
    }
  }

  protected void planDatesWithoutMachine(OperationOrder operationOrder) throws AxelorException {
    LocalDateTime plannedStartDate = operationOrder.getPlannedStartDateT();

    LocalDateTime lastOPerationDate = this.getLastOperationDate(operationOrder);
    LocalDateTime maxDate = DateTool.max(plannedStartDate, lastOPerationDate);

    operationOrder.setPlannedStartDateT(maxDate);

    operationOrder.setPlannedEndDateT(
        operationOrder.getPlannedStartDateT().plusSeconds(this.getDuration(operationOrder)));

    Long plannedDuration =
        DurationTool.getSecondsDuration(
            Duration.between(
                operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT()));

    operationOrder.setPlannedDuration(plannedDuration);
  }

  protected void planDatesWithMachine(OperationOrder operationOrder, Machine machine)
      throws AxelorException {

    LocalDateTime plannedStartDate = operationOrder.getPlannedStartDateT();

    LocalDateTime lastOPerationDate = this.getLastOperationDate(operationOrder);

    LocalDateTime maxDate = DateTool.max(plannedStartDate, lastOPerationDate);

    MachineTimeSlot freeMachineTimeSlot =
        machineService.getClosestAvailableTimeSlotFrom(
            machine, maxDate, maxDate.plusSeconds(getDuration(operationOrder)), operationOrder);
    operationOrder.setPlannedStartDateT(freeMachineTimeSlot.getStartDateT());
    operationOrder.setPlannedEndDateT(freeMachineTimeSlot.getEndDateT());

    Long plannedDuration =
        DurationTool.getSecondsDuration(
            Duration.between(
                operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT()));

    operationOrder.setPlannedDuration(plannedDuration);
  }

  @Override
  public long getMachineSetupDuration(OperationOrder operationOrder) throws AxelorException {
    ProdProcessLine prodProcessLine = operationOrder.getProdProcessLine();

    long duration = 0;

    WorkCenter workCenter = prodProcessLine.getWorkCenter();

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
            I18n.get(ProductionExceptionMessage.WORKCENTER_NO_MACHINE),
            workCenter.getName());
      }
      duration += workCenter.getStartingDuration();
      duration += workCenter.getEndingDuration();
      duration += workCenter.getSetupDuration();
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
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public OperationOrder replan(OperationOrder operationOrder) throws AxelorException {

    operationOrder.setPlannedStartDateT(null);
    operationOrder.setPlannedEndDateT(null);

    planPlannedDates(operationOrder);

    return operationOrderRepo.save(operationOrder);
  }

  /**
   * Reset the planned dates from the specified operation order list.
   *
   * @param operationOrderList
   * @return
   */
  @Override
  @Transactional
  public List<OperationOrder> resetPlannedDates(List<OperationOrder> operationOrderList) {
    for (OperationOrder operationOrder : operationOrderList) {
      operationOrder.setPlannedStartDateT(null);
      operationOrder.setPlannedEndDateT(null);
      operationOrder.setPlannedDuration(null);
    }

    return operationOrderList;
  }

  protected LocalDateTime getLastOperationDate(OperationOrder operationOrder) {
    ManufOrder manufOrder = operationOrder.getManufOrder();
    OperationOrder lastOperationOrder =
        operationOrderRepo
            .all()
            .filter(
                "self.manufOrder = :manufOrder AND ((self.priority = :priority AND self.machine = :machine) OR self.priority < :priority) AND self.statusSelect BETWEEN :statusPlanned AND :statusStandby AND self.id != :operationOrderId")
            .bind("manufOrder", manufOrder)
            .bind("priority", operationOrder.getPriority())
            .bind("statusPlanned", OperationOrderRepository.STATUS_PLANNED)
            .bind("statusStandby", OperationOrderRepository.STATUS_STANDBY)
            .bind("machine", operationOrder.getMachine())
            .bind("operationOrderId", operationOrder.getId())
            .order("-priority")
            .order("-plannedEndDateT")
            .fetchOne();

    LocalDateTime manufOrderPlannedStartDateT = manufOrder.getPlannedStartDateT();
    if (lastOperationOrder == null) {
      return manufOrderPlannedStartDateT;
    }

    LocalDateTime plannedEndDateT = lastOperationOrder.getPlannedEndDateT();

    if (Objects.equals(lastOperationOrder.getPriority(), operationOrder.getPriority())) {
      LocalDateTime plannedStartDateT = lastOperationOrder.getPlannedStartDateT();
      if (plannedStartDateT != null && plannedStartDateT.isAfter(manufOrderPlannedStartDateT)) {
        boolean isOnSameMachine =
            Objects.equals(lastOperationOrder.getMachine(), operationOrder.getMachine());
        return isOnSameMachine ? plannedEndDateT : plannedStartDateT;
      }

    } else if (plannedEndDateT != null && plannedEndDateT.isAfter(manufOrderPlannedStartDateT)) {
      return plannedEndDateT;
    }

    return manufOrderPlannedStartDateT;
  }

  protected List<OperationOrder> getNextOrderedOperationOrders(OperationOrder operationOrder) {
    ManufOrder manufOrder = operationOrder.getManufOrder();

    return manufOrder.getOperationOrderList().stream()
        .filter(oo -> oo.getPriority() > operationOrder.getPriority() && !oo.equals(operationOrder))
        .sorted((oo1, oo2) -> oo1.getPriority() - oo2.getPriority())
        .collect(Collectors.toList());
  }

  /**
   * Starts the given {@link OperationOrder} and sets its starting time
   *
   * @param operationOrder An operation order
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void start(OperationOrder operationOrder) throws AxelorException {

    if (operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_IN_PROGRESS) {
      startOperationOrderDuration(operationOrder, AuthUtils.getUser());
    } else {
      start(operationOrder, AuthUtils.getUser());
    }
    operationOrderRepo.save(operationOrder);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void start(OperationOrder operationOrder, User user) throws AxelorException {

    if (operationOrder.getStatusSelect() != OperationOrderRepository.STATUS_IN_PROGRESS) {
      operationOrder.setStatusSelect(OperationOrderRepository.STATUS_IN_PROGRESS);
      operationOrder.setRealStartDateT(appProductionService.getTodayDateTime().toLocalDateTime());

      startOperationOrderDuration(operationOrder, user);

      if (operationOrder.getManufOrder() != null) {
        int beforeOrAfterConfig =
            operationOrder.getManufOrder().getProdProcess().getStockMoveRealizeOrderSelect();
        if (beforeOrAfterConfig == ProductionConfigRepository.REALIZE_START) {
          for (StockMove stockMove : operationOrder.getInStockMoveList()) {
            manufOrderStockMoveService.finishStockMove(stockMove);
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
      manufOrderWorkflowService.start(operationOrder.getManufOrder());
    }
  }

  /**
   * Pauses the given {@link OperationOrder} and sets its pausing time
   *
   * @param operationOrder An operation order
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void pause(OperationOrder operationOrder) {

    operationOrder.setStatusSelect(OperationOrderRepository.STATUS_STANDBY);

    stopOperationOrderDuration(operationOrder);

    pauseManufOrder(operationOrder);
    operationOrderRepo.save(operationOrder);
  }

  protected void pauseManufOrder(OperationOrder operationOrder) {
    ManufOrder manufOrder = operationOrder.getManufOrder();
    if (manufOrder.getOperationOrderList().stream()
        .allMatch(
            order -> order.getStatusSelect() != OperationOrderRepository.STATUS_IN_PROGRESS)) {
      manufOrder.setStatusSelect(ManufOrderRepository.STATUS_STANDBY);
    }
  }

  @Override
  @Transactional
  public void pause(OperationOrder operationOrder, User user) {

    stopOperationOrderDuration(operationOrder, AuthUtils.getUser());

    // All operations orders duration are stopped
    if (allOperationDurationAreStopped(operationOrder)) {
      operationOrder.setStatusSelect(OperationOrderRepository.STATUS_STANDBY);
    }
    pauseManufOrder(operationOrder);
    operationOrderRepo.save(operationOrder);
  }

  /**
   * Resumes the given {@link OperationOrder} and sets its resuming time
   *
   * @param operationOrder An operation order
   */
  @Override
  @Transactional
  public void resume(OperationOrder operationOrder) {
    operationOrder.setStatusSelect(OperationOrderRepository.STATUS_IN_PROGRESS);

    startOperationOrderDuration(operationOrder, AuthUtils.getUser());
    operationOrder.getManufOrder().setStatusSelect(ManufOrderRepository.STATUS_IN_PROGRESS);
    operationOrderRepo.save(operationOrder);
  }

  /**
   * Ends the given {@link OperationOrder} and sets its stopping time<br>
   * Realizes the linked stock moves
   *
   * @param operationOrder An operation order
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void finish(OperationOrder operationOrder) throws AxelorException {
    operationOrder.setStatusSelect(OperationOrderRepository.STATUS_FINISHED);
    operationOrder.setRealEndDateT(appProductionService.getTodayDateTime().toLocalDateTime());

    stopOperationOrderDuration(operationOrder);

    operationOrderStockMoveService.finish(operationOrder);
    operationOrderRepo.save(operationOrder);
    calculateHoursOfUse(operationOrder);
  }

  /**
   * Ends the given {@link OperationOrder} and sets its stopping time<br>
   * Realizes the linked stock moves
   *
   * @param operationOrder An operation order
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void finish(OperationOrder operationOrder, User user) throws AxelorException {

    stopOperationOrderDuration(operationOrder, user);

    // All operations orders duration are stopped
    if (allOperationDurationAreStopped(operationOrder)) {

      operationOrder.setStatusSelect(OperationOrderRepository.STATUS_FINISHED);
      computeFinishDuration(operationOrder);
      operationOrder.setRealEndDateT(appProductionService.getTodayDateTime().toLocalDateTime());
      operationOrderStockMoveService.finish(operationOrder);
      operationOrderRepo.save(operationOrder);
      calculateHoursOfUse(operationOrder);
      return;
    }

    operationOrderRepo.save(operationOrder);
  }

  protected boolean allOperationDurationAreStopped(OperationOrder operationOrder) {
    return operationOrder.getOperationOrderDurationList().stream()
        .allMatch(oo -> oo.getStoppingDateTime() != null);
  }

  @Override
  public void finishAndAllOpFinished(OperationOrder operationOrder) throws AxelorException {
    ManufOrder manufOrder = operationOrder.getManufOrder();
    finishProcess(operationOrder);
    manufOrderWorkflowService.sendFinishedMail(manufOrder);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void finishProcess(OperationOrder operationOrder) throws AxelorException {
    finish(operationOrder);
    manufOrderWorkflowService.allOpFinished(operationOrder.getManufOrder());
  }

  /**
   * Cancels the given {@link OperationOrder} and its linked stock moves And sets its stopping time
   *
   * @param operationOrder An operation order
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
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
  @Override
  public void startOperationOrderDuration(OperationOrder operationOrder) {
    startOperationOrderDuration(operationOrder, AuthUtils.getUser());
  }

  protected void startOperationOrderDuration(OperationOrder operationOrder, User user) {

    if (operationOrder.getOperationOrderDurationList() != null
        && operationOrder.getOperationOrderDurationList().stream()
            .noneMatch(
                ood -> ood.getStartedBy().equals(user) && ood.getStoppingDateTime() == null)) {
      OperationOrderDuration duration = new OperationOrderDuration();
      duration.setStartedBy(user);
      duration.setStartingDateTime(appProductionService.getTodayDateTime().toLocalDateTime());
      operationOrder.addOperationOrderDurationListItem(duration);
    }
  }

  /**
   * Ends every operationDuration of operation order and sets the real duration of {@code
   * operationOrder}<br>
   * Adds the real duration to the {@link Machine} linked to {@code operationOrder}
   *
   * @param operationOrder An operation order
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void stopOperationOrderDuration(OperationOrder operationOrder) {

    stopAllOperationOrderDuration(operationOrder);
  }

  protected void stopAllOperationOrderDuration(OperationOrder operationOrder) {
    if (operationOrder.getOperationOrderDurationList() != null) {
      operationOrder.getOperationOrderDurationList().stream()
          .filter(ood -> ood.getStoppingDateTime() == null)
          .forEach(
              ood -> {
                stopOperationOrderDuration(ood);
              });
    }
  }

  @Override
  public void stopOperationOrderDuration(OperationOrder operationOrder, User user) {

    Map<String, Object> bindingMap = new HashMap<>();
    StringBuilder operationOrderFilter =
        new StringBuilder(
            "self.operationOrder.id = :operationOrderId AND self.stoppedBy IS NULL AND self.stoppingDateTime IS NULL");
    bindingMap.put("operationOrderId", operationOrder.getId());

    if (user != null) {
      operationOrderFilter.append(" AND self.startedBy = :currentUser");
      bindingMap.put("currentUser", user);
    }

    OperationOrderDuration duration =
        operationOrderDurationRepo
            .all()
            .filter(operationOrderFilter.toString())
            .bind(bindingMap)
            .fetchOne();

    stopOperationOrderDuration(duration);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void stopOperationOrderDuration(OperationOrderDuration duration) {
    if (duration != null) {
      duration.setStoppedBy(AuthUtils.getUser());
      duration.setStoppingDateTime(appProductionService.getTodayDateTime().toLocalDateTime());
      operationOrderDurationRepo.save(duration);
    }
  }

  protected void computeFinishDuration(OperationOrder operationOrder) {
    if (operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_FINISHED) {
      long durationLong = DurationTool.getSecondsDuration(computeRealDuration(operationOrder));
      operationOrder.setRealDuration(durationLong);
      Machine machine = operationOrder.getMachine();
      if (machine != null) {
        machine.setOperatingDuration(machine.getOperatingDuration() + durationLong);
      }
    }
  }

  /**
   * Compute the duration of operation order, then fill {@link OperationOrder#realDuration} with the
   * computed value.
   *
   * @param operationOrder
   */
  @Override
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
  @Override
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
   * @throws AxelorException
   */
  @Override
  @Transactional
  public OperationOrder setPlannedDates(
      OperationOrder operationOrder, LocalDateTime plannedStartDateT, LocalDateTime plannedEndDateT)
      throws AxelorException {

    if (operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_FINISHED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.OPERATION_ORDER_ALREADY_FINISHED));
    }

    operationOrder.setPlannedStartDateT(plannedStartDateT);
    operationOrder.setPlannedEndDateT(plannedEndDateT);

    List<OperationOrder> nextOperationOrders =
        getNextOrderedOperationOrders(operationOrder).stream()
            .filter(oo -> oo.getStatusSelect() != OperationOrderRepository.STATUS_FINISHED)
            .collect(Collectors.toList());

    for (OperationOrder oo : nextOperationOrders) {
      planPlannedDates(oo);
    }

    manufOrderService.updatePlannedDates(operationOrder.getManufOrder());
    return computeDuration(operationOrder);
  }

  /**
   * Set real start and end dates.
   *
   * @param operationOrder
   * @param realStartDateT
   * @param realEndDateT
   * @return
   * @throws AxelorException
   */
  @Override
  @Transactional
  public OperationOrder setRealDates(
      OperationOrder operationOrder, LocalDateTime realStartDateT, LocalDateTime realEndDateT)
      throws AxelorException {

    if (operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_FINISHED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.OPERATION_ORDER_ALREADY_FINISHED));
    }

    operationOrder.setRealStartDateT(realStartDateT);
    operationOrder.setRealEndDateT(realEndDateT);
    return computeDuration(operationOrder);
  }

  @Override
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

  @Override
  public long getDuration(OperationOrder operationOrder) throws AxelorException {
    if (operationOrder.getWorkCenter() != null) {
      return this.computeEntireCycleDuration(
          operationOrder, operationOrder.getManufOrder().getQty());
    }
    return 0;
  }

  @Override
  public long computeEntireCycleDuration(OperationOrder operationOrder, BigDecimal qty)
      throws AxelorException {
    ProdProcessLine prodProcessLine = operationOrder.getProdProcessLine();

    return prodProcessLineService.computeEntireCycleDuration(operationOrder, prodProcessLine, qty);
  }

  protected void calculateHoursOfUse(OperationOrder operationOrder) {

    if (operationOrder.getMachineTool() == null) {
      return;
    }

    long hoursOfUse =
        operationOrderRepo
            .all()
            .filter("self.machineTool.id = :id AND self.statusSelect = 6")
            .bind("id", operationOrder.getMachineTool().getId())
            .fetchStream()
            .mapToLong(OperationOrder::getRealDuration)
            .sum();

    MachineTool machineTool = machineToolRepo.find(operationOrder.getMachineTool().getId());
    machineTool.setHoursOfUse(hoursOfUse);
    machineToolRepo.save(machineTool);
  }
}
