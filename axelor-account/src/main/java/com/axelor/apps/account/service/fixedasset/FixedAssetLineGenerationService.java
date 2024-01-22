/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.base.AxelorException;

public interface FixedAssetLineGenerationService {

  /**
   * Generate and computes derogatoryLines for fixedAsset
   *
   * @param fixedAsset
   */
  void generateAndComputeFixedAssetDerogatoryLines(FixedAsset fixedAsset);

  /**
   * Generate and computes fiscalFixedAssetLines for fixedAsset
   *
   * @param fixedAsset
   * @throws AxelorException
   */
  void generateAndComputeFiscalFixedAssetLines(FixedAsset fixedAsset) throws AxelorException;

  /**
   * Generate and computes fixedAssetLines for fixedAsset
   *
   * @param fixedAsset
   * @throws AxelorException
   */
  void generateAndComputeFixedAssetLines(FixedAsset fixedAsset) throws AxelorException;

  /**
   * Generate and computes fixedAssetLines for fixedAsset but instead of generate the initial fixed
   * asset line, it starts from fixedAssetLine.
   *
   * @param fixedAsset
   * @throws AxelorException
   */
  void generateAndComputeFixedAssetLinesStartingWith(
      FixedAsset fixedAsset, FixedAssetLine fixedAssetLine) throws AxelorException;

  /**
   * Generates and compute ifrs lines for fixedAsset
   *
   * @throws AxelorException
   * @throws NullPointerException if fixedAsset is null
   */
  void generateAndComputeIfrsFixedAssetLines(FixedAsset fixedAsset) throws AxelorException;
}
