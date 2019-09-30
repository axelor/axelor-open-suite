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

import com.axelor.apps.production.db.RawMaterialRequirement;
import com.axelor.exception.AxelorException;

public interface RawMaterialRequirementService {

  /**
   * Print the raw material requirement report.
   *
   * @param rawMaterialRequirement the user defined parameter of the report.
   * @return URL to the printed report.
   */
  String print(RawMaterialRequirement rawMaterialRequirement) throws AxelorException;

  /**
   * Fetch next value for the sequence linked to the given raw material requirement.
   *
   * @param rawMaterialRequirement the report needing a sequence.
   * @return a string containing the value from the sequence.
   * @throws AxelorException if there is no sequence found.
   */
  String getSequence(RawMaterialRequirement rawMaterialRequirement) throws AxelorException;
}
