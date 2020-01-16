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
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.EmailAccountRepository;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProductionConfig;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.CostSheetRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.db.repo.ProdProcessRepository;
import com.axelor.apps.production.db.repo.ProductionConfigRepository;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.costsheet.CostSheetService;
import com.axelor.apps.production.service.operationorder.OperationOrderWorkflowService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ManufOrderWorkflowService {
  protected OperationOrderWorkflowService operationOrderWorkflowService;
  protected OperationOrderRepository operationOrderRepo;
  protected ManufOrderStockMoveService manufOrderStockMoveService;
  protected ManufOrderRepository manufOrderRepo;

  @Inject ProductionConfigRepository productionConfigRepo;

  @Inject
  public ManufOrderWorkflowService(
      OperationOrderWorkflowService operationOrderWorkflowService,
      OperationOrderRepository operationOrderRepo,
      ManufOrderStockMoveService manufOrderStockMoveService,
      ManufOrderRepository manufOrderRepo) {
    this.operationOrderWorkflowService = operationOrderWorkflowService;
    this.operationOrderRepo = operationOrderRepo;
    this.manufOrderStockMoveService = manufOrderStockMoveService;
    this.manufOrderRepo = manufOrderRepo;
  }

  @Transactional(rollbackOn = {Exception.class})
  public ManufOrder plan(ManufOrder manufOrder) throws AxelorException {
    List<ManufOrder> manufOrderList = new ArrayList<>();
    manufOrderList.add(manufOrder);
    plan(manufOrderList);
    return manufOrderRepo.save(manufOrder);
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public List<ManufOrder> plan(List<ManufOrder> manufOrderList) throws AxelorException {
    return plan(manufOrderList, true);
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public List<ManufOrder> plan(List<ManufOrder> manufOrderList, boolean quickSolve)
      throws AxelorException {
    ManufOrderService manufOrderService = Beans.get(ManufOrderService.class);
    SequenceService sequenceService = Beans.get(SequenceService.class);

    for (ManufOrder manufOrder : manufOrderList) {
      if (manufOrder.getBillOfMaterial().getStatusSelect()
              != BillOfMaterialRepository.STATUS_APPLICABLE
          && manufOrder.getProdProcess().getStatusSelect()
              != ProdProcessRepository.STATUS_APPLICABLE) {
        throw new AxelorException(
            manufOrder,
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get("Bill of material and production process must be applicable"));
      }

      if (sequenceService.isEmptyOrDraftSequenceNumber(manufOrder.getManufOrderSeq())) {
        manufOrder.setManufOrderSeq(manufOrderService.getManufOrderSeq(manufOrder));
      }
      if (CollectionUtils.isEmpty(manufOrder.getOperationOrderList())) {
        manufOrderService.preFillOperations(manufOrder);
      }
      if (!manufOrder.getIsConsProOnOperation()
          && CollectionUtils.isEmpty(manufOrder.getToConsumeProdProductList())) {
        manufOrderService.createToConsumeProdProductList(manufOrder);
      }

      if (CollectionUtils.isEmpty(manufOrder.getToProduceProdProductList())) {
        manufOrderService.createToProduceProdProductList(manufOrder);
      }

      if (manufOrder.getPlannedStartDateT() == null && manufOrder.getPlannedEndDateT() == null) {
        manufOrder.setPlannedStartDateT(
            Beans.get(AppProductionService.class).getTodayDateTime().toLocalDateTime());
      } else if (manufOrder.getPlannedStartDateT() == null
          && manufOrder.getPlannedEndDateT() != null) {
        long duration = 0;
        for (OperationOrder order : manufOrder.getOperationOrderList()) {
          duration +=
              operationOrderWorkflowService.computeEntireCycleDuration(
                  order, order.getManufOrder().getQty()); // in seconds
        }
        manufOrder.setPlannedStartDateT(manufOrder.getPlannedEndDateT().minusSeconds(duration));
      }
    }

    for (ManufOrder manufOrder : manufOrderList) {
      if (manufOrder.getOperationOrderList() != null) {
        for (OperationOrder operationOrder : getSortedOperationOrderList(manufOrder)) {
          operationOrderWorkflowService.plan(operationOrder);
        }
      }
    }

    for (ManufOrder manufOrder : manufOrderList) {
      //    	manufOrder.setPlannedStartDateT(this.computePlannedStartDateT(manufOrder));
      if (manufOrder.getPlannedEndDateT() == null) {
        manufOrder.setPlannedEndDateT(this.computePlannedEndDateT(manufOrder));
      }

      if (manufOrder.getBillOfMaterial() != null) {
        manufOrder.setUnit(manufOrder.getBillOfMaterial().getUnit());
      }

      if (!manufOrder.getIsConsProOnOperation()) {
        manufOrderStockMoveService.createToConsumeStockMove(manufOrder);
      }

      manufOrderStockMoveService.createToProduceStockMove(manufOrder);
      manufOrder.setStatusSelect(ManufOrderRepository.STATUS_PLANNED);
      manufOrder.setCancelReason(null);
      manufOrder.setCancelReasonStr(null);

      manufOrderRepo.save(manufOrder);
    }
    return manufOrderList;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void start(ManufOrder manufOrder) throws AxelorException {

    manufOrder.setRealStartDateT(
        Beans.get(AppProductionService.class).getTodayDateTime().toLocalDateTime());

    int beforeOrAfterConfig = manufOrder.getProdProcess().getStockMoveRealizeOrderSelect();
    if (beforeOrAfterConfig == ProductionConfigRepository.REALIZE_START) {
      for (StockMove stockMove : manufOrder.getInStockMoveList()) {
        manufOrderStockMoveService.finishStockMove(stockMove);
      }
    }
    manufOrder.setStatusSelect(ManufOrderRepository.STATUS_IN_PROGRESS);
    manufOrderRepo.save(manufOrder);
  }

  @Transactional
  public void pause(ManufOrder manufOrder) {
    if (manufOrder.getOperationOrderList() != null) {
      for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
        if (operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_IN_PROGRESS) {
          operationOrderWorkflowService.pause(operationOrder);
        }
      }
    }

    manufOrder.setStatusSelect(ManufOrderRepository.STATUS_STANDBY);
    manufOrderRepo.save(manufOrder);
  }

  @Transactional
  public void resume(ManufOrder manufOrder) {
    if (manufOrder.getOperationOrderList() != null) {
      for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
        if (operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_STANDBY) {
          operationOrderWorkflowService.resume(operationOrder);
        }
      }
    }

    manufOrder.setStatusSelect(ManufOrderRepository.STATUS_IN_PROGRESS);
    manufOrderRepo.save(manufOrder);
  }

  @Transactional(rollbackOn = {Exception.class})
  public boolean finish(ManufOrder manufOrder) throws AxelorException {
    if (manufOrder.getOperationOrderList() != null) {
      for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
        if (operationOrder.getStatusSelect() != OperationOrderRepository.STATUS_FINISHED) {
          if (operationOrder.getStatusSelect() != OperationOrderRepository.STATUS_IN_PROGRESS
              && operationOrder.getStatusSelect() != OperationOrderRepository.STATUS_STANDBY) {
            operationOrderWorkflowService.start(operationOrder);
          }

          operationOrderWorkflowService.finish(operationOrder);
        }
      }
    }

    // create cost sheet
    Beans.get(CostSheetService.class)
        .computeCostPrice(
            manufOrder,
            CostSheetRepository.CALCULATION_END_OF_PRODUCTION,
            Beans.get(AppBaseService.class).getTodayDate());

    // update price in product
    Product product = manufOrder.getProduct();
    if (product.getRealOrEstimatedPriceSelect() == ProductRepository.PRICE_METHOD_FORECAST) {
      product.setLastProductionPrice(manufOrder.getBillOfMaterial().getCostPrice());
    } else if (product.getRealOrEstimatedPriceSelect() == ProductRepository.PRICE_METHOD_REAL) {
      BigDecimal costPrice = computeOneUnitProductionPrice(manufOrder);
      if (costPrice.signum() != 0) {
        product.setLastProductionPrice(costPrice);
      }
    } else {
      // default value is forecast
      product.setRealOrEstimatedPriceSelect(ProductRepository.PRICE_METHOD_FORECAST);
      product.setLastProductionPrice(manufOrder.getBillOfMaterial().getCostPrice());
    }

    // update costprice in product
    if (product.getCostTypeSelect() == ProductRepository.COST_TYPE_LAST_PRODUCTION_PRICE) {
      product.setCostPrice(product.getLastProductionPrice());
      if (product.getAutoUpdateSalePrice()) {
        Beans.get(ProductService.class).updateSalePrice(product);
      }
    }

    manufOrderStockMoveService.finish(manufOrder);
    manufOrder.setRealEndDateT(
        Beans.get(AppProductionService.class).getTodayDateTime().toLocalDateTime());
    manufOrder.setStatusSelect(ManufOrderRepository.STATUS_FINISHED);
    manufOrder.setEndTimeDifference(
        new BigDecimal(
            ChronoUnit.MINUTES.between(
                manufOrder.getPlannedEndDateT(), manufOrder.getRealEndDateT())));
    manufOrderRepo.save(manufOrder);
    ProductionConfig productionConfig =
        manufOrder.getCompany() != null
            ? productionConfigRepo.findByCompany(manufOrder.getCompany())
            : null;
    if (productionConfig != null && productionConfig.getFinishMoAutomaticEmail()) {
      return this.sendMail(manufOrder, productionConfig.getFinishMoMessageTemplate());
    }
    return true;
  }

  /** Return the cost price for one unit in a manufacturing order. */
  protected BigDecimal computeOneUnitProductionPrice(ManufOrder manufOrder) {
    BigDecimal qty = manufOrder.getQty();
    if (qty.signum() != 0) {
      int scale = Beans.get(AppProductionService.class).getNbDecimalDigitForUnitPrice();
      return manufOrder.getCostPrice().divide(qty, scale, BigDecimal.ROUND_HALF_EVEN);
    } else {
      return BigDecimal.ZERO;
    }
  }

  /**
   * Allows to finish partially a manufacturing order, by realizing current stock move and planning
   * the difference with the planned prodproducts.
   *
   * @param manufOrder
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  public boolean partialFinish(ManufOrder manufOrder) throws AxelorException {
    if (manufOrder.getIsConsProOnOperation()) {
      for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
        if (operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_PLANNED) {
          operationOrderWorkflowService.start(operationOrder);
        }
      }
    }
    Beans.get(CostSheetService.class)
        .computeCostPrice(
            manufOrder,
            CostSheetRepository.CALCULATION_PARTIAL_END_OF_PRODUCTION,
            Beans.get(AppBaseService.class).getTodayDate());
    Beans.get(ManufOrderStockMoveService.class).partialFinish(manufOrder);
    ProductionConfig productionConfig =
        manufOrder.getCompany() != null
            ? productionConfigRepo.findByCompany(manufOrder.getCompany())
            : null;
    if (productionConfig != null && productionConfig.getPartFinishMoAutomaticEmail()) {
      return this.sendMail(manufOrder, productionConfig.getPartFinishMoMessageTemplate());
    }
    return true;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void cancel(ManufOrder manufOrder, CancelReason cancelReason, String cancelReasonStr)
      throws AxelorException {
    if (cancelReason == null
        && manufOrder.getStatusSelect() != ManufOrderRepository.STATUS_DRAFT
        && manufOrder.getStatusSelect() != ManufOrderRepository.STATUS_PLANNED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.MANUF_ORDER_CANCEL_REASON_ERROR));
    }
    if (manufOrder.getOperationOrderList() != null) {
      for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
        if (operationOrder.getStatusSelect() != OperationOrderRepository.STATUS_CANCELED) {
          operationOrderWorkflowService.cancel(operationOrder);
        }
      }
    }

    manufOrderStockMoveService.cancel(manufOrder);

    if (manufOrder.getConsumedStockMoveLineList() != null) {
      manufOrder
          .getConsumedStockMoveLineList()
          .forEach(stockMoveLine -> stockMoveLine.setConsumedManufOrder(null));
    }
    if (manufOrder.getProducedStockMoveLineList() != null) {
      manufOrder
          .getProducedStockMoveLineList()
          .forEach(stockMoveLine -> stockMoveLine.setProducedManufOrder(null));
    }
    if (manufOrder.getDiffConsumeProdProductList() != null) {
      manufOrder.clearDiffConsumeProdProductList();
    }

    manufOrder.setStatusSelect(ManufOrderRepository.STATUS_CANCELED);
    if (cancelReason != null) {
      manufOrder.setCancelReason(cancelReason);
      if (Strings.isNullOrEmpty(cancelReasonStr)) {
        manufOrder.setCancelReasonStr(cancelReason.getName());
      } else {
        manufOrder.setCancelReasonStr(cancelReasonStr);
      }
    }
    manufOrderRepo.save(manufOrder);
  }

  public LocalDateTime computePlannedStartDateT(ManufOrder manufOrder) {

    OperationOrder firstOperationOrder = getFirstOperationOrder(manufOrder);

    if (firstOperationOrder != null) {

      return firstOperationOrder.getPlannedStartDateT();
    }

    return manufOrder.getPlannedStartDateT();
  }

  public LocalDateTime computePlannedEndDateT(ManufOrder manufOrder) {

    OperationOrder lastOperationOrder = getLastOperationOrder(manufOrder);

    if (lastOperationOrder != null) {

      return lastOperationOrder.getPlannedEndDateT();
    }

    return manufOrder.getPlannedStartDateT();
  }

  @Transactional(rollbackOn = {Exception.class})
  public void allOpFinished(ManufOrder manufOrder) throws AxelorException {
    int count = 0;
    List<OperationOrder> operationOrderList = manufOrder.getOperationOrderList();
    for (OperationOrder operationOrderIt : operationOrderList) {
      if (operationOrderIt.getStatusSelect() == OperationOrderRepository.STATUS_FINISHED) {
        count++;
      }
    }

    if (count == operationOrderList.size()) {
      this.finish(manufOrder);
    }
  }

  /**
   * Returns first operation order (highest priority) of given {@link ManufOrder}
   *
   * @param manufOrder A manufacturing order
   * @return First operation order of {@code manufOrder}
   */
  public OperationOrder getFirstOperationOrder(ManufOrder manufOrder) {
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
  public OperationOrder getLastOperationOrder(ManufOrder manufOrder) {
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
  @Transactional(rollbackOn = {Exception.class})
  public void updatePlannedDates(ManufOrder manufOrder, LocalDateTime plannedStartDateT)
      throws AxelorException {
    manufOrder.setPlannedStartDateT(plannedStartDateT);

    if (manufOrder.getOperationOrderList() != null) {
      List<OperationOrder> operationOrderList = getSortedOperationOrderList(manufOrder);
      operationOrderWorkflowService.resetPlannedDates(operationOrderList);

      for (OperationOrder operationOrder : operationOrderList) {
        operationOrderWorkflowService.replan(operationOrder);
      }
    }

    manufOrder.setPlannedEndDateT(computePlannedEndDateT(manufOrder));
  }

  /**
   * Get a list of operation orders sorted by priority and id from the specified manufacturing
   * order.
   *
   * @param manufOrder
   * @return
   */
  protected List<OperationOrder> getSortedOperationOrderList(ManufOrder manufOrder) {
    List<OperationOrder> operationOrderList =
        MoreObjects.firstNonNull(manufOrder.getOperationOrderList(), Collections.emptyList());
    Comparator<OperationOrder> byPriority =
        Comparator.comparing(
            OperationOrder::getPriority, Comparator.nullsFirst(Comparator.naturalOrder()));
    Comparator<OperationOrder> byId =
        Comparator.comparing(
            OperationOrder::getId, Comparator.nullsFirst(Comparator.naturalOrder()));

    return operationOrderList
        .stream()
        .sorted(byPriority.thenComparing(byId))
        .collect(Collectors.toList());
  }

  protected boolean sendMail(ManufOrder manufOrder, Template template) throws AxelorException {
    if (template == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.MANUF_ORDER_MISSING_TEMPLATE));
    }
    if (Beans.get(EmailAccountRepository.class)
            .all()
            .filter("self.isDefault = true AND self.isValid = true")
            .fetchOne()
        == null) {
      return false;
    }
    try {
      Beans.get(TemplateMessageService.class).generateAndSendMessage(manufOrder, template);
    } catch (Exception e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage(), manufOrder);
    }
    return true;
  }
}
