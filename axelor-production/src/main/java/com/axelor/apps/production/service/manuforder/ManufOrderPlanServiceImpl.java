package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ManufacturingOperation;
import com.axelor.apps.production.db.ProductionConfig;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.ManufacturingOperationRepository;
import com.axelor.apps.production.db.repo.ProductionConfigRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.config.ProductionConfigService;
import com.axelor.apps.production.service.manufacturingoperation.ManufacturingOperationPlanningService;
import com.axelor.apps.production.service.manufacturingoperation.ManufacturingOperationService;
import com.axelor.apps.production.service.manufacturingoperation.ManufacturingOperationWorkflowService;
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
  protected ManufacturingOperationRepository manufacturingOperationRepo;
  protected ManufacturingOperationWorkflowService manufacturingOperationWorkflowService;
  protected ManufacturingOperationPlanningService manufacturingOperationPlanningService;
  protected ManufacturingOperationService manufacturingOperationService;
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
      ManufacturingOperationRepository manufacturingOperationRepo,
      ManufacturingOperationWorkflowService manufacturingOperationWorkflowService,
      ManufacturingOperationPlanningService manufacturingOperationPlanningService,
      ManufacturingOperationService manufacturingOperationService,
      ManufOrderStockMoveService manufOrderStockMoveService,
      ProductionOrderService productionOrderService,
      ProductionConfigService productionConfigService,
      AppBaseService appBaseService,
      AppProductionService appProductionService,
      ManufOrderCreatePurchaseOrderService manufOrderCreatePurchaseOrderService) {
    this.manufOrderRepo = manufOrderRepo;
    this.manufOrderService = manufOrderService;
    this.sequenceService = sequenceService;
    this.manufacturingOperationRepo = manufacturingOperationRepo;
    this.manufacturingOperationWorkflowService = manufacturingOperationWorkflowService;
    this.manufacturingOperationPlanningService = manufacturingOperationPlanningService;
    this.manufacturingOperationService = manufacturingOperationService;
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
    if (CollectionUtils.isEmpty(manufOrder.getManufacturingOperationList())) {
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
    planPlanningManufacturingOperations(manufOrder);
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

  protected void planPlanningManufacturingOperations(ManufOrder manufOrder) throws AxelorException {
    List<ManufacturingOperation> manufacturingOperations =
        manufOrder.getManufacturingOperationList();
    if (CollectionUtils.isNotEmpty(manufacturingOperations)) {
      manufacturingOperationPlanningService.plan(manufacturingOperations);
    }
  }

  protected void planPlannedStartDateT(ManufOrder manufOrder) throws AxelorException {
    if (manufOrder.getPlannedStartDateT() == null && manufOrder.getPlannedEndDateT() == null) {
      manufOrder.setPlannedStartDateT(appProductionService.getTodayDateTime().toLocalDateTime());
    } else if (manufOrder.getPlannedStartDateT() == null
        && manufOrder.getPlannedEndDateT() != null) {
      long duration = 0;
      for (ManufacturingOperation order : manufOrder.getManufacturingOperationList()) {
        duration +=
            manufacturingOperationService.computeEntireCycleDuration(
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

    ManufacturingOperation firstManufacturingOperation = getFirstManufacturingOperation(manufOrder);

    if (firstManufacturingOperation != null) {

      return firstManufacturingOperation.getPlannedStartDateT();
    }

    return manufOrder.getPlannedStartDateT();
  }

  @Override
  public LocalDateTime computePlannedEndDateT(ManufOrder manufOrder) {

    ManufacturingOperation lastManufacturingOperation = getLastManufacturingOperation(manufOrder);

    if (lastManufacturingOperation != null) {

      return lastManufacturingOperation.getPlannedEndDateT();
    }

    return manufOrder.getPlannedStartDateT();
  }

  /**
   * Returns first operation order (highest priority) of given {@link ManufOrder}
   *
   * @param manufOrder A manufacturing order
   * @return First operation order of {@code manufOrder}
   */
  protected ManufacturingOperation getFirstManufacturingOperation(ManufOrder manufOrder) {
    return manufacturingOperationRepo
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
  protected ManufacturingOperation getLastManufacturingOperation(ManufOrder manufOrder) {
    return manufacturingOperationRepo
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

    List<ManufacturingOperation> manufacturingOperations =
        manufOrder.getManufacturingOperationList();
    if (manufacturingOperations != null) {
      manufacturingOperationWorkflowService.resetPlannedDates(manufacturingOperations);
      manufacturingOperationPlanningService.replan(manufacturingOperations);
    }

    manufOrder.setPlannedEndDateT(computePlannedEndDateT(manufOrder));
  }
}
