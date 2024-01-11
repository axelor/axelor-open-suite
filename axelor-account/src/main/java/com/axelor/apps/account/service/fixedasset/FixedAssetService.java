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

import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AssetDisposalReason;
import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.AxelorException;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface FixedAssetService {
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

  void updateDepreciation(FixedAsset fixedAsset) throws AxelorException;

  /**
   * Split the fixed asset in two fixed asset. The split will create a fixed asset and modify
   * fixedAsset in order to have two complementary fixed assets. The new fixed asset is a copy of
   * fixedAsset except for the lines (fiscal lines, derogatory lines and economic lines). Every
   * lines that have not been realized will be removed, and others will be re-computed pro-rata to
   * the quantity. (DisposalQty / fixedAsset.qty)
   *
   * @param fixedAsset
   * @param splitType
   * @param amount
   * @return The new fixed asset created from split.
   * @throws AxelorException
   */
  FixedAsset splitFixedAsset(
      FixedAsset fixedAsset,
      int splitType,
      BigDecimal amount,
      LocalDate disposalDate,
      String comments)
      throws AxelorException;

  /**
   * Call splitFixedAsset and save both fixed asset. (Original and created)
   *
   * @param fixedAsset
   * @param splitType
   * @param amount
   * @param splittingDate
   * @param comments
   * @return
   * @throws AxelorException
   */
  FixedAsset splitAndSaveFixedAsset(
      FixedAsset fixedAsset,
      int splitType,
      BigDecimal amount,
      LocalDate splittingDate,
      String comments)
      throws AxelorException;

  void checkFixedAssetBeforeSplit(FixedAsset fixedAsset, int splitType, BigDecimal amount)
      throws AxelorException;

  void checkFixedAssetBeforeDisposal(
      FixedAsset fixedAsset,
      LocalDate disposalDate,
      int disposalQtySelect,
      BigDecimal disposalQty,
      Boolean generateSaleMove,
      TaxLine saleTaxLine)
      throws AxelorException;

  int computeTransferredReason(
      Integer disposalTypeSelect,
      Integer disposalQtySelect,
      BigDecimal disposalQty,
      FixedAsset fixedAsset);

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
      AssetDisposalReason assetDisposalReason,
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

  void onChangeDepreciationPlan(FixedAsset fixedAsset) throws AxelorException;

  void checkFixedAssetScissionQty(BigDecimal disposalQty, FixedAsset fixedAsset)
      throws AxelorException;

  public boolean checkDepreciationPlans(FixedAsset fixedAsset);

  public FixedAsset fullDisposal(
      FixedAsset fixedAsset,
      LocalDate disposalDate,
      int disposalQtySelect,
      BigDecimal disposalQty,
      Boolean generateSaleMove,
      TaxLine saleTaxLine,
      Integer disposalTypeSelect,
      BigDecimal disposalAmount,
      AssetDisposalReason assetDisposalReason,
      String comments)
      throws AxelorException;
}
