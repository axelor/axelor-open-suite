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
package com.axelor.apps.maintenance.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.CostSheet;
import com.axelor.apps.production.db.CostSheetLine;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.UnitCostCalculation;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.CostSheetRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.ProdProcessLineComputationService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.costsheet.CostSheetLineService;
import com.axelor.apps.production.service.costsheet.CostSheetServiceImpl;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Override of CostSheetService for maintenance orders (typeSelect = 3).
 *
 * <p>For maintenance BOMs and ManufOrders, the "produced product" line is excluded from the cost
 * sheet since maintenance does not produce a finished product. Only component, human resource, and
 * machine cost lines are kept.
 */
public class CostSheetServiceMaintenanceImpl extends CostSheetServiceImpl {

  protected boolean maintenanceContext;

  @Inject
  public CostSheetServiceMaintenanceImpl(
      ProdProcessLineComputationService prodProcessLineComputationService,
      AppProductionService appProductionService,
      AppBaseService appBaseService,
      BillOfMaterialRepository billOfMaterialRepo,
      CostSheetLineService costSheetLineService,
      UnitConversionService unitConversionService,
      StockMoveLineRepository stockMoveLineRepository) {
    super(
        prodProcessLineComputationService,
        appProductionService,
        appBaseService,
        billOfMaterialRepo,
        costSheetLineService,
        unitConversionService,
        stockMoveLineRepository);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public CostSheet computeCostPrice(
      BillOfMaterial billOfMaterial, int origin, UnitCostCalculation unitCostCalculation)
      throws AxelorException {

    if (billOfMaterial.getTypeSelect() == null
        || billOfMaterial.getTypeSelect() != ManufOrderRepository.TYPE_MAINTENANCE) {
      return super.computeCostPrice(billOfMaterial, origin, unitCostCalculation);
    }

    return computeMaintenanceBomCostPrice(billOfMaterial, origin, unitCostCalculation);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public CostSheet computeCostPrice(
      ManufOrder manufOrder, int calculationTypeSelect, LocalDate calculationDate)
      throws AxelorException {

    if (manufOrder.getTypeSelect() == null
        || manufOrder.getTypeSelect() != ManufOrderRepository.TYPE_MAINTENANCE) {
      return super.computeCostPrice(manufOrder, calculationTypeSelect, calculationDate);
    }

    return computeMaintenanceManufOrderCostPrice(
        manufOrder, calculationTypeSelect, calculationDate);
  }

  protected CostSheet computeMaintenanceBomCostPrice(
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

    costSheet.setCalculationTypeSelect(CostSheetRepository.CALCULATION_BILL_OF_MATERIAL);
    costSheet.setCalculationDate(appBaseService.getTodayDate(billOfMaterial.getCompany()));
    costSheet.setCalculationQty(calculationQty);

    Company company = billOfMaterial.getCompany();
    if (company != null && company.getCurrency() != null) {
      costSheet.setCurrency(company.getCurrency());
    }

    CostSheetLine tempRoot = new CostSheetLine();
    costSheet.addCostSheetLineListItem(tempRoot);

    this.maintenanceContext = true;
    try {
      this._computeCostPrice(company, billOfMaterial, 0, tempRoot, origin, unitCostCalculation);
    } finally {
      this.maintenanceContext = false;
    }

    costSheet.getCostSheetLineList().remove(tempRoot);
    if (tempRoot.getCostSheetLineList() != null) {
      for (CostSheetLine child : new ArrayList<>(tempRoot.getCostSheetLineList())) {
        child.setParentCostSheetLine(null);
        costSheet.addCostSheetLineListItem(child);
      }
    }

    BigDecimal qtyRatio = getQtyRatio(billOfMaterial);
    BigDecimal costPrice =
        this.computeCostPrice(costSheet)
            .divide(qtyRatio, appBaseService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
    costSheet.setCostPrice(costPrice);
    billOfMaterial.setCostPrice(costPrice);
    billOfMaterialRepo.save(billOfMaterial);

    return costSheet;
  }

  protected CostSheet computeMaintenanceManufOrderCostPrice(
      ManufOrder manufOrder, int calculationTypeSelect, LocalDate calculationDate)
      throws AxelorException {

    this.init();

    manufOrder.addCostSheetListItem(costSheet);

    costSheet.setCalculationTypeSelect(calculationTypeSelect);
    costSheet.setCalculationDate(
        calculationDate != null
            ? calculationDate
            : appBaseService.getTodayDate(manufOrder.getCompany()));

    costSheet.setManufOrderProducedRatio(BigDecimal.ONE);

    Company company = manufOrder.getCompany();
    if (company != null && company.getCurrency() != null) {
      costSheet.setCurrency(company.getCurrency());
    }

    CostSheetLine tempRoot = new CostSheetLine();
    costSheet.addCostSheetLineListItem(tempRoot);

    this.maintenanceContext = true;
    try {
      this.computeRealCostPrice(manufOrder, 0, tempRoot, null);
    } finally {
      this.maintenanceContext = false;
    }

    costSheet.getCostSheetLineList().remove(tempRoot);
    if (tempRoot.getCostSheetLineList() != null) {
      for (CostSheetLine child : new ArrayList<>(tempRoot.getCostSheetLineList())) {
        child.setParentCostSheetLine(null);
        costSheet.addCostSheetLineListItem(child);
      }
    }

    BigDecimal costPrice = this.computeCostPrice(costSheet);
    costSheet.setCostPrice(costPrice);
    manufOrder.setCostPrice(costPrice);
    Beans.get(ManufOrderRepository.class).save(manufOrder);

    return costSheet;
  }

  @Override
  protected void _computeCostPrice(
      Company company,
      BillOfMaterial billOfMaterial,
      int bomLevel,
      CostSheetLine parentCostSheetLine,
      int origin,
      UnitCostCalculation unitCostCalculation,
      BigDecimal calculationQty)
      throws AxelorException {

    bomLevel++;

    this._computeToConsumeProduct(
        company,
        billOfMaterial,
        bomLevel,
        parentCostSheetLine,
        origin,
        unitCostCalculation,
        getQtyRatio(billOfMaterial, billOfMaterial.getQty(), calculationQty));

    Unit pieceUnit;
    if (billOfMaterial.getProduct() != null) {
      pieceUnit = billOfMaterial.getProduct().getUnit();
    } else {
      pieceUnit = billOfMaterial.getUnit();
    }

    this._computeProcess(
        billOfMaterial.getProdProcess(), calculationQty, pieceUnit, bomLevel, parentCostSheetLine);
  }

  @Override
  protected void _computeToConsumeProduct(
      Company company,
      BillOfMaterial billOfMaterial,
      int bomLevel,
      CostSheetLine parentCostSheetLine,
      int origin,
      UnitCostCalculation unitCostCalculation,
      BigDecimal qtyRatio)
      throws AxelorException {

    if (!maintenanceContext) {
      super._computeToConsumeProduct(
          company,
          billOfMaterial,
          bomLevel,
          parentCostSheetLine,
          origin,
          unitCostCalculation,
          qtyRatio);
      return;
    }

    Product defaultValuationProduct = new Product();
    defaultValuationProduct.setBomCompValuMethodSelect(
        ProductRepository.COMPONENTS_VALUATION_METHOD_COST);
    defaultValuationProduct.setManufOrderCompValuMethodSelect(
        ProductRepository.COMPONENTS_VALUATION_METHOD_COST);
    parentCostSheetLine.setProduct(defaultValuationProduct);

    try {
      super._computeToConsumeProduct(
          company,
          billOfMaterial,
          bomLevel,
          parentCostSheetLine,
          origin,
          unitCostCalculation,
          qtyRatio);
    } finally {
      parentCostSheetLine.setProduct(null);
    }
  }

  @Override
  protected void computeRealCostPrice(
      ManufOrder manufOrder,
      int bomLevel,
      CostSheetLine parentCostSheetLine,
      LocalDate previousCostSheetDate)
      throws AxelorException {

    if (!maintenanceContext) {
      super.computeRealCostPrice(manufOrder, bomLevel, parentCostSheetLine, previousCostSheetDate);
      return;
    }

    bomLevel++;

    Product defaultValuationProduct = new Product();
    defaultValuationProduct.setBomCompValuMethodSelect(
        ProductRepository.COMPONENTS_VALUATION_METHOD_COST);
    defaultValuationProduct.setManufOrderCompValuMethodSelect(
        ProductRepository.COMPONENTS_VALUATION_METHOD_COST);
    parentCostSheetLine.setProduct(defaultValuationProduct);

    try {
      this.computeConsumedProduct(manufOrder, bomLevel, parentCostSheetLine, previousCostSheetDate);
      this.computeOutSourcedProduct(manufOrder, bomLevel, parentCostSheetLine);
    } finally {
      parentCostSheetLine.setProduct(null);
    }

    BigDecimal producedQty = parentCostSheetLine.getConsumptionQty();

    Unit pieceUnit = null;
    if (manufOrder.getBillOfMaterial() != null) {
      pieceUnit = manufOrder.getBillOfMaterial().getUnit();
    }

    this.computeRealProcess(
        manufOrder.getOperationOrderList(),
        pieceUnit,
        producedQty,
        bomLevel,
        parentCostSheetLine,
        previousCostSheetDate);
  }
}
