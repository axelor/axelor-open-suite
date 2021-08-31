package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.exception.AxelorException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

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
   * Generate a fixedAssetLine with values are computed with prorata (based on disposalDate, and
   * dates of fixedAsset)
   *
   * @param fixedAsset
   * @param disposalDate
   * @param previousRealizedLine
   * @return generated {@link FixedAssetLine}
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
   * This method group and sort {@link FixedAsset#getFixedAssetLineList()} and {@link
   * FixedAsset#getFiscalFixedAssetLineList()} by {@link FixedAssetLine#getDepreciationDate()}.
   * Because it sorted, the method will explicitly return a {@link LinkedHashMap}.
   *
   * @param fixedAsset
   * @return generated {@link LinkedHashMap}
   */
  LinkedHashMap<LocalDate, List<FixedAssetLine>> groupAndSortByDateFixedAssetLine(
      FixedAsset fixedAsset);
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
   * This method will compute on cession closest line from realized lines.
   *
   * @param fixedAsset
   * @param disposalDate
   * @return computed {@link FixedAssetLine}
   * @throws AxelorException
   */
  FixedAssetLine computeCessionLine(FixedAsset fixedAsset, LocalDate disposalDate)
      throws AxelorException;

  /**
   * This method will return any line that have the same year as disposalDate.
   *
   * @param fixedAsset
   * @param disposalDate
   * @param lineStatus
   * @return {@link FixedAssetLine} or null if there is no line with same year.
   */
  FixedAssetLine getExistingLineWithSameYear(
      FixedAsset fixedAsset, LocalDate disposalDate, int lineStatus);
}
