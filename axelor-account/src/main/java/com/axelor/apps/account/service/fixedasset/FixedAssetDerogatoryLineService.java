package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetDerogatoryLine;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetDerogatoryLineRepository;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface FixedAssetDerogatoryLineService {

  FixedAssetDerogatoryLine createFixedAssetDerogatoryLine(
      LocalDate depreciationDate,
      BigDecimal depreciationAmount,
      BigDecimal fiscalDepreciationAmount,
      BigDecimal derogatoryAmount,
      BigDecimal incomeDepreciationAmount,
      BigDecimal derogatoryBalanceAmount,
      FixedAssetLine fixedAssetLine,
      FixedAssetLine fiscalFixedAssetLine,
      int statusSelect);
  /**
   * This method will generate a fixedAssetDerogatoryLine list based on fixedAsset's fiscal and
   * economic lines that are planned. Keep in mind that it will not compute realized lines, and
   * therefore the derogatoryBalanceAmount of computed derogatory lines might be shifted from lines
   * that are realized. (Because it depends of the previous line) It might be necessary to
   * recalculate derogatoryBalanceAmount.
   */
  List<FixedAssetDerogatoryLine> computePlannedFixedAssetDerogatoryLineList(FixedAsset fixedAsset);

  void multiplyLinesBy(List<FixedAssetDerogatoryLine> fixedAssetDerogatoryLine, BigDecimal prorata);

  void generateDerogatoryCessionMove(
      FixedAssetDerogatoryLine firstPlannedDerogatoryLine,
      FixedAssetDerogatoryLine lastRealizedDerogatoryLine)
      throws AxelorException;

  void copyFixedAssetDerogatoryLineList(FixedAsset fixedAsset, FixedAsset newFixedAsset);

  void computeDerogatoryBalanceAmount(List<FixedAssetDerogatoryLine> fixedAssetDerogatoryLineList);

  /**
   * Method that generates a {@link FixedAssetDerogatoryLine} using econimicFixedAssetLine and
   * fiscalFixedAssetLine.
   *
   * @param economicFixedAssetLine
   * @param fiscalFixedAssetLine
   * @param previousFixedAssetDerogatoryLine
   * @param date
   * @return generated {@link FixedAssetDerogatoryLine}
   */
  FixedAssetDerogatoryLine computePlannedDerogatoryLine(
      FixedAssetLine economicFixedAssetLine,
      FixedAssetLine fiscalFixedAssetLine,
      FixedAssetDerogatoryLine previousFixedAssetDerogatoryLine,
      LocalDate date);

  /**
   * This method will remove every fixedAssetDerogatoryLine from database, then use {@link
   * List#clear()}
   *
   * @param fixedAssetDerogatoryLineList
   */
  void clear(List<FixedAssetDerogatoryLine> fixedAssetDerogatoryLineList);

  /**
   * Filter list with statusSelect = status. Filtered lines will be remove from database by calling
   * {@link FixedAssetDerogatoryLineRepository#remove(FixedAssetLine)}
   *
   * @param fixedAssetLineList
   * @param status
   */
  void filterListByStatus(List<FixedAssetDerogatoryLine> fixedAssetDerogatoryLineList, int status);

  public void remove(FixedAssetDerogatoryLine line);
}
