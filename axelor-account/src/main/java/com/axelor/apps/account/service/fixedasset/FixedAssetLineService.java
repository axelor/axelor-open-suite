package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.MoveLine;
import java.time.LocalDate;

public interface FixedAssetLineService {

  /**
   * This method will generate a fixed asset based on the moveLine. fixetAsset.name =
   * moveLine.description fixetAsset.company = moveLine.move.company fixetAsset.fixedAssetCategory =
   * moveLine.fixedAssetCategory fixetAsset.partner = moveLine.partner fixedAsset.purchaseAccount =
   * moveLine.account fixedAsset.journal = moveLine.journal fixedAsset.analyticDistributionTemplate
   * = moveLine.fixedAsset.analyticDistributionTemplate fixedAsset.acquisitionDate = SI
   * moveLine.originDate != NULL = moveLine.originDate ELSE moveLine.date
   *
   * @param moveLine
   * @return generated FixedAsset
   */
  FixedAsset generateFixedAsset(MoveLine moveLine);

  /**
   * This method will call generateFixedAsset(MoveLine) and call save action.
   *
   * @param moveLine
   * @return Generated fixedAsset
   */
  FixedAsset generateAndSaveFixedAsset(MoveLine moveLine);

  /**
   * Generate a fixedAssetLine with values are computed with prorata (based on disposalDate, and
   * dates of fixedAsset)
   *
   * @param fixedAsset
   * @param disposalDate
   * @param previousRealizedLine
   * @return generated fixedAssetLine
   */
  FixedAssetLine generateProrataDepreciationLine(
      FixedAsset fixedAsset, LocalDate disposalDate, FixedAssetLine previousRealizedLine);

  /**
   * Compute depreciation on fixedAssetLine.
   *
   * @param fixedAsset
   * @param fixedAssetLine
   * @param previousRealizedLine
   * @param disposalDate
   */
  void computeDepreciationWithProrata(
      FixedAsset fixedAsset,
      FixedAssetLine fixedAssetLine,
      FixedAssetLine previousRealizedLine,
      LocalDate disposalDate);

  /**
   * Copy fixedAssetLineList and fiscalFixedAssetLineList from fixedAsset to newFixedAsset.
   *
   * @param fixedAsset
   * @param newFixedAsset
   */
  void copyFixedAssetLineList(FixedAsset fixedAsset, FixedAsset newFixedAsset);
}
