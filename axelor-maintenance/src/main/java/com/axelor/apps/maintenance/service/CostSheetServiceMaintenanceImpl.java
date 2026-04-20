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
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.CostSheetLine;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.service.ProdProcessLineComputationService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.costsheet.CostSheetLineService;
import com.axelor.apps.production.service.costsheet.CostSheetServiceImpl;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Override of CostSheetService for maintenance orders (typeSelect = 3).
 *
 * <p>For maintenance BOMs and ManufOrders, the "produced product" line is excluded from the cost
 * sheet since maintenance does not produce a finished product. Only component, human resource, and
 * machine cost lines are kept, valued at component cost price.
 */
public class CostSheetServiceMaintenanceImpl extends CostSheetServiceImpl {

  @Inject
  public CostSheetServiceMaintenanceImpl(
      ProdProcessLineComputationService prodProcessLineComputationService,
      AppProductionService appProductionService,
      AppBaseService appBaseService,
      BillOfMaterialRepository billOfMaterialRepo,
      CostSheetLineService costSheetLineService,
      UnitConversionService unitConversionService,
      StockMoveLineRepository stockMoveLineRepository,
      ManufOrderRepository manufOrderRepo) {
    super(
        prodProcessLineComputationService,
        appProductionService,
        appBaseService,
        billOfMaterialRepo,
        costSheetLineService,
        unitConversionService,
        stockMoveLineRepository,
        manufOrderRepo);
  }

  @Override
  protected CostSheetLine createAndAddRootCostSheetLine(
      BillOfMaterial billOfMaterial, BigDecimal calculationQty) throws AxelorException {
    if (!isMaintenance(billOfMaterial)) {
      return super.createAndAddRootCostSheetLine(billOfMaterial, calculationQty);
    }
    CostSheetLine rootCostSheetLine = new CostSheetLine();
    costSheet.addCostSheetLineListItem(rootCostSheetLine);
    return rootCostSheetLine;
  }

  @Override
  protected CostSheetLine createAndAddRootCostSheetLine(
      ManufOrder manufOrder, BigDecimal producedQty) throws AxelorException {
    if (!isMaintenance(manufOrder)) {
      return super.createAndAddRootCostSheetLine(manufOrder, producedQty);
    }
    CostSheetLine rootCostSheetLine = new CostSheetLine();
    costSheet.addCostSheetLineListItem(rootCostSheetLine);
    return rootCostSheetLine;
  }

  @Override
  protected Product getComponentValuationOverrideProduct(BillOfMaterial billOfMaterial) {
    if (!isMaintenance(billOfMaterial)) {
      return super.getComponentValuationOverrideProduct(billOfMaterial);
    }
    return buildMaintenanceValuationProduct();
  }

  @Override
  protected Product getComponentValuationOverrideProduct(ManufOrder manufOrder) {
    if (!isMaintenance(manufOrder)) {
      return super.getComponentValuationOverrideProduct(manufOrder);
    }
    return buildMaintenanceValuationProduct();
  }

  @Override
  protected BigDecimal computeManufOrderProducedRatio(ManufOrder manufOrder, BigDecimal producedQty)
      throws AxelorException {
    if (!isMaintenance(manufOrder)) {
      return super.computeManufOrderProducedRatio(manufOrder, producedQty);
    }
    return BigDecimal.ONE;
  }

  @Override
  protected void computeResidualProduct(BillOfMaterial billOfMaterial) throws AxelorException {
    if (!isMaintenance(billOfMaterial)) {
      super.computeResidualProduct(billOfMaterial);
    }
  }

  @Override
  protected void computeRealResidualProduct(ManufOrder manufOrder) throws AxelorException {
    if (!isMaintenance(manufOrder)) {
      super.computeRealResidualProduct(manufOrder);
    }
  }

  @Override
  protected void finalizeRootCostSheetLine(
      CostSheetLine rootCostSheetLine, BillOfMaterial billOfMaterial) {
    if (!isMaintenance(billOfMaterial)) {
      super.finalizeRootCostSheetLine(rootCostSheetLine, billOfMaterial);
      return;
    }
    promoteChildrenAndDiscardRoot(rootCostSheetLine);
  }

  @Override
  protected void finalizeRootCostSheetLine(CostSheetLine rootCostSheetLine, ManufOrder manufOrder) {
    if (!isMaintenance(manufOrder)) {
      super.finalizeRootCostSheetLine(rootCostSheetLine, manufOrder);
      return;
    }
    promoteChildrenAndDiscardRoot(rootCostSheetLine);
  }

  protected boolean isMaintenance(BillOfMaterial billOfMaterial) {
    return billOfMaterial.getTypeSelect() != null
        && billOfMaterial.getTypeSelect() == ManufOrderRepository.TYPE_MAINTENANCE;
  }

  protected boolean isMaintenance(ManufOrder manufOrder) {
    return manufOrder.getTypeSelect() != null
        && manufOrder.getTypeSelect() == ManufOrderRepository.TYPE_MAINTENANCE;
  }

  protected Product buildMaintenanceValuationProduct() {
    Product valuationProduct = new Product();
    valuationProduct.setBomCompValuMethodSelect(ProductRepository.COMPONENTS_VALUATION_METHOD_COST);
    valuationProduct.setManufOrderCompValuMethodSelect(
        ProductRepository.COMPONENTS_VALUATION_METHOD_COST);
    return valuationProduct;
  }

  protected void promoteChildrenAndDiscardRoot(CostSheetLine rootCostSheetLine) {
    costSheet.getCostSheetLineList().remove(rootCostSheetLine);
    if (rootCostSheetLine.getCostSheetLineList() != null) {
      for (CostSheetLine child : new ArrayList<>(rootCostSheetLine.getCostSheetLineList())) {
        child.setParentCostSheetLine(null);
        costSheet.addCostSheetLineListItem(child);
      }
    }
  }
}
