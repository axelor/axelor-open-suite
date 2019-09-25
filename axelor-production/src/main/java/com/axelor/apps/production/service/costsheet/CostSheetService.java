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

import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.CostSheet;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.UnitCostCalculation;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;

public interface CostSheetService {

  public static final int ORIGIN_BILL_OF_MATERIAL = 0;
  public static final int ORIGIN_MANUF_ORDER = 1;
  public static final int ORIGIN_BULK_UNIT_COST_CALCULATION = 2;

  /**
   * @param billOfMaterial
   * @param origin 0 : ORIGIN_BILL_OF_MATERIAL 1 : ORIGIN_MANUF_ORDER 2 :
   *     ORIGIN_BULK_UNIT_COST_CALCULATION
   * @param unitCostCalculation Required if origin = ORIGIN_BULK_UNIT_COST_CALCULATION
   * @return
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  public CostSheet computeCostPrice(
      BillOfMaterial billOfMaterial, int origin, UnitCostCalculation unitCostCalculation)
      throws AxelorException;

  @Transactional(rollbackOn = {Exception.class})
  public CostSheet computeCostPrice(
      ManufOrder manufOrder, int calculationTypeSelect, LocalDate calculationDate)
      throws AxelorException;
}
