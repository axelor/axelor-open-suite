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
import com.axelor.apps.account.db.FixedAssetCategory;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.AxelorException;
import java.util.List;

public interface FixedAssetGenerationService {

  /**
   * Allow to generate and compute the fixed asset lines
   *
   * @param fixedAsset
   * @return
   * @throws AxelorException
   */
  FixedAsset generateAndComputeLines(FixedAsset fixedAsset) throws AxelorException;

  List<FixedAsset> createFixedAssets(Invoice invoice) throws AxelorException;

  /**
   * This method will generate a fixed asset based on the moveLine. fixetAsset.name =
   * moveLine.description fixetAsset.company = moveLine.move.company fixetAsset.fixedAssetCategory =
   * moveLine.fixedAssetCategory fixetAsset.partner = moveLine.partner fixedAsset.purchaseAccount =
   * moveLine.account fixedAsset.journal = moveLine.journal fixedAsset.analyticDistributionTemplate
   * = moveLine.fixedAsset.analyticDistributionTemplate fixedAsset.acquisitionDate = SI
   * moveLine.originDate != NULL = moveLine.originDate ELSE moveLine.date
   *
   * @param moveLine
   * @return generated {@link FixedAsset}
   * @throws AxelorException
   */
  FixedAsset generateFixedAsset(Move move, MoveLine moveLine) throws AxelorException;

  /**
   * This method will call generateFixedAsset(MoveLine) and call save action.
   *
   * @param moveLine
   * @return Generated {@link FixedAsset}
   * @throws AxelorException
   */
  FixedAsset generateAndSaveFixedAsset(Move move, MoveLine moveLine) throws AxelorException;

  /**
   * Generate sequence for fixedAsset.
   *
   * @param fixedAsset
   * @return
   * @throws AxelorException
   */
  String generateSequence(FixedAsset fixedAsset) throws AxelorException;

  /**
   * Copy FixedAsset including all lines.
   *
   * @param fixedAsset
   * @return
   * @throws AxelorException
   */
  FixedAsset copyFixedAsset(FixedAsset fixedAsset) throws AxelorException;

  /**
   * Copy fixedAssetCategory infos such as computationMethodSelect, numberOfDepreciation, etc.. in
   * fixedAsset
   *
   * @param fixedAssetCategory
   * @param fixedAsset
   */
  void copyInfos(FixedAssetCategory fixedAssetCategory, FixedAsset fixedAsset);
}
