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
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.MachineTool;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.OperationOrderDuration;
import com.axelor.apps.production.db.repo.MachineToolRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderDurationRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.db.repo.ProductionConfigRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderStockMoveService;
import com.axelor.apps.production.service.manuforder.ManufOrderWorkflowService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.auth.AuthUtils;
import com.axelor.inject.Beans;
import com.axelor.utils.date.DurationTool;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;

public class OperationOrderWorkflowServiceImpl implements OperationOrderWorkflowService {
  protected OperationOrderStockMoveService operationOrderStockMoveService;
  protected OperationOrderRepository operationOrderRepo;
  protected OperationOrderDurationRepository operationOrderDurationRepo;
  protected AppProductionService appProductionService;
  protected MachineToolRepository machineToolRepo;
  protected ManufOrderWorkflowService manufOrderWorkflowService;
  protected OperationOrderService operationOrderService;
  protected OperationOrderPlanningService operationOrderPlanningService;

  @Inject
  public OperationOrderWorkflowServiceImpl(
      OperationOrderStockMoveService operationOrderStockMoveService,
      OperationOrderRepository operationOrderRepo,
      OperationOrderDurationRepository operationOrderDurationRepo,
      AppProductionService appProductionService,
      MachineToolRepository machineToolRepo,
      ManufOrderWorkflowService manufOrderWorkflowService,
      OperationOrderService operationOrderService,
      OperationOrderPlanningService operationOrderPlanningService) {
    this.operationOrderStockMoveService = operationOrderStockMoveService;
    this.operationOrderRepo = operationOrderRepo;
    this.operationOrderDurationRepo = operationOrderDurationRepo;
    this.appProductionService = appProductionService;
    this.machineToolRepo = machineToolRepo;
    this.manufOrderWorkflowService = manufOrderWorkflowService;
    this.operationOrderService = operationOrderService;
    this.operationOrderPlanningService = operationOrderPlanningService;
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

  /**
   * Plans the given {@link OperationOrder} and sets its planned dates
   *
   * @param operationOrder An operation order
   */
  @Override
  @Transactional
  public void plan(OperationOrder operationOrder) throws AxelorException {
    operationOrderPlanningService.plan(operationOrder, null);
  }

  /**
   * re-plans the given {@link OperationOrder} and sets its planned dates
   *
   * @param operationOrder An operation order
   */
  @Override
  @Transactional
  public void replan(OperationOrder operationOrder) throws AxelorException {
    operationOrderPlanningService.replan(operationOrder);
  }

  /**
   * Starts the given {@link OperationOrder} and sets its starting time
   *
   * @param operationOrder An operation order
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void start(OperationOrder operationOrder) throws AxelorException {

    if (operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_IN_PROGRESS
        || !canStartOperationOrder(operationOrder)) {
      return;
    }

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
  @Override
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
  @Override
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
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void finish(OperationOrder operationOrder) throws AxelorException {
    operationOrder.setStatusSelect(OperationOrderRepository.STATUS_FINISHED);
    operationOrder.setRealEndDateT(appProductionService.getTodayDateTime().toLocalDateTime());

    stopOperationOrderDuration(operationOrder);

    operationOrderStockMoveService.finish(operationOrder);
    operationOrderRepo.save(operationOrder);
    calculateHoursOfUse(operationOrder);
    manufOrderWorkflowService.setOperationOrderMaxPriority(operationOrder.getManufOrder());
  }

  @Override
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
    manufOrderWorkflowService.setOperationOrderMaxPriority(operationOrder.getManufOrder());
  }

  /**
   * Starts an {@link OperationOrderDuration} and links it to the given {@link OperationOrder}
   *
   * @param operationOrder An operation order
   */
  @Override
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
  @Override
  public void stopOperationOrderDuration(OperationOrder operationOrder) {
    OperationOrderDuration duration =
        operationOrderDurationRepo
            .all()
            .filter(
                "self.operationOrder.id = ? AND self.stoppedBy IS NULL AND self.stoppingDateTime IS NULL",
                operationOrder.getId())
            .fetchOne();

    if (duration != null) {
      duration.setStoppedBy(AuthUtils.getUser());
      duration.setStoppingDateTime(appProductionService.getTodayDateTime().toLocalDateTime());
    }

    if (operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_FINISHED) {
      long durationLong =
          DurationTool.getSecondsDuration(
              operationOrderService.computeRealDuration(operationOrder));
      operationOrder.setRealDuration(durationLong);
      Machine machine = operationOrder.getMachine();
      if (machine != null) {
        machine.setOperatingDuration(machine.getOperatingDuration() + durationLong);
      }
    }

    if (duration != null) {
      operationOrderDurationRepo.save(duration);
    }
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

  @Override
  public boolean canStartOperationOrder(OperationOrder operationOrder) {
    Integer priority = operationOrder.getPriority();
    Boolean isOptional = operationOrder.getProdProcessLine().getOptional();
    ManufOrder manufOrder = operationOrder.getManufOrder();
    Integer operationOrderMaxPriority = manufOrder.getOperationOrderMaxPriority();

    return Boolean.FALSE.equals(manufOrder.getProdProcess().getOperationContinuity())
        || (!isOptional && priority.equals(operationOrderMaxPriority))
        || (isOptional && priority.compareTo(operationOrderMaxPriority) < 0);
  }
}
