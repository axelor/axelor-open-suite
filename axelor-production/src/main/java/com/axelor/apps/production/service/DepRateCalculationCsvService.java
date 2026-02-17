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
package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.UnitCostCalculation;
import com.axelor.meta.db.MetaFile;
import java.io.IOException;

public interface DepRateCalculationCsvService {

  /**
   * Export depreciation rate calculation results to CSV.
   *
   * @param unitCostCalculation the calculation to export
   * @param fileName the file name (without extension)
   * @return the generated MetaFile
   * @throws IOException if file operations fail
   */
  MetaFile exportDepRateCalc(UnitCostCalculation unitCostCalculation, String fileName)
      throws IOException;

  /**
   * Import depreciation rate adjustments from CSV.
   *
   * @param dataFile the CSV file to import
   * @param unitCostCalculation the target calculation
   * @throws IOException if file operations fail
   */
  void importDepRateCalc(MetaFile dataFile, UnitCostCalculation unitCostCalculation)
      throws IOException, AxelorException;
}
