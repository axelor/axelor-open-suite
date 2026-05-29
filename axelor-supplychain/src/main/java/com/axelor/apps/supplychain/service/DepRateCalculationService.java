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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.supplychain.db.UnitCostCalcLine;
import com.axelor.apps.supplychain.db.UnitCostCalculation;

public interface DepRateCalculationService {

  /**
   * Run depreciation rate calculation for all products matching the filters in the unit cost
   * calculation.
   */
  void runDepRateCalc(UnitCostCalculation unitCostCalculation) throws AxelorException;

  /** Update product depreciation rates based on calculated values. */
  void updateDepRates(UnitCostCalculation unitCostCalculation) throws AxelorException;

  /**
   * Recompute derived values (computedCost, valuedGap) on every line of the given calculation.
   * Useful after a CSV import that only updates costToApply.
   */
  void recomputeLineBalances(UnitCostCalculation unitCostCalculation);

  /**
   * Recompute the derived values (computedCost, valuedGap) of a single line from its current rate.
   * Used by the form onChange so the displayed values match the Java rounding logic exactly. Does
   * not persist the line.
   */
  UnitCostCalcLine computeLineBalances(UnitCostCalcLine unitCostCalcLine);
}
