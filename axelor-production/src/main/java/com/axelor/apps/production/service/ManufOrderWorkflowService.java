/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.production.db.CostSheet;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.db.repo.ProductionConfigRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.config.ProductionConfigService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ManufOrderWorkflowService {
  protected OperationOrderWorkflowService operationOrderWorkflowService;
  protected OperationOrderRepository operationOrderRepo;
  protected ManufOrderStockMoveService manufOrderStockMoveService;
  protected ManufOrderRepository manufOrderRepo;

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

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public ManufOrder plan(ManufOrder manufOrder) throws AxelorException {
    List<ManufOrder> manufOrderList = new ArrayList<>();
    manufOrderList.add(manufOrder);
    plan(manufOrderList);

    return manufOrderRepo.save(manufOrder);
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public List<ManufOrder> plan(List<ManufOrder> manufOrderList) throws AxelorException {
    ManufOrderService manufOrderService = Beans.get(ManufOrderService.class);

    for (ManufOrder manufOrder : manufOrderList) {
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

      if (manufOrder.getPlannedStartDateT() == null) {
        manufOrder.setPlannedStartDateT(
            Beans.get(AppProductionService.class).getTodayDateTime().toLocalDateTime());
      }
    }

    manufOrderService.optaPlan(manufOrderList);

    for (ManufOrder manufOrder : manufOrderList) {
      manufOrder.setPlannedEndDateT(this.computePlannedEndDateT(manufOrder));

      if (!manufOrder.getIsConsProOnOperation()) {
        manufOrderStockMoveService.createToConsumeStockMove(manufOrder);
      }

      manufOrderStockMoveService.createToProduceStockMove(manufOrder);
      manufOrder.setStatusSelect(ManufOrderRepository.STATUS_PLANNED);

      if (Beans.get(SequenceService.class)
          .isEmptyOrDraftSequenceNumber(manufOrder.getManufOrderSeq())) {
        manufOrder.setManufOrderSeq(manufOrderService.getManufOrderSeq());
      }
    }

    for (ManufOrder manufOrder : manufOrderList) {
      manufOrderRepo.save(manufOrder);
    }
    return manufOrderList;
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void start(ManufOrder manufOrder) throws AxelorException {

    manufOrder.setRealStartDateT(
        Beans.get(AppProductionService.class).getTodayDateTime().toLocalDateTime());

    int beforeOrAfterConfig =
        Beans.get(ProductionConfigService.class)
            .getProductionConfig(manufOrder.getCompany())
            .getStockMoveRealizeOrderSelect();
    if (beforeOrAfterConfig == ProductionConfigRepository.REALIZE_START) {
      manufOrder.addInStockMoveListItem(
          realizeStockMovesAndCreateOneEmpty(manufOrder, manufOrder.getInStockMoveList()));
      manufOrder.addOutStockMoveListItem(
          realizeStockMovesAndCreateOneEmpty(manufOrder, manufOrder.getOutStockMoveList()));
    }
    manufOrder.setStatusSelect(ManufOrderRepository.STATUS_IN_PROGRESS);
    manufOrderRepo.save(manufOrder);
  }

  /**
   * Finish unfinished stock move, and create an empty one.
   *
   * @param manufOrder
   * @param stockMoveList
   * @return the created empty stock move.
   */
  protected StockMove realizeStockMovesAndCreateOneEmpty(
      ManufOrder manufOrder, List<StockMove> stockMoveList) throws AxelorException {
    for (StockMove stockMove : stockMoveList) {
      manufOrderStockMoveService.finishStockMove(stockMove);
    }

    StockMove newStockMove =
        Beans.get(ManufOrderStockMoveService.class)
            ._createToConsumeStockMove(manufOrder, manufOrder.getCompany());
    newStockMove.setStockMoveLineList(new ArrayList<>());
    Beans.get(StockMoveService.class).plan(newStockMove);
    return newStockMove;
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
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

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
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

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void finish(ManufOrder manufOrder) throws AxelorException {
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
    CostSheet costSheet = Beans.get(CostSheetService.class).computeCostPrice(manufOrder);

    // update price in product
    Product product = manufOrder.getProduct();
    if (product.getRealOrEstimatedPriceSelect() == ProductRepository.PRICE_METHOD_FORECAST) {
      product.setLastProductionPrice(manufOrder.getBillOfMaterial().getCostPrice());
    } else if (product.getRealOrEstimatedPriceSelect() == ProductRepository.PRICE_METHOD_REAL) {
      product.setLastProductionPrice(costSheet.getCostPrice());
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
  }

  /**
   * Allows to finish partially a manufacturing order, by realizing current stock move and planning
   * the difference with the planned prodproducts.
   *
   * @param manufOrder
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void partialFinish(ManufOrder manufOrder) throws AxelorException {
    if (manufOrder.getIsConsProOnOperation()) {
      for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
        if (operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_PLANNED) {
          operationOrderWorkflowService.start(operationOrder);
        }
      }
    }
    Beans.get(CostSheetService.class).computeCostPrice(manufOrder);
    Beans.get(ManufOrderStockMoveService.class).partialFinish(manufOrder);
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void cancel(ManufOrder manufOrder) throws AxelorException {
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
    manufOrderRepo.save(manufOrder);
  }

  public LocalDateTime computePlannedEndDateT(ManufOrder manufOrder) {

    OperationOrder lastOperationOrder = getLastOperationOrder(manufOrder);

    if (lastOperationOrder != null) {

      return lastOperationOrder.getPlannedEndDateT();
    }

    return manufOrder.getPlannedStartDateT();
  }

  @Transactional
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
   * Returns last operation order (highest priority) of given {@link ManufOrder}
   *
   * @param manufOrder A manufacturing order
   * @return Last operation order of {@code manufOrder}
   */
  public OperationOrder getLastOperationOrder(ManufOrder manufOrder) {
    return operationOrderRepo
        .all()
        .filter("self.manufOrder = ?", manufOrder)
        .order("-plannedEndDateT")
        .fetchOne();
  }

  /**
   * Update planned dates.
   *
   * @param manufOrder
   * @param plannedStartDateT
   */
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
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
  private List<OperationOrder> getSortedOperationOrderList(ManufOrder manufOrder) {
    List<OperationOrder> operationOrderList = manufOrder.getOperationOrderList();
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
}
