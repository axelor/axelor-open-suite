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
import com.axelor.apps.account.db.Invoice;
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
   */
  FixedAsset generateAndComputeLines(FixedAsset fixedAsset);

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
   * Generate and computes derogatoryLines for fixedAsset
   *
   * @param fixedAsset
   */
  void generateAndComputeFixedAssetDerogatoryLines(FixedAsset fixedAsset);

  /**
   * Generate and computes fiscalFixedAssetLines for fixedAsset
   *
   * @param fixedAsset
   */
  void generateAndComputeFiscalFixedAssetLines(FixedAsset fixedAsset);

  /**
   * Generate and computes fixedAssetLines for fixedAsset
   *
   * @param fixedAsset
   */
  void generateAndComputeFixedAssetLines(FixedAsset fixedAsset);

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

  int computeTransferredReason(Integer disposalTypeSelect, Integer disposalQtySelect);

  /**
   * Filter lines from fixedAssetLineList / fiscalAssetLineList / fixedAssetDerogatoryLineList with
   * line.status = status.
   *
   * @param fixedAsset
   * @param statusPlanned
   * @return filteredFixedAsset
   */
  FixedAsset filterListsByStatus(FixedAsset fixedAsset, int status);

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
}
