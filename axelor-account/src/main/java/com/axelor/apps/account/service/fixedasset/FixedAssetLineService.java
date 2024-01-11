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
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.base.AxelorException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FixedAssetLineService {

  /**
   * Generate a fixedAssetLine with values are computed with prorata (based on disposalDate, and
   * dates of fixedAsset)
   *
   * @param fixedAsset
   * @param disposalDate
   * @return generated {@link FixedAssetLine}
   */
  FixedAssetLine generateProrataDepreciationLine(FixedAsset fixedAsset, LocalDate disposalDate)
      throws AxelorException;

  /**
   * Copy fixedAssetLineList and fiscalFixedAssetLineList from fixedAsset to newFixedAsset.
   *
   * @param fixedAsset
   * @param newFixedAsset
   */
  void copyFixedAssetLineList(FixedAsset fixedAsset, FixedAsset newFixedAsset);

  /**
   * Return line with smallest depreciation date with statusSelect = status. The method will skip
   * nbLineToSkip, meaning that it will ignore nbLineToSkipResult.
   *
   * @param fixedAssetLineList
   * @param status
   * @param nbLineToSkip
   * @return {@link Optional} of {@link FixedAssetLine}
   */
  Optional<FixedAssetLine> findOldestFixedAssetLine(
      List<FixedAssetLine> fixedAssetLineList, int status, int nbLineToSkip);

  /**
   * Return line with greatest depreciation date with statusSelect = status. The method will skip
   * nbLineToSkip, meaning that it will ignore nbLineToSkipResult.
   *
   * @param fixedAssetLineList
   * @param status
   * @param nbLineToSkip
   * @return {@link Optional} of {@link FixedAssetLine}
   */
  Optional<FixedAssetLine> findNewestFixedAssetLine(
      List<FixedAssetLine> fixedAssetLineList, int status, int nbLineToSkip);

  /**
   * This method will remove every fixedAssetLine from database, then use {@link List#clear()}
   *
   * @param fixedAssetLineList
   */
  void clear(List<FixedAssetLine> fixedAssetLineList);

  /**
   * Call {@link FixedAssetLineRepository#remove(FixedAssetLine)} on line
   *
   * @param line
   */
  void remove(FixedAssetLine line);

  /**
   * Filter list with statusSelect = status. Filtered lines will be remove from database by calling
   * {@link FixedAssetLineRepository#remove(FixedAssetLine)}
   *
   * @param fixedAssetLineList
   * @param status
   */
  void filterListByStatus(List<FixedAssetLine> fixedAssetLineList, int status);

  /**
   * Get Fixed asset of fixedAssetLine.
   *
   * @param fixedAssetLine
   * @return fixedAsset : {@link FixedAsset}
   */
  FixedAsset getFixedAsset(FixedAssetLine fixedAssetLine) throws AxelorException;

  /**
   * Set Fixed asset of fixedAssetLine.
   *
   * @param fixedAsset
   * @param fixedAssetLine
   */
  void setFixedAsset(FixedAsset fixedAsset, FixedAssetLine fixedAssetLine) throws AxelorException;
}
