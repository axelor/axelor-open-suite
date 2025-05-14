/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.MachineTool;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.OperationOrderDuration;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.ProdProcessLineComputationService;
import com.axelor.apps.production.service.ProdProcessLineService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderCheckStockMoveLineService;
import com.axelor.apps.production.service.manuforder.ManufOrderPlanStockMoveService;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.production.service.manuforder.ManufOrderStockMoveService;
import com.axelor.apps.production.service.manuforder.ManufOrderUpdateStockMoveService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationOrderServiceImpl implements OperationOrderService {

  protected AppProductionService appProductionService;
  protected ManufOrderStockMoveService manufOrderStockMoveService;
  protected ProdProcessLineService prodProcessLineService;
  protected OperationOrderRepository operationOrderRepository;
  protected OperationOrderOutsourceService operationOrderOutsourceService;
  protected ManufOrderCheckStockMoveLineService manufOrderCheckStockMoveLineService;
  protected ManufOrderPlanStockMoveService manufOrderPlanStockMoveService;
  protected ManufOrderUpdateStockMoveService manufOrderUpdateStockMoveService;
  protected final ProdProcessLineComputationService prodProcessLineComputationService;

  @Inject
  public OperationOrderServiceImpl(
      AppProductionService appProductionService,
      ManufOrderStockMoveService manufOrderStockMoveService,
      ProdProcessLineService prodProcessLineService,
      OperationOrderRepository operationOrderRepository,
      OperationOrderOutsourceService operationOrderOutsourceService,
      ManufOrderCheckStockMoveLineService manufOrderCheckStockMoveLineService,
      ManufOrderPlanStockMoveService manufOrderPlanStockMoveService,
      ManufOrderUpdateStockMoveService manufOrderUpdateStockMoveService,
      ProdProcessLineComputationService prodProcessLineComputationService) {
    this.appProductionService = appProductionService;
    this.manufOrderStockMoveService = manufOrderStockMoveService;
    this.prodProcessLineService = prodProcessLineService;
    this.operationOrderRepository = operationOrderRepository;
    this.operationOrderOutsourceService = operationOrderOutsourceService;
    this.manufOrderCheckStockMoveLineService = manufOrderCheckStockMoveLineService;
    this.manufOrderPlanStockMoveService = manufOrderPlanStockMoveService;
    this.manufOrderUpdateStockMoveService = manufOrderUpdateStockMoveService;
    this.prodProcessLineComputationService = prodProcessLineComputationService;
  }

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Transactional(rollbackOn = {Exception.class})
  public OperationOrder createOperationOrder(ManufOrder manufOrder, ProdProcessLine prodProcessLine)
      throws AxelorException {

    if (prodProcessLine.getWorkCenter() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.PROD_PROCESS_LINE_MISSING_WORK_CENTER),
          prodProcessLine.getProdProcess() != null
              ? prodProcessLine.getProdProcess().getCode()
              : "null",
          prodProcessLine.getName());
    }
    OperationOrder operationOrder =
        this.createOperationOrder(
            manufOrder,
            prodProcessLine.getPriority(),
            prodProcessLine.getWorkCenter(),
            prodProcessLine.getWorkCenter().getMachine(),
            prodProcessLine.getMachineTool(),
            prodProcessLine);

    return Beans.get(OperationOrderRepository.class).save(operationOrder);
  }

  @Transactional
  public OperationOrder createOperationOrder(
      ManufOrder manufOrder,
      int priority,
      WorkCenter workCenter,
      Machine machine,
      MachineTool machineTool,
      ProdProcessLine prodProcessLine)
      throws AxelorException {

    logger.debug(
        "Creation of an operation {} for the manufacturing order {}",
        priority,
        manufOrder.getManufOrderSeq());

    String operationName = prodProcessLine.getName();

    OperationOrder operationOrder =
        new OperationOrder(
            priority,
            this.computeName(manufOrder, priority, operationName),
            operationName,
            manufOrder,
            workCenter,
            machine,
            OperationOrderRepository.STATUS_DRAFT,
            prodProcessLine,
            machineTool);

    operationOrder.setOutsourcing(manufOrder.getOutsourcing() || prodProcessLine.getOutsourcing());
    operationOrder.setOutsourcingPartner(
        operationOrderOutsourceService.getOutsourcePartner(operationOrder).orElse(null));

    return Beans.get(OperationOrderRepository.class).save(operationOrder);
  }

  public String computeName(ManufOrder manufOrder, int priority, String operationName) {

    String name = "";
    if (manufOrder != null) {

      if (manufOrder.getManufOrderSeq() != null) {
        name += manufOrder.getManufOrderSeq();
      } else {
        name += manufOrder.getId();
      }
    }

    name += "-" + priority + "-" + operationName;

    return name;
  }

  @Override
  public void createToConsumeProdProductList(OperationOrder operationOrder) throws AxelorException {

    BigDecimal manufOrderQty = operationOrder.getManufOrder().getQty();
    BigDecimal bomQty = operationOrder.getManufOrder().getBillOfMaterial().getQty();
    ProdProcessLine prodProcessLine = operationOrder.getProdProcessLine();

    if (prodProcessLine == null) {

      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(ProductionExceptionMessage.PRODUCTION_PROCESS_IS_EMPTY));
    }
    if (prodProcessLine.getToConsumeProdProductList() != null) {
      for (ProdProduct prodProduct : prodProcessLine.getToConsumeProdProductList()) {

        BigDecimal qty =
            Beans.get(ManufOrderService.class)
                .computeToConsumeProdProductLineQuantity(
                    bomQty, manufOrderQty, prodProduct.getQty());

        operationOrder.addToConsumeProdProductListItem(
            new ProdProduct(prodProduct.getProduct(), qty, prodProduct.getUnit()));
      }
    }
  }

  @Override
  public OperationOrder updateDiffProdProductList(OperationOrder operationOrder)
      throws AxelorException {
    List<ProdProduct> toConsumeList = operationOrder.getToConsumeProdProductList();
    List<StockMoveLine> consumedList = operationOrder.getConsumedStockMoveLineList();
    if (toConsumeList == null || consumedList == null) {
      return operationOrder;
    }
    List<ProdProduct> diffConsumeList =
        createDiffProdProductList(operationOrder, toConsumeList, consumedList);

    operationOrder.clearDiffConsumeProdProductList();
    diffConsumeList.forEach(operationOrder::addDiffConsumeProdProductListItem);
    return operationOrder;
  }

  @Override
  public List<ProdProduct> createDiffProdProductList(
      OperationOrder operationOrder,
      List<ProdProduct> prodProductList,
      List<StockMoveLine> stockMoveLineList)
      throws AxelorException {
    List<ProdProduct> diffConsumeList =
        Beans.get(ManufOrderService.class)
            .createDiffProdProductList(prodProductList, stockMoveLineList);
    diffConsumeList.forEach(
        prodProduct -> prodProduct.setDiffConsumeOperationOrder(operationOrder));
    return diffConsumeList;
  }

  @Override
  public void checkConsumedStockMoveLineList(
      OperationOrder operationOrder, OperationOrder oldOperationOrder) throws AxelorException {
    manufOrderCheckStockMoveLineService.checkRealizedStockMoveLineList(
        operationOrder.getConsumedStockMoveLineList(),
        oldOperationOrder.getConsumedStockMoveLineList());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateConsumedStockMoveFromOperationOrder(OperationOrder operationOrder)
      throws AxelorException {
    this.updateDiffProdProductList(operationOrder);
    ManufOrder manufOrder = operationOrder.getManufOrder();
    Company company = manufOrder.getCompany();
    List<StockMoveLine> consumedStockMoveLineList = operationOrder.getConsumedStockMoveLineList();
    StockLocation fromStockLocation =
        manufOrderStockMoveService.getFromStockLocationForConsumedStockMove(manufOrder, company);
    StockLocation virtualStockLocation =
        manufOrderStockMoveService.getVirtualStockLocationForConsumedStockMove(manufOrder, company);
    if (consumedStockMoveLineList == null) {
      return;
    }
    Optional<StockMove> stockMoveOpt =
        operationOrder.getInStockMoveList().stream()
            .filter(stockMove -> stockMove.getStatusSelect() == StockMoveRepository.STATUS_PLANNED)
            .findFirst();
    StockMove stockMove;
    if (stockMoveOpt.isPresent()) {
      stockMove = stockMoveOpt.get();
    } else {
      stockMove =
          manufOrderPlanStockMoveService
              .createAndPlanToConsumeStockMove(manufOrder)
              .map(
                  sm -> {
                    operationOrder.addInStockMoveListItem(sm);
                    return sm;
                  })
              .orElse(null);
    }

    Beans.get(ManufOrderUpdateStockMoveService.class)
        .updateStockMoveFromManufOrder(consumedStockMoveLineList, stockMove);
  }

  @Override
  public long computeEntireCycleDuration(OperationOrder operationOrder, BigDecimal qty)
      throws AxelorException {
    ProdProcessLine prodProcessLine = operationOrder.getProdProcessLine();

    return prodProcessLineComputationService.computeEntireCycleDuration(
        operationOrder, prodProcessLine, qty);
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

  @Override
  public LocalDateTime getNextOperationDate(OperationOrder operationOrder) {
    ManufOrder manufOrder = operationOrder.getManufOrder();
    OperationOrder nextOperationOrder =
        operationOrderRepository
            .all()
            .filter(
                "self.manufOrder = :manufOrder AND self.priority >= :priority AND self.statusSelect BETWEEN :statusPlanned AND :statusStandby AND self.id != :operationOrderId")
            .bind("manufOrder", manufOrder)
            .bind("priority", operationOrder.getPriority())
            .bind("statusPlanned", OperationOrderRepository.STATUS_PLANNED)
            .bind("statusStandby", OperationOrderRepository.STATUS_STANDBY)
            .bind("operationOrderId", operationOrder.getId())
            .order("priority")
            .order("plannedStartDateT")
            .fetchOne();

    LocalDateTime manufOrderPlannedEndDateT = manufOrder.getPlannedEndDateT();
    if (nextOperationOrder == null) {
      return manufOrderPlannedEndDateT;
    }

    LocalDateTime plannedStartDateT =
        computePlannedStartTimeForNextOperationDate(operationOrder, nextOperationOrder);

    if (Objects.equals(nextOperationOrder.getPriority(), operationOrder.getPriority())) {
      LocalDateTime plannedEndDateT =
          computePlannedEndTimeForNextOperationDate(operationOrder, nextOperationOrder);

      if (plannedEndDateT != null && plannedEndDateT.isBefore(manufOrderPlannedEndDateT)) {
        if (operationOrder.getOutsourcing()) {
          return plannedEndDateT;
        }
        boolean isOnSameMachine =
            Objects.equals(nextOperationOrder.getMachine(), operationOrder.getMachine());
        return isOnSameMachine ? plannedStartDateT : plannedEndDateT;
      }

    } else if (plannedStartDateT != null && plannedStartDateT.isBefore(manufOrderPlannedEndDateT)) {
      return plannedStartDateT;
    }

    return manufOrderPlannedEndDateT;
  }

  @Override
  public LocalDateTime getLastOperationDate(OperationOrder operationOrder) {
    ManufOrder manufOrder = operationOrder.getManufOrder();
    OperationOrder lastOperationOrder =
        operationOrderRepository
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
            .order("-plannedEndWithWaitingDateT")
            .fetchOne();

    LocalDateTime manufOrderPlannedStartDateT = manufOrder.getPlannedStartDateT();
    if (lastOperationOrder == null) {
      return manufOrderPlannedStartDateT;
    }

    LocalDateTime plannedEndDateT = lastOperationOrder.getPlannedEndWithWaitingDateT();

    if (Objects.equals(lastOperationOrder.getPriority(), operationOrder.getPriority())) {
      LocalDateTime plannedStartDateT = lastOperationOrder.getPlannedStartDateT();
      if (plannedStartDateT != null && plannedStartDateT.isAfter(manufOrderPlannedStartDateT)) {
        if (operationOrder.getOutsourcing()) {
          return plannedStartDateT;
        }
        boolean isOnSameMachine =
            Objects.equals(lastOperationOrder.getMachine(), operationOrder.getMachine());
        return isOnSameMachine ? plannedEndDateT : plannedStartDateT;
      }

    } else if (plannedEndDateT != null && plannedEndDateT.isAfter(manufOrderPlannedStartDateT)) {
      return plannedEndDateT;
    }

    return manufOrderPlannedStartDateT;
  }

  protected LocalDateTime computePlannedStartTimeForNextOperationDate(
      OperationOrder operationOrder, OperationOrder nextOperationOrder) {
    if (operationOrder.getProdProcessLine() == null) {
      return nextOperationOrder.getPlannedStartDateT();
    }
    return nextOperationOrder
        .getPlannedStartDateT()
        .minusSeconds(operationOrder.getProdProcessLine().getTimeBeforeNextOperation());
  }

  protected LocalDateTime computePlannedEndTimeForNextOperationDate(
      OperationOrder operationOrder, OperationOrder nextOperationOrder) {
    if (operationOrder.getProdProcessLine() == null) {
      return nextOperationOrder.getPlannedEndDateT();
    }
    return nextOperationOrder
        .getPlannedEndDateT()
        .minusSeconds(
            operationOrder.getProdProcessLine().getTimeBeforeNextOperation()
                - nextOperationOrder.getProdProcessLine().getTimeBeforeNextOperation());
  }

  @Override
  public long getDuration(OperationOrder operationOrder) throws AxelorException {
    if (operationOrder.getWorkCenter() != null) {
      return computeEntireCycleDuration(operationOrder, operationOrder.getManufOrder().getQty());
    }
    return 0;
  }

  /**
   * Sort operationOrders list by priority and id.
   *
   * @param operationOrders
   * @return
   */
  @Override
  public List<OperationOrder> getSortedOperationOrderList(List<OperationOrder> operationOrders) {

    Comparator<OperationOrder> byPriority =
        Comparator.comparing(
            OperationOrder::getPriority, Comparator.nullsFirst(Comparator.naturalOrder()));
    Comparator<OperationOrder> byId =
        Comparator.comparing(
            OperationOrder::getId, Comparator.nullsFirst(Comparator.naturalOrder()));

    return operationOrders.stream()
        .sorted(byPriority.thenComparing(byId))
        .collect(Collectors.toList());
  }

  /**
   * Reverse sort operationOrders list by priority and id.
   *
   * @param operationOrders
   * @return
   */
  @Override
  public List<OperationOrder> getReversedSortedOperationOrderList(
      List<OperationOrder> operationOrders) {

    return Lists.reverse(getSortedOperationOrderList(operationOrders));
  }
}
