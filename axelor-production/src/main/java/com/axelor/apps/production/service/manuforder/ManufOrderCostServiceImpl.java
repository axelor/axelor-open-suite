/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.CostSheet;
import com.axelor.apps.production.db.CostSheetLine;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.OperationOrderDuration;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.CostSheetLineRepository;
import com.axelor.apps.production.db.repo.CostSheetRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.WorkCenterRepository;
import com.axelor.apps.production.service.ProdProcessLineComputationService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ManufOrderCostServiceImpl implements ManufOrderCostService {

  protected static final BigDecimal SECONDS_PER_HOUR = BigDecimal.valueOf(3600);

  protected static final int HOURS_SCALE = 10;

  protected static final List<String> COST_COMPONENTS =
      List.of(MATERIAL_COST, LABOR_COST, MACHINE_COST, SUBCONTRACTING_COST);

  protected AppBaseService appBaseService;
  protected AppProductionService appProductionService;
  protected ProdProcessLineComputationService prodProcessLineComputationService;

  @Inject
  public ManufOrderCostServiceImpl(
      AppBaseService appBaseService,
      AppProductionService appProductionService,
      ProdProcessLineComputationService prodProcessLineComputationService) {
    this.appBaseService = appBaseService;
    this.appProductionService = appProductionService;
    this.prodProcessLineComputationService = prodProcessLineComputationService;
  }

  @Override
  public Map<String, Object> getRealCostValues(ManufOrder manufOrder) throws AxelorException {
    int scale = appBaseService.getNbDecimalDigitForUnitPrice();

    List<CostSheet> closingCostSheets = getClosingCostSheets(manufOrder);
    boolean isFinished = manufOrder.getStatusSelect() == ManufOrderRepository.STATUS_FINISHED;

    Map<String, BigDecimal> realCost =
        isFinished && !closingCostSheets.isEmpty()
            ? computeRealCostFromCostSheets(closingCostSheets)
            : computeLiveRealCost(manufOrder);

    BigDecimal totalRealCost = realCost.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal theoreticalCost = computeTheoreticalCost(manufOrder);
    BigDecimal varianceAmount = BigDecimal.ZERO;
    BigDecimal variancePercent = BigDecimal.ZERO;
    if (isFinished) {
      varianceAmount = totalRealCost.subtract(theoreticalCost);
      variancePercent = computeVariancePercent(varianceAmount, theoreticalCost);
    }

    Map<String, Object> values = new HashMap<>();
    realCost.forEach(
        (key, amount) -> values.put(key, amount.setScale(scale, RoundingMode.HALF_UP)));
    values.put(REAL_COST, totalRealCost.setScale(scale, RoundingMode.HALF_UP));
    values.put(THEORETICAL_COST, theoreticalCost.setScale(scale, RoundingMode.HALF_UP));
    values.put(VARIANCE_AMOUNT, varianceAmount.setScale(scale, RoundingMode.HALF_UP));
    values.put(VARIANCE_PERCENT, variancePercent);
    return values;
  }

  protected Map<String, BigDecimal> newRealCost() {
    Map<String, BigDecimal> realCost = new HashMap<>();
    COST_COMPONENTS.forEach(key -> realCost.put(key, BigDecimal.ZERO));
    return realCost;
  }

  protected void addCost(Map<String, BigDecimal> realCost, String key, BigDecimal amount) {
    realCost.merge(key, amount, BigDecimal::add);
  }

  protected List<CostSheet> getClosingCostSheets(ManufOrder manufOrder) {
    if (CollectionUtils.isEmpty(manufOrder.getCostSheetList())) {
      return new ArrayList<>();
    }
    return manufOrder.getCostSheetList().stream()
        .filter(
            costSheet ->
                costSheet.getCalculationTypeSelect()
                        == CostSheetRepository.CALCULATION_END_OF_PRODUCTION
                    || costSheet.getCalculationTypeSelect()
                        == CostSheetRepository.CALCULATION_PARTIAL_END_OF_PRODUCTION)
        .collect(Collectors.toList());
  }

  protected Map<String, BigDecimal> computeRealCostFromCostSheets(List<CostSheet> costSheets) {
    Map<String, BigDecimal> realCost = newRealCost();
    for (CostSheet costSheet : costSheets) {
      if (CollectionUtils.isEmpty(costSheet.getCostSheetLineList())) {
        continue;
      }
      for (CostSheetLine costSheetLine : costSheet.getCostSheetLineList()) {
        addCostSheetLine(realCost, costSheetLine);
      }
    }
    return realCost;
  }

  protected void addCostSheetLine(Map<String, BigDecimal> realCost, CostSheetLine costSheetLine) {
    if (CollectionUtils.isNotEmpty(costSheetLine.getCostSheetLineList())) {
      for (CostSheetLine child : costSheetLine.getCostSheetLineList()) {
        addCostSheetLine(realCost, child);
      }
      return;
    }
    BigDecimal costPrice = costSheetLine.getCostPrice();
    switch (costSheetLine.getTypeSelect()) {
      case CostSheetLineRepository.TYPE_HUMAN:
        addCost(realCost, LABOR_COST, costPrice);
        break;
      case CostSheetLineRepository.TYPE_WORK_CENTER:
        addCost(
            realCost,
            costSheetLine.getTypeSelectIcon() == CostSheetLineRepository.TYPE_HUMAN
                ? LABOR_COST
                : MACHINE_COST,
            costPrice);
        break;
      case CostSheetLineRepository.TYPE_CONSUMED_PRODUCT:
        addCost(
            realCost,
            costSheetLine.getProduct() == null ? SUBCONTRACTING_COST : MATERIAL_COST,
            costPrice);
        break;
      case CostSheetLineRepository.TYPE_CONSUMED_PRODUCT_WASTE:
      case CostSheetLineRepository.TYPE_PRODUCED_PRODUCT:
        addCost(realCost, MATERIAL_COST, costPrice);
        break;
      default:
        break;
    }
  }

  protected Map<String, BigDecimal> computeLiveRealCost(ManufOrder manufOrder)
      throws AxelorException {
    Map<String, BigDecimal> realCost = newRealCost();
    addCost(realCost, MATERIAL_COST, computeMaterialCost(manufOrder));
    addCost(realCost, SUBCONTRACTING_COST, computeSubcontractingCost(manufOrder));

    if (CollectionUtils.isEmpty(manufOrder.getOperationOrderList())) {
      return realCost;
    }

    BigDecimal producedQty = computeProducedQty(manufOrder);
    LocalDateTime now = appBaseService.getTodayDateTime(manufOrder.getCompany()).toLocalDateTime();

    for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
      WorkCenter workCenter = operationOrder.getWorkCenter();
      if (operationOrder.getOutsourcing() || workCenter == null) {
        continue;
      }
      BigDecimal hours = computeOperationOrderHours(operationOrder, now);
      int workCenterType = workCenter.getWorkCenterTypeSelect();

      if (workCenterType == WorkCenterRepository.WORK_CENTER_TYPE_HUMAN
          || workCenterType == WorkCenterRepository.WORK_CENTER_TYPE_BOTH) {
        addCost(
            realCost, LABOR_COST, computeLaborCost(operationOrder, workCenter, hours, producedQty));
      }
      if (workCenterType == WorkCenterRepository.WORK_CENTER_TYPE_MACHINE
          || workCenterType == WorkCenterRepository.WORK_CENTER_TYPE_BOTH) {
        addCost(
            realCost,
            MACHINE_COST,
            computeMachineCost(operationOrder, workCenter, hours, producedQty));
      }
    }
    return realCost;
  }

  protected BigDecimal computeMaterialCost(ManufOrder manufOrder) {
    List<StockMoveLine> stockMoveLineList = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(manufOrder.getConsumedStockMoveLineList())) {
      stockMoveLineList.addAll(manufOrder.getConsumedStockMoveLineList());
    }
    if (CollectionUtils.isNotEmpty(manufOrder.getOperationOrderList())) {
      for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
        if (CollectionUtils.isNotEmpty(operationOrder.getConsumedStockMoveLineList())) {
          stockMoveLineList.addAll(operationOrder.getConsumedStockMoveLineList());
        }
      }
    }
    BigDecimal total = BigDecimal.ZERO;
    for (StockMoveLine stockMoveLine : stockMoveLineList) {
      if (!isRealized(stockMoveLine)) {
        continue;
      }
      BigDecimal unitPrice = stockMoveLine.getWapPrice();
      if (unitPrice.signum() == 0) {
        unitPrice = stockMoveLine.getCompanyUnitPriceUntaxed();
      }
      total = total.add(stockMoveLine.getRealQty().multiply(unitPrice));
    }
    return total;
  }

  protected BigDecimal computeSubcontractingCost(ManufOrder manufOrder) {
    if (CollectionUtils.isEmpty(manufOrder.getPurchaseOrderSet())) {
      return BigDecimal.ZERO;
    }
    BigDecimal total = BigDecimal.ZERO;
    for (PurchaseOrder purchaseOrder : manufOrder.getPurchaseOrderSet()) {
      if (purchaseOrder == null
          || purchaseOrder.getOrderDate() == null
          || (purchaseOrder.getStatusSelect() != PurchaseOrderRepository.STATUS_VALIDATED
              && purchaseOrder.getStatusSelect() != PurchaseOrderRepository.STATUS_FINISHED)
          || CollectionUtils.isEmpty(purchaseOrder.getPurchaseOrderLineList())) {
        continue;
      }
      for (PurchaseOrderLine purchaseOrderLine : purchaseOrder.getPurchaseOrderLineList()) {
        if (purchaseOrderLine.getIsTitleLine()) {
          continue;
        }
        total = total.add(purchaseOrderLine.getCompanyExTaxTotal());
      }
    }
    return total;
  }

  protected BigDecimal computeLaborCost(
      OperationOrder operationOrder,
      WorkCenter workCenter,
      BigDecimal hours,
      BigDecimal producedQty)
      throws AxelorException {
    ProdProcessLine prodProcessLine = operationOrder.getProdProcessLine();
    boolean isCostPerProcessLine =
        appProductionService.getIsCostPerProcessLine() && prodProcessLine != null;
    int costType =
        isCostPerProcessLine
            ? prodProcessLine.getHrCostTypeSelect()
            : workCenter.getHrCostTypeSelect();
    BigDecimal costAmount =
        isCostPerProcessLine ? prodProcessLine.getHrCostAmount() : workCenter.getHrCostAmount();
    return computeWorkCenterCost(
        costType, costAmount, hours, producedQty, getMaxCapacityPerCycle(operationOrder));
  }

  protected BigDecimal computeMachineCost(
      OperationOrder operationOrder,
      WorkCenter workCenter,
      BigDecimal hours,
      BigDecimal producedQty)
      throws AxelorException {
    ProdProcessLine prodProcessLine = operationOrder.getProdProcessLine();
    boolean isCostPerProcessLine =
        appProductionService.getIsCostPerProcessLine() && prodProcessLine != null;
    int costType =
        isCostPerProcessLine ? prodProcessLine.getCostTypeSelect() : workCenter.getCostTypeSelect();
    BigDecimal costAmount =
        isCostPerProcessLine ? prodProcessLine.getCostAmount() : workCenter.getCostAmount();
    return computeWorkCenterCost(
        costType, costAmount, hours, producedQty, getMaxCapacityPerCycle(operationOrder));
  }

  protected BigDecimal computeWorkCenterCost(
      int costType,
      BigDecimal costAmount,
      BigDecimal hours,
      BigDecimal producedQty,
      BigDecimal maxCapacityPerCycle) {
    switch (costType) {
      case WorkCenterRepository.COST_TYPE_PER_HOUR:
        return hours.multiply(costAmount);
      case WorkCenterRepository.COST_TYPE_PER_CYCLE:
        return prodProcessLineComputationService
            .computeNbCycle(producedQty, maxCapacityPerCycle)
            .multiply(costAmount);
      case WorkCenterRepository.COST_TYPE_PER_PIECE:
        return producedQty.multiply(costAmount);
      default:
        return BigDecimal.ZERO;
    }
  }

  protected BigDecimal getMaxCapacityPerCycle(OperationOrder operationOrder) {
    ProdProcessLine prodProcessLine = operationOrder.getProdProcessLine();
    if (prodProcessLine != null) {
      return prodProcessLine.getMaxCapacityPerCycle();
    }
    return operationOrder.getWorkCenter().getMaxCapacityPerCycle();
  }

  protected BigDecimal computeProducedQty(ManufOrder manufOrder) {
    if (CollectionUtils.isEmpty(manufOrder.getProducedStockMoveLineList())) {
      return BigDecimal.ZERO;
    }
    BigDecimal producedQty = BigDecimal.ZERO;
    for (StockMoveLine stockMoveLine : manufOrder.getProducedStockMoveLineList()) {
      if (isRealized(stockMoveLine)
          && Objects.equals(stockMoveLine.getProduct(), manufOrder.getProduct())) {
        producedQty = producedQty.add(stockMoveLine.getRealQty());
      }
    }
    return producedQty;
  }

  protected boolean isRealized(StockMoveLine stockMoveLine) {
    return stockMoveLine != null
        && stockMoveLine.getStockMove() != null
        && stockMoveLine.getStockMove().getStatusSelect() == StockMoveRepository.STATUS_REALIZED;
  }

  protected BigDecimal computeOperationOrderHours(
      OperationOrder operationOrder, LocalDateTime now) {
    BigDecimal totalSeconds = BigDecimal.ZERO;
    List<OperationOrderDuration> durationList = operationOrder.getOperationOrderDurationList();
    if (CollectionUtils.isNotEmpty(durationList)) {
      for (OperationOrderDuration duration : durationList) {
        if (duration.getStartingDateTime() == null) {
          continue;
        }
        LocalDateTime stoppingDateTime =
            duration.getStoppingDateTime() != null ? duration.getStoppingDateTime() : now;
        if (stoppingDateTime.isBefore(duration.getStartingDateTime())) {
          continue;
        }
        long seconds =
            Duration.between(duration.getStartingDateTime(), stoppingDateTime).getSeconds();
        totalSeconds = totalSeconds.add(BigDecimal.valueOf(seconds));
      }
    } else if (operationOrder.getRealDuration() != null) {
      totalSeconds = BigDecimal.valueOf(operationOrder.getRealDuration());
    }
    return totalSeconds.divide(SECONDS_PER_HOUR, HOURS_SCALE, RoundingMode.HALF_UP);
  }

  protected BigDecimal computeTheoreticalCost(ManufOrder manufOrder) {
    if (manufOrder.getBillOfMaterial() == null) {
      return BigDecimal.ZERO;
    }
    return manufOrder.getBillOfMaterial().getCostPrice().multiply(manufOrder.getQty());
  }

  protected BigDecimal computeVariancePercent(
      BigDecimal varianceAmount, BigDecimal theoreticalCost) {
    if (theoreticalCost.signum() == 0) {
      return BigDecimal.ZERO;
    }
    return varianceAmount
        .multiply(BigDecimal.valueOf(100))
        .divide(theoreticalCost, 2, RoundingMode.HALF_UP);
  }
}
