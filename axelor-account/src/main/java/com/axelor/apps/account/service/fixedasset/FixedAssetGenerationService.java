/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetCategory;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
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
   * Allow to create fixed asset from invoice
   *
   * @param invoice
   * @return
   * @throws AxelorException
   */
  List<FixedAsset> createFixedAssets(Invoice invoice) throws AxelorException;

  /**
   * Generates and compute ifrs lines for fixedAsset
   *
   * @throws AxelorException
   * @throws NullPointerException if fixedAsset is null
   */
  void generateAndComputeIfrsFixedAssetLines(FixedAsset fixedAsset) throws AxelorException;

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
   * @param disposalQty
   * @return
   * @throws AxelorException
   */
  FixedAsset copyFixedAsset(FixedAsset fixedAsset, BigDecimal disposalQty) throws AxelorException;

  /**
   * Copy fixedAssetCategory infos such as computationMethodSelect, numberOfDepreciation, etc.. in
   * fixedAsset
   *
   * @param fixedAssetCategory
   * @param fixedAsset
   */
  void copyInfos(FixedAssetCategory fixedAssetCategory, FixedAsset fixedAsset);
}
