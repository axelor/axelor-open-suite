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
package com.axelor.apps.production.service.costsheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.db.CostSheet;
import com.axelor.apps.production.db.CostSheetLine;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.db.ProdResidualProduct;
import com.axelor.apps.production.db.UnitCostCalculation;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.CostSheetRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.WorkCenterRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.ProdProcessLineComputationService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.studio.db.AppProduction;
import com.axelor.utils.helpers.date.DurationHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CostSheetServiceImpl implements CostSheetService {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected UnitConversionService unitConversionService;
  protected CostSheetLineService costSheetLineService;
  protected BillOfMaterialRepository billOfMaterialRepo;
  protected AppBaseService appBaseService;
  protected AppProductionService appProductionService;
  protected ProdProcessLineComputationService prodProcessLineComputationService;
  protected Unit hourUnit;
  protected Unit cycleUnit;
  protected boolean manageResidualProductOnBom;
  protected CostSheet costSheet;

  @Inject
  public CostSheetServiceImpl(
      ProdProcessLineComputationService prodProcessLineComputationService,
      AppProductionService appProductionService,
      AppBaseService appBaseService,
      BillOfMaterialRepository billOfMaterialRepo,
      CostSheetLineService costSheetLineService,
      UnitConversionService unitConversionService) {
    this.prodProcessLineComputationService = prodProcessLineComputationService;
    this.appProductionService = appProductionService;
    this.appBaseService = appBaseService;
    this.billOfMaterialRepo = billOfMaterialRepo;
    this.costSheetLineService = costSheetLineService;
    this.unitConversionService = unitConversionService;
  }

  protected void init() {

    AppProduction appProduction = appProductionService.getAppProduction();
    this.hourUnit = appProductionService.getAppBase().getUnitHours();
    this.cycleUnit = appProduction.getCycleUnit();
    this.manageResidualProductOnBom = appProduction.getManageResidualProductOnBom();

    costSheet = new CostSheet();
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public CostSheet computeCostPrice(
      BillOfMaterial billOfMaterial, int origin, UnitCostCalculation unitCostCalculation)
      throws AxelorException {

    this.init();

    billOfMaterial.addCostSheetListItem(costSheet);

    BigDecimal calculationQty = billOfMaterial.getCalculationQty();

    if (calculationQty.compareTo(BigDecimal.ZERO) == 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.BILL_OF_MATERIAL_WRONG_CALCULATION_QTY));
    }

    CostSheetLine producedCostSheetLine =
        costSheetLineService.createProducedProductCostSheetLine(
            billOfMaterial.getProduct(), billOfMaterial.getUnit(), calculationQty);

    costSheet.addCostSheetLineListItem(producedCostSheetLine);
    costSheet.setCalculationTypeSelect(CostSheetRepository.CALCULATION_BILL_OF_MATERIAL);
    costSheet.setCalculationDate(appBaseService.getTodayDate(billOfMaterial.getCompany()));
    costSheet.setCalculationQty(calculationQty);
    Company company = billOfMaterial.getCompany();
    if (company != null && company.getCurrency() != null) {
      costSheet.setCurrency(company.getCurrency());
    }

    this._computeCostPrice(
        billOfMaterial.getCompany(),
        billOfMaterial,
        0,
        producedCostSheetLine,
        origin,
        unitCostCalculation);

    this.computeResidualProduct(billOfMaterial);

    billOfMaterial.setCostPrice(this.computeCostPrice(costSheet));

    billOfMaterialRepo.save(billOfMaterial);

    return costSheet;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public CostSheet computeCostPrice(
      ManufOrder manufOrder, int calculationTypeSelect, LocalDate calculationDate)
      throws AxelorException {
    this.init();

    List<CostSheet> costSheetList = manufOrder.getCostSheetList();
    LocalDate previousCostSheetDate = null;
    for (CostSheet costSheet : costSheetList) {
      if ((costSheet.getCalculationTypeSelect() == CostSheetRepository.CALCULATION_END_OF_PRODUCTION
              || costSheet.getCalculationTypeSelect()
                  == CostSheetRepository.CALCULATION_PARTIAL_END_OF_PRODUCTION)
          && costSheet.getCalculationDate() != null) {
        if (previousCostSheetDate == null) {
          previousCostSheetDate = costSheet.getCalculationDate();
        } else if (costSheet.getCalculationDate().isAfter(previousCostSheetDate)) {
          previousCostSheetDate = costSheet.getCalculationDate();
        }
      }
    }
    manufOrder.addCostSheetListItem(costSheet);

    costSheet.setCalculationTypeSelect(calculationTypeSelect);
    costSheet.setCalculationDate(
        calculationDate != null
            ? calculationDate
            : appBaseService.getTodayDate(manufOrder.getCompany()));

    BigDecimal producedQty =
        computeTotalProducedQty(
            manufOrder.getProduct(),
            manufOrder.getProducedStockMoveLineList(),
            costSheet.getCalculationDate(),
            previousCostSheetDate,
            costSheet.getCalculationTypeSelect());

    CostSheetLine producedCostSheetLine =
        costSheetLineService.createProducedProductCostSheetLine(
            manufOrder.getProduct(), manufOrder.getUnit(), producedQty);
    costSheet.addCostSheetLineListItem(producedCostSheetLine);

    Company company = manufOrder.getCompany();
    if (company != null && company.getCurrency() != null) {
      costSheet.setCurrency(company.getCurrency());
    }

    BigDecimal totalToProduceQty = getTotalToProduceQty(manufOrder);
    BigDecimal ratio = BigDecimal.ZERO;
    if (totalToProduceQty.compareTo(BigDecimal.ZERO) != 0) {
      ratio = producedQty.divide(totalToProduceQty, 5, RoundingMode.HALF_UP);
    }

    costSheet.setManufOrderProducedRatio(ratio);

    this.computeRealCostPrice(manufOrder, 0, producedCostSheetLine, previousCostSheetDate);

    this.computeRealResidualProduct(manufOrder);

    this.computeCostPrice(costSheet);
    manufOrder.setCostPrice(costSheet.getCostPrice());
    Beans.get(ManufOrderRepository.class).save(manufOrder);

    return costSheet;
  }

  protected void computeResidualProduct(BillOfMaterial billOfMaterial) throws AxelorException {

    if (this.manageResidualProductOnBom && billOfMaterial.getProdResidualProductList() != null) {

      BigDecimal qtyRatio = getQtyRatio(billOfMaterial);

      for (ProdResidualProduct prodResidualProduct : billOfMaterial.getProdResidualProductList()) {

        BigDecimal qty = prodResidualProduct.getQty().multiply(qtyRatio);

        CostSheetLine costSheetLine =
            costSheetLineService.createResidualProductCostSheetLine(
                prodResidualProduct.getProduct(),
                prodResidualProduct.getUnit(),
                qty,
                billOfMaterial.getCompany());

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
      Company company,
      BillOfMaterial billOfMaterial,
      int bomLevel,
      CostSheetLine parentCostSheetLine,
      int origin,
      UnitCostCalculation unitCostCalculation)
      throws AxelorException {

    bomLevel++;

    // Cout des composants
    this._computeToConsumeProduct(
        company, billOfMaterial, bomLevel, parentCostSheetLine, origin, unitCostCalculation);

    // Cout des operations
    this._computeProcess(
        billOfMaterial.getProdProcess(),
        billOfMaterial.getCalculationQty(),
        billOfMaterial.getProduct().getUnit(),
        bomLevel,
        parentCostSheetLine);
  }

  protected void _computeToConsumeProduct(
      Company company,
      BillOfMaterial billOfMaterial,
      int bomLevel,
      CostSheetLine parentCostSheetLine,
      int origin,
      UnitCostCalculation unitCostCalculation)
      throws AxelorException {

    if (billOfMaterial.getBillOfMaterialLineList() != null) {

      BigDecimal qtyRatio = getQtyRatio(billOfMaterial);

      for (BillOfMaterialLine billOfMaterialLine : billOfMaterial.getBillOfMaterialLineList()) {

        Product product = billOfMaterialLine.getProduct();
        BigDecimal qty = billOfMaterialLine.getQty().multiply(qtyRatio);
        if (product != null) {

          CostSheetLine costSheetLine =
              costSheetLineService.createConsumedProductCostSheetLine(
                  company,
                  product,
                  billOfMaterialLine.getUnit(),
                  bomLevel,
                  parentCostSheetLine,
                  qty,
                  origin,
                  unitCostCalculation);

          BigDecimal wasteRate = billOfMaterialLine.getWasteRate();

          if (wasteRate != null && wasteRate.compareTo(BigDecimal.ZERO) > 0) {
            costSheetLineService.createConsumedProductWasteCostSheetLine(
                company,
                product,
                billOfMaterialLine.getUnit(),
                bomLevel,
                parentCostSheetLine,
                qty,
                wasteRate,
                origin,
                unitCostCalculation);
          }

          if (billOfMaterialLine.getBillOfMaterial() != null) {
            this._computeCostPrice(
                company,
                billOfMaterialLine.getBillOfMaterial(),
                bomLevel,
                costSheetLine,
                origin,
                unitCostCalculation);
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

          if (workCenterTypeSelect == WorkCenterRepository.WORK_CENTER_TYPE_HUMAN
              || workCenterTypeSelect == WorkCenterRepository.WORK_CENTER_TYPE_BOTH) {

            this._computeHumanResourceCost(
                prodProcessLine,
                workCenter,
                producedQty,
                prodProcessLine.getHumanDuration(),
                pieceUnit,
                prodProcessLine.getPriority(),
                bomLevel,
                parentCostSheetLine);
          }
          if (workCenterTypeSelect == WorkCenterRepository.WORK_CENTER_TYPE_MACHINE
              || workCenterTypeSelect == WorkCenterRepository.WORK_CENTER_TYPE_BOTH) {

            this._computeMachineCost(
                prodProcessLine, producedQty, pieceUnit, bomLevel, parentCostSheetLine);
          }
        }
      }
    }
  }

  protected void _computeHumanResourceCost(
      ProdProcessLine prodProcessLine,
      WorkCenter workCenter,
      BigDecimal producedQty,
      Long humanDuration,
      Unit pieceUnit,
      int priority,
      int bomLevel,
      CostSheetLine parentCostSheetLine)
      throws AxelorException {

    int hrCostType =
        appProductionService.getIsCostPerProcessLine()
            ? prodProcessLine.getHrCostTypeSelect()
            : workCenter.getHrCostTypeSelect();
    BigDecimal costAmount =
        appProductionService.getIsCostPerProcessLine()
            ? prodProcessLine.getHrCostAmount()
            : workCenter.getHrCostAmount();

    if (hrCostType == WorkCenterRepository.COST_TYPE_PER_HOUR) {
      BigDecimal durationHours =
          BigDecimal.valueOf(humanDuration)
              .divide(
                  BigDecimal.valueOf(3600),
                  appProductionService.getNbDecimalDigitForUnitPrice(),
                  RoundingMode.HALF_UP);

      costSheetLineService.createWorkCenterHRCostSheetLine(
          workCenter,
          priority,
          bomLevel,
          parentCostSheetLine,
          durationHours,
          costAmount.multiply(durationHours),
          hourUnit);
    } else if (hrCostType == WorkCenterRepository.COST_TYPE_PER_PIECE) {
      BigDecimal costPrice = costAmount.multiply(producedQty);
      costSheetLineService.createWorkCenterHRCostSheetLine(
          workCenter, priority, bomLevel, parentCostSheetLine, producedQty, costPrice, pieceUnit);
    }
  }

  protected void _computeMachineCost(
      ProdProcessLine prodProcessLine,
      BigDecimal producedQty,
      Unit pieceUnit,
      int bomLevel,
      CostSheetLine parentCostSheetLine)
      throws AxelorException {

    WorkCenter workCenter = prodProcessLine.getWorkCenter();
    if (prodProcessLine.getWorkCenter() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.PROD_PROCESS_LINE_MISSING_WORK_CENTER),
          prodProcessLine.getProdProcess() != null
              ? prodProcessLine.getProdProcess().getCode()
              : "null",
          prodProcessLine.getName());
    }
    int costType =
        appProductionService.getIsCostPerProcessLine()
            ? prodProcessLine.getCostTypeSelect()
            : workCenter.getCostTypeSelect();
    BigDecimal costAmount =
        appProductionService.getIsCostPerProcessLine()
            ? prodProcessLine.getCostAmount()
            : workCenter.getCostAmount();
    if (costType == WorkCenterRepository.COST_TYPE_PER_CYCLE) {

      BigDecimal nbCycle =
          prodProcessLineComputationService.getNbCycle(prodProcessLine, producedQty);
      BigDecimal costPrice = costAmount.multiply(nbCycle);

      costSheetLineService.createWorkCenterMachineCostSheetLine(
          workCenter,
          prodProcessLine.getPriority(),
          bomLevel,
          parentCostSheetLine,
          nbCycle,
          costPrice,
          cycleUnit);

    } else if (costType == WorkCenterRepository.COST_TYPE_PER_HOUR) {

      BigDecimal machineDuration =
          prodProcessLineComputationService.getHourMachineDuration(
              prodProcessLine,
              prodProcessLineComputationService.getNbCycle(prodProcessLine, producedQty));
      BigDecimal costPrice = costAmount.multiply(machineDuration);

      costSheetLineService.createWorkCenterMachineCostSheetLine(
          workCenter,
          prodProcessLine.getPriority(),
          bomLevel,
          parentCostSheetLine,
          machineDuration,
          costPrice,
          hourUnit);

    } else if (costType == WorkCenterRepository.COST_TYPE_PER_PIECE) {
      BigDecimal costPrice = costAmount.multiply(producedQty);

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

  protected void computeRealResidualProduct(ManufOrder manufOrder) throws AxelorException {
    for (StockMoveLine stockMoveLine : manufOrder.getProducedStockMoveLineList()) {
      if (stockMoveLine.getProduct() != null
          && manufOrder.getProduct() != null
          && (!stockMoveLine.getProduct().equals(manufOrder.getProduct()))) {
        CostSheetLine costSheetLine =
            costSheetLineService.createResidualProductCostSheetLine(
                stockMoveLine.getProduct(),
                stockMoveLine.getUnit(),
                stockMoveLine.getRealQty(),
                manufOrder.getCompany());
        costSheet.addCostSheetLineListItem(costSheetLine);
      }
    }
  }

  protected void computeRealCostPrice(
      ManufOrder manufOrder,
      int bomLevel,
      CostSheetLine parentCostSheetLine,
      LocalDate previousCostSheetDate)
      throws AxelorException {

    bomLevel++;

    this.computeConsumedProduct(manufOrder, bomLevel, parentCostSheetLine, previousCostSheetDate);
    BigDecimal producedQty = parentCostSheetLine.getConsumptionQty();
    this.computeRealProcess(
        manufOrder.getOperationOrderList(),
        manufOrder.getProduct().getUnit(),
        producedQty,
        bomLevel,
        parentCostSheetLine,
        previousCostSheetDate);
  }

  protected void computeConsumedProduct(
      ManufOrder manufOrder,
      int bomLevel,
      CostSheetLine parentCostSheetLine,
      LocalDate previousCostSheetDate)
      throws AxelorException {

    BigDecimal ratio = costSheet.getManufOrderProducedRatio();

    if (manufOrder.getIsConsProOnOperation()) {
      for (OperationOrder operation : manufOrder.getOperationOrderList()) {

        this.computeConsumedProduct(
            bomLevel,
            previousCostSheetDate,
            parentCostSheetLine,
            operation.getConsumedStockMoveLineList(),
            operation.getToConsumeProdProductList(),
            ratio);
      }
    } else {

      this.computeConsumedProduct(
          bomLevel,
          previousCostSheetDate,
          parentCostSheetLine,
          manufOrder.getConsumedStockMoveLineList(),
          manufOrder.getToConsumeProdProductList(),
          ratio);
    }
  }

  protected void computeConsumedProduct(
      int bomLevel,
      LocalDate previousCostSheetDate,
      CostSheetLine parentCostSheetLine,
      List<StockMoveLine> consumedStockMoveLineList,
      List<ProdProduct> toConsumeProdProductList,
      BigDecimal ratio)
      throws AxelorException {

    CostSheet parentCostSheet = parentCostSheetLine.getCostSheet();
    int calculationTypeSelect = parentCostSheet.getCalculationTypeSelect();
    LocalDate calculationDate = parentCostSheet.getCalculationDate();

    Map<List<Object>, BigDecimal> consumedStockMoveLinePerProductAndUnit =
        getTotalQtyPerProductAndUnit(
            consumedStockMoveLineList,
            calculationDate,
            previousCostSheetDate,
            calculationTypeSelect);

    for (List<Object> keys : consumedStockMoveLinePerProductAndUnit.keySet()) {

      Iterator<Object> iterator = keys.iterator();
      Product product = (Product) iterator.next();
      Unit unit = (Unit) iterator.next();
      BigDecimal realQty = consumedStockMoveLinePerProductAndUnit.get(keys);

      if (product == null) {
        continue;
      }

      costSheetLineService.createConsumedProductCostSheetLine(
          parentCostSheet.getManufOrder().getCompany(),
          product,
          unit,
          bomLevel,
          parentCostSheetLine,
          realQty,
          CostSheetService.ORIGIN_MANUF_ORDER,
          null);
    }
  }

  protected BigDecimal computeTotalProducedQty(
      Product producedProduct,
      List<StockMoveLine> producedStockMoveLineList,
      LocalDate calculationDate,
      LocalDate previousCostSheetDate,
      int calculationTypeSelect)
      throws AxelorException {

    BigDecimal totalQty = BigDecimal.ZERO;

    Map<List<Object>, BigDecimal> producedStockMoveLinePerProductAndUnit =
        getTotalQtyPerProductAndUnit(
            producedStockMoveLineList,
            calculationDate,
            previousCostSheetDate,
            calculationTypeSelect);

    for (List<Object> keys : producedStockMoveLinePerProductAndUnit.keySet()) {

      Iterator<Object> iterator = keys.iterator();
      Product product = (Product) iterator.next();
      Unit unit = (Unit) iterator.next();
      BigDecimal realQty = producedStockMoveLinePerProductAndUnit.get(keys);

      if (product == null || !product.equals(producedProduct)) {
        continue;
      }

      totalQty =
          totalQty.add(
              unitConversionService.convert(
                  unit, costSheet.getManufOrder().getUnit(), realQty, realQty.scale(), product));
    }

    return totalQty;
  }

  protected BigDecimal computeTotalQtyPerUnit(
      List<ProdProduct> prodProductList, Product product, Unit unit) {

    BigDecimal totalQty = BigDecimal.ZERO;

    for (ProdProduct prodProduct : prodProductList) {
      if (product.equals(prodProduct.getProduct()) && unit.equals(prodProduct.getUnit())) {
        totalQty = totalQty.add(prodProduct.getQty());
      }
    }

    return totalQty;
  }

  protected Map<List<Object>, BigDecimal> getTotalQtyPerProductAndUnit(
      List<StockMoveLine> stockMoveLineList,
      LocalDate calculationDate,
      LocalDate previousCostSheetDate,
      int calculationType) {

    Map<List<Object>, BigDecimal> stockMoveLinePerProductAndUnitMap = new HashMap<>();

    if (stockMoveLineList == null) {
      return stockMoveLinePerProductAndUnitMap;
    }

    for (StockMoveLine stockMoveLine : stockMoveLineList) {

      StockMove stockMove = stockMoveLine.getStockMove();

      if (stockMove == null
          || StockMoveRepository.STATUS_REALIZED
              != stockMoveLine.getStockMove().getStatusSelect()) {
        continue;
      }

      if ((calculationType == CostSheetRepository.CALCULATION_PARTIAL_END_OF_PRODUCTION
              || calculationType == CostSheetRepository.CALCULATION_END_OF_PRODUCTION)
          && previousCostSheetDate != null
          && !previousCostSheetDate.isBefore(stockMove.getRealDate())) {
        continue;

      } else if (calculationType == CostSheetRepository.CALCULATION_WORK_IN_PROGRESS
          && calculationDate.isBefore(stockMove.getRealDate())) {
        continue;
      }

      Product productKey = stockMoveLine.getProduct();
      Unit unitKey = stockMoveLine.getUnit();

      List<Object> keys = new ArrayList<Object>();
      keys.add(productKey);
      keys.add(unitKey);

      BigDecimal qty = stockMoveLinePerProductAndUnitMap.get(keys);

      if (qty == null) {
        qty = BigDecimal.ZERO;
      }

      stockMoveLinePerProductAndUnitMap.put(keys, qty.add(stockMoveLine.getRealQty()));
    }

    return stockMoveLinePerProductAndUnitMap;
  }

  protected void computeRealProcess(
      List<OperationOrder> operationOrders,
      Unit pieceUnit,
      BigDecimal producedQty,
      int bomLevel,
      CostSheetLine parentCostSheetLine,
      LocalDate previousCostSheetDate)
      throws AxelorException {
    for (OperationOrder operationOrder : operationOrders) {

      WorkCenter workCenter = operationOrder.getWorkCenter();
      if (workCenter == null) {
        continue;
      }
      int workCenterTypeSelect = workCenter.getWorkCenterTypeSelect();
      if (workCenterTypeSelect == WorkCenterRepository.WORK_CENTER_TYPE_HUMAN
          || workCenterTypeSelect == WorkCenterRepository.WORK_CENTER_TYPE_BOTH) {

        this.computeRealHumanResourceCost(
            operationOrder,
            operationOrder.getPriority(),
            bomLevel,
            parentCostSheetLine,
            previousCostSheetDate);
      }
      if (workCenterTypeSelect == WorkCenterRepository.WORK_CENTER_TYPE_MACHINE
          || workCenterTypeSelect == WorkCenterRepository.WORK_CENTER_TYPE_BOTH) {

        this.computeRealMachineCost(
            operationOrder,
            workCenter,
            producedQty,
            pieceUnit,
            bomLevel,
            parentCostSheetLine,
            previousCostSheetDate);
      }
    }
  }

  protected void computeRealHumanResourceCost(
      OperationOrder operationOrder,
      int priority,
      int bomLevel,
      CostSheetLine parentCostSheetLine,
      LocalDate previousCostSheetDate)
      throws AxelorException {
    BigDecimal duration = BigDecimal.ZERO;

    if (parentCostSheetLine.getCostSheet().getCalculationTypeSelect()
            == CostSheetRepository.CALCULATION_END_OF_PRODUCTION
        || parentCostSheetLine.getCostSheet().getCalculationTypeSelect()
            == CostSheetRepository.CALCULATION_PARTIAL_END_OF_PRODUCTION) {
      Period period =
          previousCostSheetDate != null
              ? Period.between(
                  parentCostSheetLine.getCostSheet().getCalculationDate(), previousCostSheetDate)
              : null;
      duration =
          period != null
              ? new BigDecimal(period.getDays() * 24)
              : new BigDecimal(operationOrder.getRealDuration());
    } else if (parentCostSheetLine.getCostSheet().getCalculationTypeSelect()
        == CostSheetRepository.CALCULATION_WORK_IN_PROGRESS) {

      BigDecimal ratio = costSheet.getManufOrderProducedRatio();

      BigDecimal plannedDuration =
          new BigDecimal(
                  DurationHelper.getSecondsDuration(
                      Duration.between(
                          operationOrder.getPlannedStartDateT(),
                          operationOrder.getPlannedEndDateT())))
              .multiply(ratio);

      Long totalPlannedDuration = 0L;
      for (OperationOrder manufOperationOrder :
          operationOrder.getManufOrder().getOperationOrderList()) {
        if (manufOperationOrder.equals(operationOrder)) {
          totalPlannedDuration += manufOperationOrder.getPlannedDuration();
        }
      }
      duration = (new BigDecimal(totalPlannedDuration).subtract(plannedDuration)).abs();
    }
    this.computeRealHumanResourceCost(
        operationOrder.getProdProcessLine(),
        operationOrder.getWorkCenter(),
        priority,
        bomLevel,
        parentCostSheetLine,
        duration);
  }

  protected void computeRealHumanResourceCost(
      WorkCenter workCenter,
      int priority,
      int bomLevel,
      CostSheetLine parentCostSheetLine,
      Long realDuration)
      throws AxelorException {
    BigDecimal costPerHour = workCenter.getCostAmount();
    BigDecimal durationHours =
        new BigDecimal(realDuration)
            .divide(
                new BigDecimal(3600),
                appProductionService.getNbDecimalDigitForUnitPrice(),
                RoundingMode.HALF_UP);

    costSheetLineService.createWorkCenterHRCostSheetLine(
        workCenter,
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
      CostSheetLine parentCostSheetLine,
      LocalDate previousCostSheetDate)
      throws AxelorException {
    int costType =
        appProductionService.getIsCostPerProcessLine()
            ? operationOrder.getProdProcessLine().getCostTypeSelect()
            : workCenter.getCostTypeSelect();
    BigDecimal costAmount =
        appProductionService.getIsCostPerProcessLine()
            ? operationOrder.getProdProcessLine().getCostAmount()
            : workCenter.getCostAmount();

    if (costType == WorkCenterRepository.COST_TYPE_PER_CYCLE) {
      costSheetLineService.createWorkCenterMachineCostSheetLine(
          workCenter,
          operationOrder.getPriority(),
          bomLevel,
          parentCostSheetLine,
          prodProcessLineComputationService.computeNbCycle(
              workCenter.getMaxCapacityPerCycle(), producedQty),
          costAmount,
          cycleUnit);
    } else if (costType == WorkCenterRepository.COST_TYPE_PER_HOUR) {
      BigDecimal qty = BigDecimal.ZERO;

      if (workCenter.getIsRevaluationAtActualPrices()) {

        qty =
            new BigDecimal(operationOrder.getRealDuration())
                .divide(
                    new BigDecimal(3600),
                    appProductionService.getNbDecimalDigitForUnitPrice(),
                    RoundingMode.HALF_UP);
      } else {

        BigDecimal manufOrderQty = operationOrder.getManufOrder().getQty();
        BigDecimal durationPerCycle =
            new BigDecimal(workCenter.getDurationPerCycle())
                .divide(
                    new BigDecimal(3600),
                    appProductionService.getNbDecimalDigitForUnitPrice(),
                    RoundingMode.HALF_UP);

        if (manufOrderQty.compareTo(workCenter.getMinCapacityPerCycle()) == 1) {
          BigDecimal maxCapacityPerCycle =
              workCenter.getMaxCapacityPerCycle().compareTo(BigDecimal.ZERO) == 0
                  ? BigDecimal.ONE
                  : workCenter.getMaxCapacityPerCycle();
          qty =
              manufOrderQty
                  .divide(
                      maxCapacityPerCycle,
                      appProductionService.getNbDecimalDigitForUnitPrice(),
                      RoundingMode.HALF_UP)
                  .multiply(durationPerCycle)
                  .setScale(appBaseService.getNbDecimalDigitForQty(), RoundingMode.HALF_UP);
        } else {
          qty = durationPerCycle;
        }
      }
      qty =
          qty.multiply(
              costSheet
                  .getManufOrderProducedRatio()); // Using produced ratio for prorata calculation
      BigDecimal costPrice = costAmount.multiply(qty);
      costSheetLineService.createWorkCenterMachineCostSheetLine(
          workCenter,
          operationOrder.getPriority(),
          bomLevel,
          parentCostSheetLine,
          qty,
          costPrice,
          hourUnit);
    } else if (costType == WorkCenterRepository.COST_TYPE_PER_PIECE) {

      BigDecimal costPrice = costAmount.multiply(producedQty);
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

  protected BigDecimal getTotalToProduceQty(ManufOrder manufOrder) throws AxelorException {

    BigDecimal totalProducedQty = BigDecimal.ZERO;

    for (StockMoveLine stockMoveLine : manufOrder.getProducedStockMoveLineList()) {

      if (stockMoveLine.getUnit().equals(manufOrder.getUnit())
          && (stockMoveLine.getStockMove().getStatusSelect() == StockMoveRepository.STATUS_PLANNED
              || stockMoveLine.getStockMove().getStatusSelect()
                  == StockMoveRepository.STATUS_REALIZED)) {
        Product product = stockMoveLine.getProduct();
        totalProducedQty =
            totalProducedQty.add(
                unitConversionService.convert(
                    stockMoveLine.getUnit(),
                    costSheet.getManufOrder().getUnit(),
                    stockMoveLine.getQty(),
                    stockMoveLine.getQty().scale(),
                    product));
      }
    }

    return totalProducedQty;
  }

  /*
   * Changing the type of realDuration from Long to BigDecimal to use it with manufOrderProducedRatio
   */
  protected void computeRealHumanResourceCost(
      ProdProcessLine prodProcessLine,
      WorkCenter workCenter,
      int priority,
      int bomLevel,
      CostSheetLine parentCostSheetLine,
      BigDecimal realDuration)
      throws AxelorException {

    BigDecimal costPerHour =
        appProductionService.getIsCostPerProcessLine()
            ? prodProcessLine.getHrCostAmount()
            : workCenter.getHrCostAmount();
    BigDecimal durationHours =
        realDuration.divide(
            new BigDecimal(3600),
            appProductionService.getNbDecimalDigitForUnitPrice(),
            RoundingMode.HALF_UP);

    costSheetLineService.createWorkCenterHRCostSheetLine(
        workCenter,
        priority,
        bomLevel,
        parentCostSheetLine,
        durationHours,
        costPerHour.multiply(durationHours),
        hourUnit);
  }

  @Override
  public BigDecimal getQtyRatio(BillOfMaterial billOfMaterial) {
    BigDecimal bomQty = billOfMaterial.getQty();
    if (bomQty.compareTo(BigDecimal.ZERO) == 0) {
      return BigDecimal.ZERO;
    }

    BigDecimal calculationQty = billOfMaterial.getCalculationQty();
    return calculationQty.divide(bomQty, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
  }
}
