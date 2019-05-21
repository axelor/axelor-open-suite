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
package com.axelor.apps.production.service.costsheet;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.production.db.CostSheetGroup;
import com.axelor.apps.production.db.CostSheetLine;
import com.axelor.apps.production.db.ProdHumanResource;
import com.axelor.apps.production.db.UnitCostCalculation;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;

public interface CostSheetLineService {

  public CostSheetLine createCostSheetLine(
      String name,
      String code,
      int bomLevel,
      BigDecimal consumptionQty,
      BigDecimal costPrice,
      CostSheetGroup costSheetGroup,
      Product product,
      int typeSelect,
      int typeSelectIcon,
      Unit unit,
      WorkCenter workCenter,
      CostSheetLine parentCostSheetLine);

  public CostSheetLine createProducedProductCostSheetLine(
      Product product, Unit unit, BigDecimal consumptionQty);

  public CostSheetLine createResidualProductCostSheetLine(
      Product product, Unit unit, BigDecimal consumptionQty) throws AxelorException;

  public CostSheetLine createConsumedProductCostSheetLine(
      Company company,
      Product product,
      Unit unit,
      int bomLevel,
      CostSheetLine parentCostSheetLine,
      BigDecimal consumptionQty,
      int origin,
      UnitCostCalculation unitCostCalculation)
      throws AxelorException;

  public CostSheetLine createConsumedProductWasteCostSheetLine(
      Company company,
      Product product,
      Unit unit,
      int bomLevel,
      CostSheetLine parentCostSheetLine,
      BigDecimal consumptionQty,
      BigDecimal wasteRate,
      int origin,
      UnitCostCalculation unitCostCalculation)
      throws AxelorException;

  public CostSheetLine createWorkCenterHRCostSheetLine(
      WorkCenter workCenter,
      ProdHumanResource prodHumanResource,
      int priority,
      int bomLevel,
      CostSheetLine parentCostSheetLine,
      BigDecimal consumptionQty,
      BigDecimal costPrice,
      Unit unit);

  public CostSheetLine createWorkCenterMachineCostSheetLine(
      WorkCenter workCenter,
      int priority,
      int bomLevel,
      CostSheetLine parentCostSheetLine,
      BigDecimal consumptionQty,
      BigDecimal costPrice,
      Unit unit);
}
