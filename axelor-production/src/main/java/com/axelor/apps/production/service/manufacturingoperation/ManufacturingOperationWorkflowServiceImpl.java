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
package com.axelor.apps.production.service.manufacturingoperation;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.MachineTool;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ManufacturingOperation;
import com.axelor.apps.production.db.ManufacturingOperationDuration;
import com.axelor.apps.production.db.repo.MachineToolRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.ManufacturingOperationDurationRepository;
import com.axelor.apps.production.db.repo.ManufacturingOperationRepository;
import com.axelor.apps.production.db.repo.ProductionConfigRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderStockMoveService;
import com.axelor.apps.production.service.manuforder.ManufOrderWorkflowService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import com.axelor.utils.helpers.date.DurationHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManufacturingOperationWorkflowServiceImpl
    implements ManufacturingOperationWorkflowService {
  protected ManufacturingOperationStockMoveService manufacturingOperationStockMoveService;
  protected ManufacturingOperationRepository manufacturingOperationRepo;
  protected ManufacturingOperationDurationRepository manufacturingOperationDurationRepo;
  protected AppProductionService appProductionService;
  protected MachineToolRepository machineToolRepo;
  protected ManufOrderWorkflowService manufOrderWorkflowService;
  protected ManufOrderStockMoveService manufOrderStockMoveService;
  protected ManufacturingOperationService manufacturingOperationService;
  protected ManufacturingOperationPlanningService manufacturingOperationPlanningService;

  @Inject
  public ManufacturingOperationWorkflowServiceImpl(
      ManufacturingOperationStockMoveService manufacturingOperationStockMoveService,
      ManufacturingOperationRepository manufacturingOperationRepo,
      ManufacturingOperationDurationRepository manufacturingOperationDurationRepo,
      AppProductionService appProductionService,
      MachineToolRepository machineToolRepo,
      ManufOrderWorkflowService manufOrderWorkflowService,
      ManufacturingOperationService manufacturingOperationService,
      ManufacturingOperationPlanningService manufacturingOperationPlanningService,
      ManufOrderStockMoveService manufOrderStockMoveService) {
    this.manufacturingOperationStockMoveService = manufacturingOperationStockMoveService;
    this.manufacturingOperationRepo = manufacturingOperationRepo;
    this.manufacturingOperationDurationRepo = manufacturingOperationDurationRepo;
    this.appProductionService = appProductionService;
    this.machineToolRepo = machineToolRepo;
    this.manufOrderWorkflowService = manufOrderWorkflowService;
    this.manufacturingOperationService = manufacturingOperationService;
    this.manufacturingOperationPlanningService = manufacturingOperationPlanningService;
    this.manufOrderStockMoveService = manufOrderStockMoveService;
  }

  /**
   * Reset the planned dates from the specified operation order list.
   *
   * @param manufacturingOperationList
   * @return
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public List<ManufacturingOperation> resetPlannedDates(
      List<ManufacturingOperation> manufacturingOperationList) {
    for (ManufacturingOperation manufacturingOperation : manufacturingOperationList) {
      manufacturingOperation.setPlannedStartDateT(null);
      manufacturingOperation.setPlannedEndDateT(null);
      manufacturingOperation.setPlannedDuration(null);
    }

    return manufacturingOperationList;
  }

  /**
   * Plans the given {@link ManufacturingOperation} and sets its planned dates
   *
   * @param manufacturingOperation An operation order
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void plan(ManufacturingOperation manufacturingOperation) throws AxelorException {
    manufacturingOperationPlanningService.plan(List.of(manufacturingOperation));
  }

  /**
   * re-plans the given {@link ManufacturingOperation} and sets its planned dates
   *
   * @param manufacturingOperation An operation order
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void replan(ManufacturingOperation manufacturingOperation) throws AxelorException {
    manufacturingOperationPlanningService.replan(List.of(manufacturingOperation));
  }

  /**
   * Starts the given {@link ManufacturingOperation} and sets its starting time
   *
   * @param manufacturingOperation An operation order
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void start(ManufacturingOperation manufacturingOperation) throws AxelorException {

    if (manufacturingOperation.getStatusSelect()
        == ManufacturingOperationRepository.STATUS_IN_PROGRESS) {
      startManufacturingOperationDuration(manufacturingOperation, AuthUtils.getUser());
    } else {
      start(manufacturingOperation, AuthUtils.getUser());
    }
    manufacturingOperationRepo.save(manufacturingOperation);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void start(ManufacturingOperation manufacturingOperation, User user)
      throws AxelorException {

    if (manufacturingOperation.getStatusSelect()
            == ManufacturingOperationRepository.STATUS_IN_PROGRESS
        || !canStartManufacturingOperation(manufacturingOperation)) {
      return;
    }

    manufacturingOperation.setStatusSelect(ManufacturingOperationRepository.STATUS_IN_PROGRESS);
    manufacturingOperation.setRealStartDateT(
        appProductionService.getTodayDateTime().toLocalDateTime());

    startManufacturingOperationDuration(manufacturingOperation, user);

    if (manufacturingOperation.getManufOrder() != null) {
      int beforeOrAfterConfig =
          manufacturingOperation.getManufOrder().getProdProcess().getStockMoveRealizeOrderSelect();
      if (beforeOrAfterConfig == ProductionConfigRepository.REALIZE_START) {
        for (StockMove stockMove : manufacturingOperation.getInStockMoveList()) {
          manufOrderStockMoveService.finishStockMove(stockMove);
        }

        StockMove newStockMove =
            manufacturingOperationStockMoveService._createToConsumeStockMove(
                manufacturingOperation, manufacturingOperation.getManufOrder().getCompany());
        newStockMove.setStockMoveLineList(new ArrayList<>());
        Beans.get(StockMoveService.class).plan(newStockMove);
        manufacturingOperation.addInStockMoveListItem(newStockMove);
      }
    }
    manufacturingOperationRepo.save(manufacturingOperation);

    if (manufacturingOperation.getManufOrder().getStatusSelect()
        != ManufOrderRepository.STATUS_IN_PROGRESS) {
      manufOrderWorkflowService.start(manufacturingOperation.getManufOrder());
    }
  }

  /**
   * Pauses the given {@link ManufacturingOperation} and sets its pausing time
   *
   * @param manufacturingOperation An operation order
   * @throws AxelorException
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void pause(ManufacturingOperation manufacturingOperation) throws AxelorException {

    manufacturingOperation.setStatusSelect(ManufacturingOperationRepository.STATUS_STANDBY);

    stopManufacturingOperationDuration(manufacturingOperation);

    pauseManufOrder(manufacturingOperation);
    manufacturingOperationRepo.save(manufacturingOperation);
  }

  protected void pauseManufOrder(ManufacturingOperation manufacturingOperation) {
    ManufOrder manufOrder = manufacturingOperation.getManufOrder();
    if (manufOrder.getManufacturingOperationList().stream()
        .allMatch(
            order ->
                order.getStatusSelect() != ManufacturingOperationRepository.STATUS_IN_PROGRESS)) {
      manufOrder.setStatusSelect(ManufOrderRepository.STATUS_STANDBY);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void pause(ManufacturingOperation manufacturingOperation, User user)
      throws AxelorException {

    stopManufacturingOperationDuration(manufacturingOperation, AuthUtils.getUser());

    // All operations orders duration are stopped
    if (allOperationDurationAreStopped(manufacturingOperation)) {
      manufacturingOperation.setStatusSelect(ManufacturingOperationRepository.STATUS_STANDBY);
    }
    pauseManufOrder(manufacturingOperation);
    manufacturingOperationRepo.save(manufacturingOperation);
  }

  /**
   * Resumes the given {@link ManufacturingOperation} and sets its resuming time
   *
   * @param manufacturingOperation An operation order
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void resume(ManufacturingOperation manufacturingOperation) {
    manufacturingOperation.setStatusSelect(ManufacturingOperationRepository.STATUS_IN_PROGRESS);

    startManufacturingOperationDuration(manufacturingOperation, AuthUtils.getUser());
    manufacturingOperation.getManufOrder().setStatusSelect(ManufOrderRepository.STATUS_IN_PROGRESS);
    manufacturingOperationRepo.save(manufacturingOperation);
  }

  /**
   * Ends the given {@link ManufacturingOperation} and sets its stopping time<br>
   * Realizes the linked stock moves
   *
   * @param manufacturingOperation An operation order
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void finish(ManufacturingOperation manufacturingOperation) throws AxelorException {
    manufacturingOperation.setStatusSelect(ManufacturingOperationRepository.STATUS_FINISHED);
    manufacturingOperation.setRealEndDateT(
        appProductionService.getTodayDateTime().toLocalDateTime());

    stopManufacturingOperationDuration(manufacturingOperation);

    manufacturingOperationStockMoveService.finish(manufacturingOperation);
    manufacturingOperationRepo.save(manufacturingOperation);
    calculateHoursOfUse(manufacturingOperation);
    manufOrderWorkflowService.setManufacturingOperationMaxPriority(
        manufacturingOperation.getManufOrder());
  }

  /**
   * Ends the given {@link ManufacturingOperation} and sets its stopping time<br>
   * Realizes the linked stock moves
   *
   * @param manufacturingOperation An operation order
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void finish(ManufacturingOperation manufacturingOperation, User user)
      throws AxelorException {

    stopManufacturingOperationDuration(manufacturingOperation, user);

    // All operations orders duration are stopped
    if (allOperationDurationAreStopped(manufacturingOperation)) {

      manufacturingOperation.setStatusSelect(ManufacturingOperationRepository.STATUS_FINISHED);
      computeFinishDuration(manufacturingOperation);
      manufacturingOperation.setRealEndDateT(
          appProductionService.getTodayDateTime().toLocalDateTime());
      manufacturingOperationStockMoveService.finish(manufacturingOperation);
      manufacturingOperationRepo.save(manufacturingOperation);
      calculateHoursOfUse(manufacturingOperation);
      return;
    }

    manufacturingOperationRepo.save(manufacturingOperation);
  }

  protected boolean allOperationDurationAreStopped(ManufacturingOperation manufacturingOperation) {
    return manufacturingOperation.getManufacturingOperationDurationList().stream()
        .allMatch(oo -> oo.getStoppingDateTime() != null);
  }

  @Override
  public void finishAndAllOpFinished(ManufacturingOperation manufacturingOperation)
      throws AxelorException {
    ManufOrder manufOrder = manufacturingOperation.getManufOrder();
    finishProcess(manufacturingOperation);
    manufOrderWorkflowService.sendFinishedMail(manufOrder);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void finishProcess(ManufacturingOperation manufacturingOperation)
      throws AxelorException {
    finish(manufacturingOperation);
    manufOrderWorkflowService.allOpFinished(manufacturingOperation.getManufOrder());
  }

  /**
   * Cancels the given {@link ManufacturingOperation} and its linked stock moves And sets its
   * stopping time
   *
   * @param manufacturingOperation An operation order
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void cancel(ManufacturingOperation manufacturingOperation) throws AxelorException {
    int oldStatus = manufacturingOperation.getStatusSelect();
    manufacturingOperation.setStatusSelect(ManufacturingOperationRepository.STATUS_CANCELED);

    if (oldStatus == ManufacturingOperationRepository.STATUS_IN_PROGRESS) {
      stopManufacturingOperationDuration(manufacturingOperation);
    }
    if (manufacturingOperation.getConsumedStockMoveLineList() != null) {
      manufacturingOperation
          .getConsumedStockMoveLineList()
          .forEach(stockMoveLine -> stockMoveLine.setConsumedManufacturingOperation(null));
    }
    manufacturingOperationStockMoveService.cancel(manufacturingOperation);

    manufacturingOperationRepo.save(manufacturingOperation);
    manufOrderWorkflowService.setManufacturingOperationMaxPriority(
        manufacturingOperation.getManufOrder());
  }

  /**
   * Starts an {@link ManufacturingOperationDuration} and links it to the given {@link
   * ManufacturingOperation}
   *
   * @param manufacturingOperation An operation order
   */
  @Override
  public void startManufacturingOperationDuration(ManufacturingOperation manufacturingOperation) {
    startManufacturingOperationDuration(manufacturingOperation, AuthUtils.getUser());
  }

  protected void startManufacturingOperationDuration(
      ManufacturingOperation manufacturingOperation, User user) {

    if (manufacturingOperation.getManufacturingOperationDurationList() != null
        && manufacturingOperation.getManufacturingOperationDurationList().stream()
            .noneMatch(
                ood -> ood.getStartedBy().equals(user) && ood.getStoppingDateTime() == null)) {
      ManufacturingOperationDuration duration = new ManufacturingOperationDuration();
      duration.setStartedBy(user);
      duration.setStartingDateTime(appProductionService.getTodayDateTime().toLocalDateTime());
      manufacturingOperation.addManufacturingOperationDurationListItem(duration);
    }
  }

  /**
   * Ends every operationDuration of operation order and sets the real duration of {@code
   * manufacturingOperation}<br>
   * Adds the real duration to the {@link Machine} linked to {@code manufacturingOperation}
   *
   * @param manufacturingOperation An operation order
   * @throws AxelorException
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void stopManufacturingOperationDuration(ManufacturingOperation manufacturingOperation)
      throws AxelorException {

    stopAllManufacturingOperationDuration(manufacturingOperation);
  }

  protected void stopAllManufacturingOperationDuration(
      ManufacturingOperation manufacturingOperation) throws AxelorException {
    if (manufacturingOperation.getManufacturingOperationDurationList() != null) {
      for (ManufacturingOperationDuration ood :
          manufacturingOperation.getManufacturingOperationDurationList()) {
        if (ood.getStoppingDateTime() == null) {
          stopManufacturingOperationDuration(ood);
        }
      }
    }
  }

  @Override
  public void stopManufacturingOperationDuration(
      ManufacturingOperation manufacturingOperation, User user) throws AxelorException {

    Map<String, Object> bindingMap = new HashMap<>();
    StringBuilder manufacturingOperationFilter =
        new StringBuilder(
            "self.manufacturingOperation.id = :manufacturingOperationId AND self.stoppedBy IS NULL AND self.stoppingDateTime IS NULL");
    bindingMap.put("manufacturingOperationId", manufacturingOperation.getId());

    if (user != null) {
      manufacturingOperationFilter.append(" AND self.startedBy = :currentUser");
      bindingMap.put("currentUser", user);
    }

    ManufacturingOperationDuration duration =
        manufacturingOperationDurationRepo
            .all()
            .filter(manufacturingOperationFilter.toString())
            .bind(bindingMap)
            .fetchOne();

    stopManufacturingOperationDuration(duration);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void stopManufacturingOperationDuration(ManufacturingOperationDuration duration)
      throws AxelorException {
    if (duration != null) {
      duration.setStoppedBy(AuthUtils.getUser());
      duration.setStoppingDateTime(appProductionService.getTodayDateTime().toLocalDateTime());
      manufacturingOperationDurationRepo.save(duration);
    }
  }

  protected void computeFinishDuration(ManufacturingOperation manufacturingOperation) {
    if (manufacturingOperation.getStatusSelect()
        == ManufacturingOperationRepository.STATUS_FINISHED) {
      long durationLong =
          DurationHelper.getSecondsDuration(
              manufacturingOperationService.computeRealDuration(manufacturingOperation));
      manufacturingOperation.setRealDuration(durationLong);
      Machine machine = manufacturingOperation.getMachine();
      if (machine != null) {
        machine.setOperatingDuration(machine.getOperatingDuration() + durationLong);
      }
    }
  }

  protected void calculateHoursOfUse(ManufacturingOperation manufacturingOperation) {

    if (manufacturingOperation.getMachineTool() == null) {
      return;
    }

    long hoursOfUse =
        manufacturingOperationRepo
            .all()
            .filter("self.machineTool.id = :id AND self.statusSelect = 6")
            .bind("id", manufacturingOperation.getMachineTool().getId())
            .fetchStream()
            .mapToLong(ManufacturingOperation::getRealDuration)
            .sum();

    MachineTool machineTool = machineToolRepo.find(manufacturingOperation.getMachineTool().getId());
    machineTool.setHoursOfUse(hoursOfUse);
    machineToolRepo.save(machineTool);
  }

  @Override
  public boolean canStartManufacturingOperation(ManufacturingOperation manufacturingOperation) {
    Integer priority = manufacturingOperation.getPriority();
    Boolean isOptional = manufacturingOperation.getProdProcessLine().getOptional();
    ManufOrder manufOrder = manufacturingOperation.getManufOrder();
    Integer manufacturingOperationMaxPriority = manufOrder.getManufacturingOperationMaxPriority();

    return Boolean.FALSE.equals(manufOrder.getProdProcess().getOperationContinuity())
        || (!isOptional && priority.equals(manufacturingOperationMaxPriority))
        || (isOptional && priority.compareTo(manufacturingOperationMaxPriority) < 0);
  }
}
