/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetCategory;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface FixedAssetService {

  /**
   * Allow to generate and compute the fixed asset lines
   *
   * @param fixedAsset
   * @return
   * @throws AxelorException
   */
  FixedAsset generateAndComputeLines(FixedAsset fixedAsset) throws AxelorException;

  /**
   * Allow to create fixed asset from invoice
   *
   * @param invoice
   * @return
   * @throws AxelorException
   */
  List<FixedAsset> createFixedAssets(Invoice invoice) throws AxelorException;

  /**
   * Allow to disposal remaining depreciation
   *
   * @param disposalDate
   * @param disposalAmount
   * @param fixedAsset
   * @throws AxelorException
   */
  void disposal(
      LocalDate disposalDate,
      BigDecimal disposalAmount,
      FixedAsset fixedAsset,
      int transferredReason)
      throws AxelorException;

  void createAnalyticOnMoveLine(
      AnalyticDistributionTemplate analyticDistributionTemplate, MoveLine moveLine)
      throws AxelorException;

  void updateAnalytic(FixedAsset fixedAsset) throws AxelorException;

  /**
   * Validate fixedAsset
   *
   * @param fixedAsset
   * @throws AxelorException
   */
  void validate(FixedAsset fixedAsset) throws AxelorException;

  int massValidation(List<Long> fixedAssetIds) throws AxelorException;

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
   * Compute first depreciation date of fixedAsset
   *
   * @param fixedAsset
   */
  void computeFirstDepreciationDate(FixedAsset fixedAsset);

  void updateDepreciation(FixedAsset fixedAsset) throws AxelorException;

  /**
   * Split the fixed asset in two fixed asset. The split will create a fixed asset and modify
   * fixedAsset in order to have two complementary fixed assets. The new fixed asset is a copy of
   * fixedAsset except for the lines (fiscal lines, derogatory lines and economic lines). Every
   * lines that have not been realized will be removed, and others will be re-computed pro-rata to
   * the quantity. (DisposalQty / fixedAsset.qty)
   *
   * @param fixedAsset
   * @param disposalQty
   * @return The new fixed asset created from split.
   * @throws AxelorException
   */
  FixedAsset splitFixedAsset(
      FixedAsset fixedAsset, BigDecimal disposalQty, LocalDate disposalDate, String comments)
      throws AxelorException;
  /**
   * Call splitFixedAsset and save both fixed asset. (Original and created)
   *
   * @param fixedAsset
   * @param disposalQty
   * @param splittingDate
   * @param comments
   * @return
   * @throws AxelorException
   */
  FixedAsset splitAndSaveFixedAsset(
      FixedAsset fixedAsset, BigDecimal disposalQty, LocalDate splittingDate, String comments)
      throws AxelorException;

  int computeTransferredReason(Integer disposalTypeSelect, Integer disposalQtySelect);

  /**
   * Filter lines from fixedAssetLineList / fiscalAssetLineList / fixedAssetDerogatoryLineList with
   * line.status = status. Line that doesn't match the status will be removed from database.
   *
   * @param fixedAsset
   * @param statusPlanned
   * @return filteredFixedAsset
   */
  FixedAsset filterListsByStatus(FixedAsset fixedAsset, int status);

  /**
   * Method that manage disposal action. The process will be different depending on the
   * transferredReason.
   */
  FixedAsset computeDisposal(
      FixedAsset fixedAsset,
      LocalDate disposalDate,
      BigDecimal disposalQty,
      BigDecimal disposalAmount,
      int transferredReason,
      String comments)
      throws AxelorException;

  FixedAsset cession(
      FixedAsset fixedAsset,
      LocalDate disposalDate,
      BigDecimal disposalAmount,
      int transferredReason,
      String comments)
      throws AxelorException;

  /**
   * Multiply fiscal and economic lines of fixedAsset by prorata. Then compute derogatory lines.
   *
   * @param newFixedAsset
   * @param prorata
   * @throws AxelorException
   */
  void multiplyLinesBy(FixedAsset newFixedAsset, BigDecimal prorata) throws AxelorException;

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
   * Copy fixedAssetCategory infos such as computationMethodSelect, numberOfDepreciation, etc.. in
   * fixedAsset
   *
   * @param fixedAssetCategory
   * @param fixedAsset
   */
  void copyInfos(FixedAssetCategory fixedAssetCategory, FixedAsset fixedAsset);
}
