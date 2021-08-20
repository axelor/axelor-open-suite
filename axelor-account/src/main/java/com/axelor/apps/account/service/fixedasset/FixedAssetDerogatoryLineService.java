package com.axelor.apps.account.service.fixedasset;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetDerogatoryLine;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.exception.AxelorException;

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

  List<FixedAssetDerogatoryLine> computeFixedAssetDerogatoryLineList(FixedAsset fixedAsset);

  void multiplyLinesBy(List<FixedAssetDerogatoryLine> fixedAssetDerogatoryLine, BigDecimal prorata);

  void generateDerogatoryCessionMove(
      FixedAssetDerogatoryLine firstPlannedDerogatoryLine,
      FixedAssetDerogatoryLine lastRealizedDerogatoryLine)
      throws AxelorException;

void copyFixedAssetDerogatoryLineList(FixedAsset fixedAsset, FixedAsset newFixedAsset);
}
