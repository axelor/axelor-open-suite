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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** This service is used to compute new lines from an existing fixed asset header. */
public interface FixedAssetLineComputationService {

  /**
   * Compute the first fixed asset line from an empty fixed asset.
   *
   * @param fixedAsset a fixed asset with no lines
   * @param typeSelect typeSelect of the fixedAssetLine
   * @return the created fixed asset line
   * @throws AxelorException
   */
  Optional<FixedAssetLine> computeInitialPlannedFixedAssetLine(FixedAsset fixedAsset)
      throws AxelorException;

  /**
   * Compute the next fixed asset line from a fixed asset and the previous line.
   *
   * @param fixedAsset a fixed asset with existing lines
   * @param previousFixedAssetLine the previous line
   * @param typeSelect typeSelect of the fixedAssetLine
   * @return the created fixed asset line
   * @throws AxelorException
   */
  FixedAssetLine computePlannedFixedAssetLine(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine) throws AxelorException;

  /**
   * Multiply line by prorata
   *
   * @param line
   * @param prorata
   */
  void multiplyLineBy(FixedAssetLine line, BigDecimal prorata);

  /**
   * Multiply economic and fiscal lines by prorata
   *
   * @param line
   * @param prorata
   */
  void multiplyLinesBy(List<FixedAssetLine> fixedAssetLineList, BigDecimal prorata);

  FixedAssetLine createFixedAssetLine(
      FixedAsset fixedAsset,
      LocalDate depreciationDate,
      BigDecimal depreciation,
      BigDecimal cumulativeDepreciation,
      BigDecimal accountingValue,
      BigDecimal depreciationBase,
      int typeSelect,
      int statusSelect);
}
