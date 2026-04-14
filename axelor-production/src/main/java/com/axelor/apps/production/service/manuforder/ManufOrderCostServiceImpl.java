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
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.OperationOrderDuration;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.WorkCenterRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.utils.JpaModelHelper;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class ManufOrderCostServiceImpl implements ManufOrderCostService {

  protected ManufOrderRepository manufOrderRepository;
  protected AppBaseService appBaseService;
  protected AppProductionService appProductionService;

  @Inject
  public ManufOrderCostServiceImpl(
      ManufOrderRepository manufOrderRepository,
      AppBaseService appBaseService,
      AppProductionService appProductionService) {
    this.manufOrderRepository = manufOrderRepository;
    this.appBaseService = appBaseService;
    this.appProductionService = appProductionService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void computeRealCosts(ManufOrder manufOrder) throws AxelorException {
    if (manufOrder == null) {
      return;
    }
    int scale = appBaseService.getNbDecimalDigitForUnitPrice();

    manufOrder = JpaModelHelper.ensureManaged(manufOrder);

    BigDecimal materialCost = computeMaterialCost(manufOrder, scale);
    BigDecimal laborCost = computeLaborCost(manufOrder, scale);
    BigDecimal subcontractingCost = computeSubcontractingCost(manufOrder, scale);
    BigDecimal realCost = materialCost.add(laborCost).add(subcontractingCost);
    BigDecimal theoreticalCost = computeTheoreticalCost(manufOrder, scale);
    BigDecimal varianceAmount = computeVarianceAmount(realCost, theoreticalCost);
    BigDecimal variancePercent = computeVariancePercent(varianceAmount, theoreticalCost);

    manufOrder.setMaterialCost(materialCost);
    manufOrder.setLaborCost(laborCost);
    manufOrder.setSubcontractingCost(subcontractingCost);
    manufOrder.setRealCost(realCost);
    manufOrder.setTheoreticalCost(theoreticalCost);
    manufOrder.setVarianceAmount(varianceAmount);
    manufOrder.setVariancePercent(variancePercent);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void computeMaterialCost(ManufOrder manufOrder) throws AxelorException {
    if (manufOrder == null) {
      return;
    }
    int scale = appBaseService.getNbDecimalDigitForUnitPrice();
    manufOrder.setMaterialCost(computeMaterialCost(manufOrder, scale));
    recomputeRealCosts(manufOrder, scale);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void computeLaborCost(ManufOrder manufOrder) throws AxelorException {
    if (manufOrder == null) {
      return;
    }
    int scale = appBaseService.getNbDecimalDigitForUnitPrice();
    manufOrder.setLaborCost(computeLaborCost(manufOrder, scale));
    recomputeRealCosts(manufOrder, scale);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void computeSubcontractingCost(ManufOrder manufOrder) throws AxelorException {
    if (manufOrder == null) {
      return;
    }
    int scale = appBaseService.getNbDecimalDigitForUnitPrice();
    manufOrder.setSubcontractingCost(computeSubcontractingCost(manufOrder, scale));
    recomputeRealCosts(manufOrder, scale);
  }

  protected void recomputeRealCosts(ManufOrder manufOrder, int scale) {
    BigDecimal realCost =
        manufOrder
            .getMaterialCost()
            .add(manufOrder.getLaborCost())
            .add(manufOrder.getSubcontractingCost());
    BigDecimal theoreticalCost = manufOrder.getTheoreticalCost();
    BigDecimal varianceAmount = computeVarianceAmount(realCost, theoreticalCost);
    BigDecimal variancePercent = computeVariancePercent(varianceAmount, theoreticalCost);
    manufOrder.setRealCost(realCost.setScale(scale, RoundingMode.HALF_UP));
    manufOrder.setVarianceAmount(varianceAmount.setScale(scale, RoundingMode.HALF_UP));
    manufOrder.setVariancePercent(variancePercent);
  }

  protected BigDecimal computeMaterialCost(ManufOrder manufOrder, int scale) {
    List<StockMoveLine> stockMoveLineList = new ArrayList<>();
    if (!CollectionUtils.isEmpty(manufOrder.getConsumedStockMoveLineList())) {
      stockMoveLineList.addAll(manufOrder.getConsumedStockMoveLineList());
    }
    if (!CollectionUtils.isEmpty(manufOrder.getOperationOrderList())) {
      for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
        if (!CollectionUtils.isEmpty(operationOrder.getConsumedStockMoveLineList())) {
          stockMoveLineList.addAll(operationOrder.getConsumedStockMoveLineList());
        }
      }
    }
    BigDecimal total = BigDecimal.ZERO;
    for (StockMoveLine stockMoveLine : stockMoveLineList) {
      if (stockMoveLine == null
          || stockMoveLine.getStockMove() == null
          || stockMoveLine.getStockMove().getStatusSelect()
              != StockMoveRepository.STATUS_REALIZED) {
        continue;
      }
      BigDecimal realQty = stockMoveLine.getRealQty();
      BigDecimal wapPrice = stockMoveLine.getWapPrice();
      if (wapPrice.compareTo(BigDecimal.ZERO) == 0) {
        wapPrice = stockMoveLine.getCompanyUnitPriceUntaxed();
      }
      total = total.add(realQty.multiply(wapPrice));
    }
    return total.setScale(scale, RoundingMode.HALF_UP);
  }

  protected BigDecimal computeLaborCost(ManufOrder manufOrder, int scale) {
    if (CollectionUtils.isEmpty(manufOrder.getOperationOrderList())) {
      return BigDecimal.ZERO;
    }

    BigDecimal total = BigDecimal.ZERO;
    LocalDateTime now = appBaseService.getTodayDateTime().toLocalDateTime();
    for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
      if (operationOrder.getOutsourcing()) {
        continue;
      }
      BigDecimal hours = computeOperationOrderHours(operationOrder, now, scale);
      BigDecimal hourlyRate = computeLaborHourlyRate(operationOrder);
      total = total.add(hours.multiply(hourlyRate));
    }
    return total.setScale(scale, RoundingMode.HALF_UP);
  }

  protected BigDecimal computeSubcontractingCost(ManufOrder manufOrder, int scale) {
    if (CollectionUtils.isEmpty(manufOrder.getPurchaseOrderSet())) {
      return BigDecimal.ZERO.setScale(scale, RoundingMode.HALF_UP);
    }
    BigDecimal total = BigDecimal.ZERO;
    for (PurchaseOrder purchaseOrder : manufOrder.getPurchaseOrderSet()) {
      if (purchaseOrder == null
          || purchaseOrder.getOrderDate() == null
          || purchaseOrder.getTypeSelect() != PurchaseOrderRepository.TYPE_SUBCONTRACTING
          || (PurchaseOrderRepository.STATUS_VALIDATED != purchaseOrder.getStatusSelect()
              && PurchaseOrderRepository.STATUS_FINISHED != purchaseOrder.getStatusSelect())) {
        continue;
      }
      if (CollectionUtils.isEmpty(purchaseOrder.getPurchaseOrderLineList())) {
        continue;
      }
      for (PurchaseOrderLine line : purchaseOrder.getPurchaseOrderLineList()) {
        if (line.getIsTitleLine()) {
          continue;
        }
        total = total.add(line.getExTaxTotal());
      }
    }
    return total.setScale(scale, RoundingMode.HALF_UP);
  }

  protected BigDecimal computeTheoreticalCost(ManufOrder manufOrder, int scale) {
    if (manufOrder.getBillOfMaterial() == null) {
      return BigDecimal.ZERO;
    }
    return manufOrder
        .getBillOfMaterial()
        .getCostPrice()
        .multiply(manufOrder.getQty())
        .setScale(scale, RoundingMode.HALF_UP);
  }

  protected BigDecimal computeVarianceAmount(BigDecimal realCost, BigDecimal theoreticalCost) {
    return realCost.subtract(theoreticalCost);
  }

  protected BigDecimal computeVariancePercent(
      BigDecimal varianceAmount, BigDecimal theoreticalCost) {
    return theoreticalCost.signum() != 0
        ? varianceAmount
            .multiply(BigDecimal.valueOf(100))
            .divide(theoreticalCost, 2, RoundingMode.HALF_UP)
        : BigDecimal.ZERO;
  }

  protected BigDecimal computeOperationOrderHours(
      OperationOrder operationOrder, LocalDateTime now, int scale) {
    BigDecimal totalSeconds = BigDecimal.ZERO;
    List<OperationOrderDuration> operationOrderDurationList =
        operationOrder.getOperationOrderDurationList();
    if (!CollectionUtils.isEmpty(operationOrderDurationList)) {
      for (OperationOrderDuration duration : operationOrderDurationList) {
        if (duration.getStartingDateTime() == null) {
          continue;
        }
        LocalDateTime stoppingDateTime =
            duration.getStoppingDateTime() != null ? duration.getStoppingDateTime() : now;
        if (stoppingDateTime.isBefore(duration.getStartingDateTime())) {
          continue;
        }
        long seconds =
            java.time.Duration.between(duration.getStartingDateTime(), stoppingDateTime)
                .getSeconds();
        totalSeconds = totalSeconds.add(BigDecimal.valueOf(seconds));
      }
    } else {
      totalSeconds = BigDecimal.valueOf(operationOrder.getRealDuration());
    }
    return totalSeconds.divide(BigDecimal.valueOf(3600), scale, RoundingMode.HALF_UP);
  }

  protected BigDecimal computeLaborHourlyRate(OperationOrder operationOrder) {
    WorkCenter workCenter = operationOrder.getWorkCenter();
    ProdProcessLine prodProcessLine = operationOrder.getProdProcessLine();

    BigDecimal costAmount;
    int costTypeSelect;
    if (appProductionService.getIsCostPerProcessLine() && prodProcessLine != null) {
      costAmount = prodProcessLine.getHrCostAmount();
      costTypeSelect = prodProcessLine.getHrCostTypeSelect();
    } else if (workCenter != null) {
      costAmount = workCenter.getHrCostAmount();
      costTypeSelect = workCenter.getHrCostTypeSelect();
    } else if (prodProcessLine != null) {
      costAmount = prodProcessLine.getHrCostAmount();
      costTypeSelect = prodProcessLine.getHrCostTypeSelect();
    } else {
      return BigDecimal.ZERO;
    }

    if (costAmount == null) {
      return BigDecimal.ZERO;
    }

    if (costTypeSelect == WorkCenterRepository.COST_TYPE_PER_PIECE) {
      if (workCenter == null || workCenter.getHrDurationPerCycle() == null) {
        return BigDecimal.ZERO;
      }
      BigDecimal hrDurationPerCycle =
          BigDecimal.valueOf(workCenter.getHrDurationPerCycle())
              .divide(
                  BigDecimal.valueOf(3600),
                  appBaseService.getNbDecimalDigitForUnitPrice(),
                  RoundingMode.HALF_UP);
      if (hrDurationPerCycle.signum() == 0) {
        return BigDecimal.ZERO;
      }
      return costAmount.divide(
          hrDurationPerCycle, appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
    }

    return costAmount;
  }
}
