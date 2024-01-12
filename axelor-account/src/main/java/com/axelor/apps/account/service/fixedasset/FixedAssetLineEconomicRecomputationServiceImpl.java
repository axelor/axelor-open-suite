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

import static com.axelor.apps.account.service.fixedasset.FixedAssetServiceImpl.RETURNED_SCALE;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * It mostly works the same as {@link FixedAssetLineEconomicComputationServiceImpl} but it takes
 * into account the realized lines of the fixed asset. If there a not realized fixed asset lines
 * then please do not use this implementation.
 */
public class FixedAssetLineEconomicRecomputationServiceImpl
    extends FixedAssetLineEconomicComputationServiceImpl {

  private BigDecimal linearDepreciationBase;

  @Inject
  public FixedAssetLineEconomicRecomputationServiceImpl(
      FixedAssetDateService fixedAssetDateService,
      FixedAssetFailOverControlService fixedAssetFailOverControlService,
      AppBaseService appBaseService) {
    super(fixedAssetDateService, fixedAssetFailOverControlService, appBaseService);
  }

  @Override
  public Optional<FixedAssetLine> computeInitialPlannedFixedAssetLine(FixedAsset fixedAsset)
      throws AxelorException {
    throw new AxelorException(
        TraceBackRepository.CATEGORY_INCONSISTENCY,
        "this method is not supposed to be call with this implementation");
  }

  @Override
  protected BigDecimal getNumberOfDepreciation(FixedAsset fixedAsset) {
    BigDecimal initialProrata = computeProrataTemporis(fixedAsset);
    int nbRealizedLines =
        Optional.ofNullable(fixedAsset.getFixedAssetLineList())
            .map(
                list ->
                    list.stream()
                        .filter(
                            fixedAssetLine ->
                                fixedAssetLine.getStatusSelect()
                                    == FixedAssetLineRepository.STATUS_REALIZED)
                        .count())
            .orElse(0l)
            .intValue();
    if (isProrataTemporis(fixedAsset)
        && getComputationMethodSelect(fixedAsset)
            .equals(FixedAssetRepository.COMPUTATION_METHOD_LINEAR)) {
      if (fixedAsset.getCorrectedAccountingValue().signum() > 0) {
        // Nb depreciation = new nb depreciation - ( nb lines realized + prorata )
        return BigDecimal.valueOf(fixedAsset.getNumberOfDepreciation() - nbRealizedLines)
            .subtract(initialProrata);
      } else {
        // Nb depreciation = new nb depreciation - ( nb lines realized + prorata - 1 )
        return BigDecimal.valueOf(fixedAsset.getNumberOfDepreciation() - nbRealizedLines + 1)
            .subtract(initialProrata);
      }
    }
    return BigDecimal.valueOf(fixedAsset.getNumberOfDepreciation() - nbRealizedLines);
  }

  @Override
  protected BigDecimal numberOfDepreciationDone(FixedAsset fixedAsset) {
    List<FixedAssetLine> fixedAssetLineList = getFixedAssetLineList(fixedAsset);
    // We substract nbRealizedLines because we already take it into account in
    // getNumberOfDepreciation
    int nbRealizedLines =
        Optional.ofNullable(fixedAsset.getFixedAssetLineList())
            .map(
                list ->
                    list.stream()
                        .filter(
                            fixedAssetLine ->
                                fixedAssetLine.getStatusSelect()
                                    == FixedAssetLineRepository.STATUS_REALIZED)
                        .count())
            .orElse(0l)
            .intValue();
    if (fixedAssetFailOverControlService.isFailOver(fixedAsset)) {
      if (fixedAssetLineList == null) {
        return getNumberOfPastDepreciation(fixedAsset);
      }
      return BigDecimal.valueOf(fixedAssetLineList.size())
          .add(getNumberOfPastDepreciation(fixedAsset))
          .subtract(BigDecimal.valueOf(nbRealizedLines));
    }
    if (fixedAssetLineList == null) {
      return BigDecimal.ZERO;
    }
    return BigDecimal.valueOf(fixedAssetLineList.size())
        .subtract(BigDecimal.valueOf(nbRealizedLines));
  }

  @Override
  protected BigDecimal computeDegressiveDepreciation(BigDecimal baseValue, FixedAsset fixedAsset) {
    // We substract nbRealizedLines because we took them into account in getNumberOfDepreciation
    // But in this method we must count every lines.
    BigDecimal ddRate = getDegressiveCoef(fixedAsset);
    int nbRealizedLines =
        Optional.ofNullable(fixedAsset.getFixedAssetLineList())
            .map(
                list ->
                    list.stream()
                        .filter(
                            fixedAssetLine ->
                                fixedAssetLine.getStatusSelect()
                                    == FixedAssetLineRepository.STATUS_REALIZED)
                        .count())
            .orElse(0l)
            .intValue();
    return computeDepreciationNumerator(
            baseValue, getNumberOfDepreciation(fixedAsset).add(BigDecimal.valueOf(nbRealizedLines)))
        .multiply(ddRate)
        .setScale(RETURNED_SCALE, RoundingMode.HALF_UP);
  }

  @Override
  protected BigDecimal computeDepreciationBase(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine) {
    if (getComputationMethodSelect(fixedAsset)
        .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
      return getAccountingValue(previousFixedAssetLine);
    }

    return linearDepreciationBase;
  }

  @Override
  protected BigDecimal computeDepreciation(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine, BigDecimal baseValue) {
    if (linearDepreciationBase == null) {
      linearDepreciationBase = getAccountingValue(previousFixedAssetLine);
    }
    return super.computeDepreciation(fixedAsset, previousFixedAssetLine, linearDepreciationBase);
  }

  @Override
  protected Integer getPeriodicityInMonthProrataTemporis(FixedAsset fixedAsset) {
    return fixedAsset.getInitialPeriodicityInMonth();
  }
}
