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
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProductionConfig;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.db.repo.ProductionConfigRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.config.ProductionConfigService;
import com.axelor.apps.production.service.operationorder.OperationOrderPlanningService;
import com.axelor.apps.production.service.operationorder.OperationOrderService;
import com.axelor.apps.production.service.operationorder.OperationOrderWorkflowService;
import com.axelor.apps.production.service.productionorder.ProductionOrderService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class ManufOrderPlanServiceImpl implements ManufOrderPlanService {

  protected ManufOrderRepository manufOrderRepo;
  protected ManufOrderService manufOrderService;
  protected SequenceService sequenceService;
  protected OperationOrderRepository operationOrderRepo;
  protected OperationOrderWorkflowService operationOrderWorkflowService;
  protected OperationOrderPlanningService operationOrderPlanningService;
  protected OperationOrderService operationOrderService;
  protected ManufOrderStockMoveService manufOrderStockMoveService;
  protected ProductionOrderService productionOrderService;
  protected ProductionConfigService productionConfigService;
  protected AppBaseService appBaseService;
  protected AppProductionService appProductionService;
  protected ManufOrderCreatePurchaseOrderService manufOrderCreatePurchaseOrderService;

  @Inject
  public ManufOrderPlanServiceImpl(
      ManufOrderRepository manufOrderRepo,
      ManufOrderService manufOrderService,
      SequenceService sequenceService,
      OperationOrderRepository operationOrderRepo,
      OperationOrderWorkflowService operationOrderWorkflowService,
      OperationOrderPlanningService operationOrderPlanningService,
      OperationOrderService operationOrderService,
      ManufOrderStockMoveService manufOrderStockMoveService,
      ProductionOrderService productionOrderService,
      ProductionConfigService productionConfigService,
      AppBaseService appBaseService,
      AppProductionService appProductionService,
      ManufOrderCreatePurchaseOrderService manufOrderCreatePurchaseOrderService) {
    this.manufOrderRepo = manufOrderRepo;
    this.manufOrderService = manufOrderService;
    this.sequenceService = sequenceService;
    this.operationOrderRepo = operationOrderRepo;
    this.operationOrderWorkflowService = operationOrderWorkflowService;
    this.operationOrderPlanningService = operationOrderPlanningService;
    this.operationOrderService = operationOrderService;
    this.manufOrderStockMoveService = manufOrderStockMoveService;
    this.productionOrderService = productionOrderService;
    this.productionConfigService = productionConfigService;
    this.appBaseService = appBaseService;
    this.appProductionService = appProductionService;
    this.manufOrderCreatePurchaseOrderService = manufOrderCreatePurchaseOrderService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public String planManufOrders(List<ManufOrder> manufOrderList) throws AxelorException {

    StringBuilder messageBuilder = new StringBuilder();

    for (ManufOrder manufOrder : manufOrderList) {
      this.plan(manufOrder);
      if (!Strings.isNullOrEmpty(manufOrder.getMoCommentFromSaleOrder())) {
        messageBuilder.append(manufOrder.getMoCommentFromSaleOrder());
      }

      manufOrderCreatePurchaseOrderService.createPurchaseOrders(manufOrder);

      if (!Strings.isNullOrEmpty(manufOrder.getMoCommentFromSaleOrderLine())) {
        messageBuilder
            .append(System.lineSeparator())
            .append(manufOrder.getMoCommentFromSaleOrderLine());
      }
    }
    return messageBuilder.toString();
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public ManufOrder plan(ManufOrder manufOrder) throws AxelorException {

    initFieldsNeededForPlan(manufOrder);
    planSchedulingDates(manufOrder);
    updateStatusToPlan(manufOrder);

    return manufOrderRepo.save(manufOrder);
  }

  protected void initFieldsNeededForPlan(ManufOrder manufOrder) throws AxelorException {
    manufOrderService.checkApplicableManufOrder(manufOrder);
    if (sequenceService.isEmptyOrDraftSequenceNumber(manufOrder.getManufOrderSeq())) {
      manufOrder.setManufOrderSeq(manufOrderService.getManufOrderSeq(manufOrder));
    }
    manufOrderService.createBarcode(manufOrder);
    if (CollectionUtils.isEmpty(manufOrder.getOperationOrderList())) {
      manufOrderService.preFillOperations(manufOrder);
    } else {
      manufOrderService.updateOperationsName(manufOrder);
    }
    planProdProducts(manufOrder);
    planPlannedStartDateT(manufOrder);

    manufOrder.setRealStartDateT(null);
    manufOrder.setRealEndDateT(null);
  }

  protected void planSchedulingDates(ManufOrder manufOrder) throws AxelorException {
    planPlanningOperationOrders(manufOrder);
    // Updating plannedStartDate since, it may be different now that operation orders are
    // planned
    manufOrder.setPlannedStartDateT(this.computePlannedStartDateT(manufOrder));
    checkPlannedStartDateT(manufOrder);
    if (manufOrder.getPlannedEndDateT() == null) {
      manufOrder.setPlannedEndDateT(this.computePlannedEndDateT(manufOrder));
    }
  }

  protected void updateStatusToPlan(ManufOrder manufOrder) throws AxelorException {

    if (manufOrder.getBillOfMaterial() != null) {
      manufOrder.setUnit(manufOrder.getBillOfMaterial().getUnit());
    }

    planStockMoves(manufOrder);
    manufOrder.setStatusSelect(ManufOrderRepository.STATUS_PLANNED);
    manufOrder.setCancelReason(null);
    manufOrder.setCancelReasonStr(null);

    manufOrderRepo.save(manufOrder);
    productionOrderService.updateStatus(manufOrder.getProductionOrderSet());
  }

  protected void planStockMoves(ManufOrder manufOrder) throws AxelorException {
    if (!manufOrder.getIsConsProOnOperation()) {
      manufOrderStockMoveService
          .createAndPlanToConsumeStockMoveWithLines(manufOrder)
          .ifPresent(
              stockMove -> {
                manufOrder.addInStockMoveListItem(stockMove);
                // fill here the consumed stock move line list item to manage the
                // case where we had to split tracked stock move lines
                addToConsumedStockMoveLineList(manufOrder, stockMove);
              });
    }

    manufOrderStockMoveService
        .createAndPlanToProduceStockMoveWithLines(manufOrder)
        .ifPresent(
            sm -> {
              manufOrder.addOutStockMoveListItem(sm);
              addToProducedStockMoveLineList(manufOrder, sm);
            });
  }

  protected void addToProducedStockMoveLineList(ManufOrder manufOrder, StockMove stockMove) {
    if (stockMove.getStockMoveLineList() != null) {
      for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
        manufOrder.addProducedStockMoveLineListItem(stockMoveLine);
      }
    }
  }

  protected void addToConsumedStockMoveLineList(ManufOrder manufOrder, StockMove stockMove) {
    if (stockMove.getStockMoveLineList() != null) {
      for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
        manufOrder.addConsumedStockMoveLineListItem(stockMoveLine);
      }
    }
  }

  protected void checkPlannedStartDateT(ManufOrder manufOrder) throws AxelorException {
    ProductionConfig productionConfig =
        productionConfigService.getProductionConfig(manufOrder.getCompany());
    int qtyScale = appBaseService.getNbDecimalDigitForQty();
    LocalDateTime todayDateT =
        appBaseService.getTodayDateTime(manufOrder.getCompany()).toLocalDateTime();
    if (productionConfig.getScheduling() == ProductionConfigRepository.AT_THE_LATEST_SCHEDULING
        && manufOrder.getPlannedStartDateT().isBefore(todayDateT)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.PLAN_IS_BEFORE_TODAY_DATE),
          String.format(
              "%s %s",
              manufOrder.getQty() != null
                  ? manufOrder.getQty().setScale(qtyScale, RoundingMode.HALF_UP)
                  : null,
              manufOrder.getProduct().getFullName()));
    }
  }

  protected void planPlanningOperationOrders(ManufOrder manufOrder) throws AxelorException {
    List<OperationOrder> operationOrders = manufOrder.getOperationOrderList();
    if (CollectionUtils.isNotEmpty(operationOrders)) {
      operationOrderPlanningService.plan(operationOrders);
    }
  }

  protected void planPlannedStartDateT(ManufOrder manufOrder) throws AxelorException {
    if (manufOrder.getPlannedStartDateT() == null && manufOrder.getPlannedEndDateT() == null) {
      manufOrder.setPlannedStartDateT(appProductionService.getTodayDateTime().toLocalDateTime());
    } else if (manufOrder.getPlannedStartDateT() == null
        && manufOrder.getPlannedEndDateT() != null) {
      long duration = 0;
      for (OperationOrder order : manufOrder.getOperationOrderList()) {
        duration +=
            operationOrderService.computeEntireCycleDuration(
                order, order.getManufOrder().getQty()); // in seconds
      }
      // This is a estimation only, it will be updated later
      // It does not take into configuration such as machine planning etc...
      manufOrder.setPlannedStartDateT(manufOrder.getPlannedEndDateT().minusSeconds(duration));
    }
  }

  protected void planProdProducts(ManufOrder manufOrder) {
    if (!manufOrder.getIsConsProOnOperation()
        && CollectionUtils.isEmpty(manufOrder.getToConsumeProdProductList())) {
      manufOrderService.createToConsumeProdProductList(manufOrder);
    }

    if (CollectionUtils.isEmpty(manufOrder.getToProduceProdProductList())) {
      manufOrderService.createToProduceProdProductList(manufOrder);
    }
  }

  @Override
  public LocalDateTime computePlannedStartDateT(ManufOrder manufOrder) {

    OperationOrder firstOperationOrder = getFirstOperationOrder(manufOrder);

    if (firstOperationOrder != null) {

      return firstOperationOrder.getPlannedStartDateT();
    }

    return manufOrder.getPlannedStartDateT();
  }

  @Override
  public LocalDateTime computePlannedEndDateT(ManufOrder manufOrder) {

    OperationOrder lastOperationOrder = getLastOperationOrder(manufOrder);

    if (lastOperationOrder != null) {

      return lastOperationOrder.getPlannedEndDateT();
    }

    return manufOrder.getPlannedStartDateT();
  }

  /**
   * Returns first operation order (highest priority) of given {@link ManufOrder}
   *
   * @param manufOrder A manufacturing order
   * @return First operation order of {@code manufOrder}
   */
  protected OperationOrder getFirstOperationOrder(ManufOrder manufOrder) {
    return operationOrderRepo
        .all()
        .filter("self.manufOrder = ? AND self.plannedStartDateT IS NOT NULL", manufOrder)
        .order("plannedStartDateT")
        .fetchOne();
  }

  /**
   * Returns last operation order (highest priority) of given {@link ManufOrder}
   *
   * @param manufOrder A manufacturing order
   * @return Last operation order of {@code manufOrder}
   */
  protected OperationOrder getLastOperationOrder(ManufOrder manufOrder) {
    return operationOrderRepo
        .all()
        .filter("self.manufOrder = ? AND self.plannedEndDateT IS NOT NULL", manufOrder)
        .order("-plannedEndDateT")
        .fetchOne();
  }

  /**
   * Update planned dates.
   *
   * @param manufOrder
   * @param plannedStartDateT
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updatePlannedDates(ManufOrder manufOrder, LocalDateTime plannedStartDateT)
      throws AxelorException {
    manufOrder.setPlannedStartDateT(plannedStartDateT);

    List<OperationOrder> operationOrders = manufOrder.getOperationOrderList();
    if (operationOrders != null) {
      operationOrderWorkflowService.resetPlannedDates(operationOrders);
      operationOrderPlanningService.replan(operationOrders);
    }

    manufOrder.setPlannedEndDateT(computePlannedEndDateT(manufOrder));
  }
}
