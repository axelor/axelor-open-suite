/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import com.axelor.app.production.db.IWorkCenter;
import com.axelor.apps.base.db.AppProduction;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.production.db.*;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CostSheetServiceImpl implements CostSheetService {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final int QTY_MAX_SCALE = 10;

  protected UnitConversionService unitConversionService;
  protected CostSheetLineService costSheetLineService;
  protected BillOfMaterialRepository billOfMaterialRepo;
  protected AppProductionService appProductionService;

  protected Unit hourUnit;
  protected Unit cycleUnit;
  protected boolean manageResidualProductOnBom;
  protected CostSheet costSheet;

  @Inject
  public CostSheetServiceImpl(
      AppProductionService appProductionService,
      UnitConversionService unitConversionService,
      CostSheetLineService costSheetLineService,
      BillOfMaterialRepository billOfMaterialRepo) {

    this.appProductionService = appProductionService;
    this.unitConversionService = unitConversionService;
    this.costSheetLineService = costSheetLineService;
    this.billOfMaterialRepo = billOfMaterialRepo;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public CostSheet computeCostPrice(BillOfMaterial billOfMaterial) throws AxelorException {

    this.init();

    CostSheetLine producedCostSheetLine =
        costSheetLineService.createProducedProductCostSheetLine(
            billOfMaterial.getProduct(), billOfMaterial.getUnit(), billOfMaterial.getQty());

    costSheet.addCostSheetLineListItem(producedCostSheetLine);

    this._computeCostPrice(billOfMaterial, 0, producedCostSheetLine);

    this.computeResidualProduct(billOfMaterial);

    billOfMaterial.setCostPrice(this.computeCostPrice(costSheet));

    billOfMaterial.addCostSheetListItem(costSheet);

    billOfMaterialRepo.save(billOfMaterial);

    return costSheet;
  }

  protected void init() {

    AppProduction appProduction = appProductionService.getAppProduction();
    this.hourUnit = appProductionService.getAppBase().getUnitHours();
    this.cycleUnit = appProduction.getCycleUnit();
    this.manageResidualProductOnBom = appProduction.getManageResidualProductOnBom();

    costSheet = new CostSheet();
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public CostSheet computeCostPrice(ManufOrder manufOrder) throws AxelorException {
    this.init();

    BigDecimal producedQuantity =
        Beans.get(ManufOrderService.class).getProducedQuantity(manufOrder);
    CostSheetLine producedCostSheetLine =
        costSheetLineService.createProducedProductCostSheetLine(
            manufOrder.getProduct(), manufOrder.getBillOfMaterial().getUnit(), producedQuantity);
    costSheet.addCostSheetLineListItem(producedCostSheetLine);
    this.computeRealCostPrice(manufOrder, 0, producedCostSheetLine);

    this.computeRealResidualProduct(manufOrder);

    this.computeCostPrice(costSheet);
    manufOrder.addCostSheetListItem(costSheet);
    manufOrder.setCostPrice(costSheet.getCostPrice());
    Beans.get(ManufOrderRepository.class).save(manufOrder);

    return costSheet;
  }

  protected void computeResidualProduct(BillOfMaterial billOfMaterial) throws AxelorException {

    if (this.manageResidualProductOnBom && billOfMaterial.getProdResidualProductList() != null) {

      for (ProdResidualProduct prodResidualProduct : billOfMaterial.getProdResidualProductList()) {

        CostSheetLine costSheetLine =
            costSheetLineService.createResidualProductCostSheetLine(
                prodResidualProduct.getProduct(),
                prodResidualProduct.getUnit(),
                prodResidualProduct.getQty());

        costSheet.addCostSheetLineListItem(costSheetLine);
      }
    }
  }

  protected BigDecimal computeCostPrice(CostSheet costSheet) {

    BigDecimal costPrice = BigDecimal.ZERO;

    if (costSheet.getCostSheetLineList() != null) {
      for (CostSheetLine costSheetLine : costSheet.getCostSheetLineList()) {

        if (costSheetLine.getCostSheetLineList() != null
            && !costSheetLine.getCostSheetLineList().isEmpty()) {
          costPrice = costPrice.add(this.computeCostPrice(costSheetLine));
        } else {
          costPrice = costPrice.add(costSheetLine.getCostPrice());
        }
      }
    }

    costSheet.setCostPrice(costPrice);

    return costPrice;
  }

  protected BigDecimal computeCostPrice(CostSheetLine parentCostSheetLine) {

    BigDecimal costPrice = BigDecimal.ZERO;

    if (parentCostSheetLine.getCostSheetLineList() != null) {
      for (CostSheetLine costSheetLine : parentCostSheetLine.getCostSheetLineList()) {

        if (costSheetLine.getCostSheetLineList() != null
            && !costSheetLine.getCostSheetLineList().isEmpty()) {
          costPrice = costPrice.add(this.computeCostPrice(costSheetLine));
        } else {
          costPrice = costPrice.add(costSheetLine.getCostPrice());
        }
      }
    }

    parentCostSheetLine.setCostPrice(costPrice);

    return costPrice;
  }

  protected void _computeCostPrice(
      BillOfMaterial billOfMaterial, int bomLevel, CostSheetLine parentCostSheetLine)
      throws AxelorException {

    bomLevel++;

    // Cout des composants
    this._computeToConsumeProduct(billOfMaterial, bomLevel, parentCostSheetLine);

    // Cout des operations
    this._computeProcess(
        billOfMaterial.getProdProcess(),
        billOfMaterial.getQty(),
        billOfMaterial.getProduct().getUnit(),
        bomLevel,
        parentCostSheetLine);
  }

  protected void _computeToConsumeProduct(
      BillOfMaterial billOfMaterial, int bomLevel, CostSheetLine parentCostSheetLine)
      throws AxelorException {

    if (billOfMaterial.getBillOfMaterialSet() != null) {

      for (BillOfMaterial billOfMaterialLine : billOfMaterial.getBillOfMaterialSet()) {

        Product product = billOfMaterialLine.getProduct();

        if (product != null) {

          CostSheetLine costSheetLine =
              costSheetLineService.createConsumedProductCostSheetLine(
                  product,
                  billOfMaterialLine.getUnit(),
                  bomLevel,
                  parentCostSheetLine,
                  billOfMaterialLine.getQty());

          BigDecimal wasteRate = billOfMaterialLine.getWasteRate();

          if (wasteRate != null && wasteRate.compareTo(BigDecimal.ZERO) > 0) {
            costSheetLineService.createConsumedProductWasteCostSheetLine(
                product,
                billOfMaterialLine.getUnit(),
                bomLevel,
                parentCostSheetLine,
                billOfMaterialLine.getQty(),
                wasteRate);
          }

          if (billOfMaterialLine.getDefineSubBillOfMaterial()) {
            this._computeCostPrice(billOfMaterialLine, bomLevel, costSheetLine);
          }
        }
      }
    }
  }

  protected void _computeProcess(
      ProdProcess prodProcess,
      BigDecimal producedQty,
      Unit pieceUnit,
      int bomLevel,
      CostSheetLine parentCostSheetLine)
      throws AxelorException {

    if (prodProcess != null && prodProcess.getProdProcessLineList() != null) {

      for (ProdProcessLine prodProcessLine : prodProcess.getProdProcessLineList()) {

        WorkCenter workCenter = prodProcessLine.getWorkCenter();

        if (workCenter != null) {

          int workCenterTypeSelect = workCenter.getWorkCenterTypeSelect();

          if (workCenterTypeSelect == IWorkCenter.WORK_CENTER_HUMAN
              || workCenterTypeSelect == IWorkCenter.WORK_CENTER_BOTH) {

            this._computeHumanResourceCost(
                workCenter, prodProcessLine.getPriority(), bomLevel, parentCostSheetLine);
          }
          if (workCenterTypeSelect == IWorkCenter.WORK_CENTER_MACHINE
              || workCenterTypeSelect == IWorkCenter.WORK_CENTER_BOTH) {

            this._computeMachineCost(
                prodProcessLine, producedQty, pieceUnit, bomLevel, parentCostSheetLine);
          }
        }
      }
    }
  }

  protected void _computeHumanResourceCost(
      WorkCenter workCenter, int priority, int bomLevel, CostSheetLine parentCostSheetLine)
      throws AxelorException {

    if (workCenter.getProdHumanResourceList() != null) {

      for (ProdHumanResource prodHumanResource : workCenter.getProdHumanResourceList()) {

        this._computeHumanResourceCost(prodHumanResource, priority, bomLevel, parentCostSheetLine);
      }
    }
  }

  protected void _computeHumanResourceCost(
      ProdHumanResource prodHumanResource,
      int priority,
      int bomLevel,
      CostSheetLine parentCostSheetLine)
      throws AxelorException {

    BigDecimal costPerHour = BigDecimal.ZERO;

    if (prodHumanResource.getProduct() != null) {

      Product product = prodHumanResource.getProduct();

      costPerHour =
          unitConversionService.convert(
              hourUnit,
              product.getUnit(),
              product.getCostPrice(),
              appProductionService.getNbDecimalDigitForUnitPrice(),
              product);
    }

    BigDecimal durationHours =
        BigDecimal.valueOf(prodHumanResource.getDuration())
            .divide(
                BigDecimal.valueOf(3600),
                appProductionService.getNbDecimalDigitForUnitPrice(),
                RoundingMode.HALF_EVEN);

    costSheetLineService.createWorkCenterHRCostSheetLine(
        prodHumanResource.getWorkCenter(),
        prodHumanResource,
        priority,
        bomLevel,
        parentCostSheetLine,
        durationHours,
        costPerHour.multiply(durationHours),
        hourUnit);
  }

  protected void _computeMachineCost(
      ProdProcessLine prodProcessLine,
      BigDecimal producedQty,
      Unit pieceUnit,
      int bomLevel,
      CostSheetLine parentCostSheetLine) {

    WorkCenter workCenter = prodProcessLine.getWorkCenter();

    int costType = workCenter.getCostTypeSelect();

    if (costType == IWorkCenter.COST_PER_CYCLE) {

      costSheetLineService.createWorkCenterMachineCostSheetLine(
          workCenter,
          prodProcessLine.getPriority(),
          bomLevel,
          parentCostSheetLine,
          this.getNbCycle(producedQty, prodProcessLine.getMaxCapacityPerCycle()),
          workCenter.getCostAmount(),
          cycleUnit);

    } else if (costType == IWorkCenter.COST_PER_HOUR) {

      BigDecimal qty =
          new BigDecimal(prodProcessLine.getDurationPerCycle())
              .divide(
                  new BigDecimal(3600),
                  appProductionService.getNbDecimalDigitForUnitPrice(),
                  BigDecimal.ROUND_HALF_EVEN)
              .multiply(this.getNbCycle(producedQty, prodProcessLine.getMaxCapacityPerCycle()));
      qty = qty.setScale(QTY_MAX_SCALE, BigDecimal.ROUND_HALF_EVEN);
      BigDecimal costPrice = workCenter.getCostAmount().multiply(qty);

      costSheetLineService.createWorkCenterMachineCostSheetLine(
          workCenter,
          prodProcessLine.getPriority(),
          bomLevel,
          parentCostSheetLine,
          qty,
          costPrice,
          hourUnit);

    } else if (costType == IWorkCenter.COST_PER_PIECE) {

      BigDecimal costPrice = workCenter.getCostAmount().multiply(producedQty);

      costSheetLineService.createWorkCenterMachineCostSheetLine(
          workCenter,
          prodProcessLine.getPriority(),
          bomLevel,
          parentCostSheetLine,
          producedQty,
          costPrice,
          pieceUnit);
    }
  }

  protected BigDecimal getNbCycle(BigDecimal producedQty, BigDecimal capacityPerCycle) {

    if (capacityPerCycle.compareTo(BigDecimal.ZERO) == 0) {
      return producedQty;
    }

    return producedQty.divide(capacityPerCycle, RoundingMode.CEILING);
  }

  protected void computeRealResidualProduct(ManufOrder manufOrder) throws AxelorException {
    for (StockMoveLine stockMoveLine : manufOrder.getProducedStockMoveLineList()) {
      if (!stockMoveLine.getProduct().equals(manufOrder.getProduct())) {
        CostSheetLine costSheetLine =
            costSheetLineService.createResidualProductCostSheetLine(
                stockMoveLine.getProduct(), stockMoveLine.getUnit(), stockMoveLine.getRealQty());
        costSheet.addCostSheetLineListItem(costSheetLine);
      }
    }
  }

  protected void computeRealCostPrice(
      ManufOrder manufOrder, int bomLevel, CostSheetLine parentCostSheetLine)
      throws AxelorException {

    bomLevel++;

    this.computeConsumedProduct(manufOrder, bomLevel, parentCostSheetLine);
    BigDecimal producedQty = Beans.get(ManufOrderService.class).getProducedQuantity(manufOrder);
    this.computeRealProcess(
        manufOrder.getOperationOrderList(),
        manufOrder.getProduct().getUnit(),
        producedQty,
        bomLevel,
        parentCostSheetLine);
  }

  protected void computeConsumedProduct(
      ManufOrder manufOrder, int bomLevel, CostSheetLine parentCostSheetLine)
      throws AxelorException {

    if (manufOrder.getIsConsProOnOperation()) {
      for (OperationOrder operation : manufOrder.getOperationOrderList()) {
        if (operation.getConsumedStockMoveLineList() == null) {
          continue;
        }
        for (StockMoveLine stockMoveLine : operation.getConsumedStockMoveLineList()) {
          if (stockMoveLine.getProduct() == null) {
            continue;
          }
          costSheetLineService.createConsumedProductCostSheetLine(
              stockMoveLine.getProduct(),
              stockMoveLine.getUnit(),
              bomLevel,
              parentCostSheetLine,
              stockMoveLine.getRealQty());
        }
      }
    } else {
      for (StockMoveLine stockMoveLine : manufOrder.getConsumedStockMoveLineList()) {
        if (stockMoveLine.getProduct() == null) {
          continue;
        }
        costSheetLineService.createConsumedProductCostSheetLine(
            stockMoveLine.getProduct(),
            stockMoveLine.getUnit(),
            bomLevel,
            parentCostSheetLine,
            stockMoveLine.getRealQty());
      }
    }
  }

  protected void computeRealProcess(
      List<OperationOrder> operationOrders,
      Unit pieceUnit,
      BigDecimal producedQty,
      int bomLevel,
      CostSheetLine parentCostSheetLine)
      throws AxelorException {
    for (OperationOrder operationOrder : operationOrders) {

      WorkCenter workCenter = operationOrder.getMachineWorkCenter();
      if (workCenter == null) {
        workCenter = operationOrder.getWorkCenter();
      }
      if (workCenter == null) {
        continue;
      }
      int workCenterTypeSelect = workCenter.getWorkCenterTypeSelect();
      if (workCenterTypeSelect == IWorkCenter.WORK_CENTER_HUMAN
          || workCenterTypeSelect == IWorkCenter.WORK_CENTER_BOTH) {

        this.computeRealHumanResourceCost(
            operationOrder, operationOrder.getPriority(), bomLevel, parentCostSheetLine);
      }
      if (workCenterTypeSelect == IWorkCenter.WORK_CENTER_MACHINE
          || workCenterTypeSelect == IWorkCenter.WORK_CENTER_BOTH) {

        this.computeRealMachineCost(
            operationOrder, workCenter, producedQty, pieceUnit, bomLevel, parentCostSheetLine);
      }
    }
  }

  protected void computeRealHumanResourceCost(
      OperationOrder operationOrder, int priority, int bomLevel, CostSheetLine parentCostSheetLine)
      throws AxelorException {
    if (operationOrder.getProdHumanResourceList() != null) {
      for (ProdHumanResource prodHumanResource : operationOrder.getProdHumanResourceList()) {
        this.computeRealHumanResourceCost(
            prodHumanResource,
            operationOrder.getWorkCenter(),
            priority,
            bomLevel,
            parentCostSheetLine,
            operationOrder.getRealDuration());
      }
    }
  }

  protected void computeRealHumanResourceCost(
      ProdHumanResource prodHumanResource,
      WorkCenter workCenter,
      int priority,
      int bomLevel,
      CostSheetLine parentCostSheetLine,
      Long realDuration)
      throws AxelorException {
    BigDecimal costPerHour = BigDecimal.ZERO;
    if (prodHumanResource.getProduct() != null) {
      Product product = prodHumanResource.getProduct();
      costPerHour =
          unitConversionService.convert(
              hourUnit,
              product.getUnit(),
              product.getCostPrice(),
              appProductionService.getNbDecimalDigitForUnitPrice(),
              product);
    }
    BigDecimal durationHours =
        new BigDecimal(realDuration)
            .divide(
                new BigDecimal(3600),
                appProductionService.getNbDecimalDigitForUnitPrice(),
                BigDecimal.ROUND_HALF_EVEN);

    costSheetLineService.createWorkCenterHRCostSheetLine(
        workCenter,
        prodHumanResource,
        priority,
        bomLevel,
        parentCostSheetLine,
        durationHours,
        costPerHour.multiply(durationHours),
        hourUnit);
  }

  protected void computeRealMachineCost(
      OperationOrder operationOrder,
      WorkCenter workCenter,
      BigDecimal producedQty,
      Unit pieceUnit,
      int bomLevel,
      CostSheetLine parentCostSheetLine) {
    int costType = workCenter.getCostTypeSelect();

    if (costType == IWorkCenter.COST_PER_CYCLE) {
      costSheetLineService.createWorkCenterMachineCostSheetLine(
          workCenter,
          operationOrder.getPriority(),
          bomLevel,
          parentCostSheetLine,
          this.getNbCycle(producedQty, workCenter.getMaxCapacityPerCycle()),
          workCenter.getCostAmount(),
          cycleUnit);
    } else if (costType == IWorkCenter.COST_PER_HOUR) {
      BigDecimal qty =
          new BigDecimal(operationOrder.getRealDuration())
              .divide(
                  new BigDecimal(3600),
                  appProductionService.getNbDecimalDigitForUnitPrice(),
                  BigDecimal.ROUND_HALF_EVEN);
      BigDecimal costPrice = workCenter.getCostAmount().multiply(qty);
      costSheetLineService.createWorkCenterMachineCostSheetLine(
          workCenter,
          operationOrder.getPriority(),
          bomLevel,
          parentCostSheetLine,
          qty,
          costPrice,
          hourUnit);
    } else if (costType == IWorkCenter.COST_PER_PIECE) {

      BigDecimal costPrice = workCenter.getCostAmount().multiply(producedQty);
      costSheetLineService.createWorkCenterMachineCostSheetLine(
          workCenter,
          operationOrder.getPriority(),
          bomLevel,
          parentCostSheetLine,
          producedQty,
          costPrice,
          pieceUnit);
    }
  }
}
