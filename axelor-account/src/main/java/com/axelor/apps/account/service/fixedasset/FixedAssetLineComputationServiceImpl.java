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

import static com.axelor.apps.account.service.fixedasset.FixedAssetServiceImpl.CALCULATION_SCALE;
import static com.axelor.apps.account.service.fixedasset.FixedAssetServiceImpl.RETURNED_SCALE;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.tool.date.DateTool;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.Calendar;

public class FixedAssetLineComputationServiceImpl implements FixedAssetLineComputationService {

  @Override
  public FixedAssetLine computeInitialPlannedFixedAssetLine(FixedAsset fixedAsset) {
    LocalDate firstDepreciationDate = fixedAsset.getFirstDepreciationDate();
    BigDecimal depreciation = computeInitialDepreciation(fixedAsset);
    return createPlannedFixedAssetLine(
        firstDepreciationDate,
        depreciation,
        depreciation,
        fixedAsset.getGrossValue().subtract(depreciation));
  }

  protected BigDecimal computeInitialDepreciation(FixedAsset fixedAsset) {
    if (fixedAsset
        .getComputationMethodSelect()
        .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
      return computeInitialDegressiveDepreciation(fixedAsset);
    } else {
      return computeInitialLinearDepreciation(fixedAsset);
    }
  }

  protected BigDecimal computeInitialLinearDepreciation(FixedAsset fixedAsset) {
    return computeInitialDepreciationNumerator(fixedAsset.getGrossValue(), fixedAsset)
        .setScale(RETURNED_SCALE, RoundingMode.HALF_UP);
  }

  protected BigDecimal computeInitialDegressiveDepreciation(FixedAsset fixedAsset) {
    BigDecimal ddRate = fixedAsset.getDegressiveCoef();
    return computeInitialDepreciationNumerator(fixedAsset.getGrossValue(), fixedAsset)
        .multiply(ddRate)
        .setScale(RETURNED_SCALE, RoundingMode.HALF_UP);
  }

  protected BigDecimal computeDepreciation(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine) {
    BigDecimal depreciation;
    if (fixedAsset
        .getComputationMethodSelect()
        .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
      depreciation = computeOnGoingDegressiveDepreciation(fixedAsset, previousFixedAssetLine);
    } else {
      depreciation = computeLinearDepreciation(fixedAsset);
    }
    if (depreciation
            .add(previousFixedAssetLine.getCumulativeDepreciation())
            .compareTo(fixedAsset.getGrossValue())
        > 0) {
      depreciation =
          fixedAsset.getGrossValue().subtract(previousFixedAssetLine.getCumulativeDepreciation());
    }
    return depreciation;
  }

  protected BigDecimal computeLinearDepreciation(FixedAsset fixedAsset) {
    return computeDepreciationNumerator(
            fixedAsset.getGrossValue(), fixedAsset.getNumberOfDepreciation())
        .setScale(RETURNED_SCALE, RoundingMode.HALF_UP);
  }

  protected BigDecimal computeOnGoingDegressiveDepreciation(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine) {
    BigDecimal degressiveDepreciation =
        computeDegressiveDepreciation(previousFixedAssetLine.getResidualValue(), fixedAsset);
    BigDecimal linearDepreciation =
        previousFixedAssetLine
            .getResidualValue()
            .divide(
                BigDecimal.valueOf(
                    fixedAsset.getNumberOfDepreciation()
                        - fixedAsset.getFixedAssetLineList().size()),
                RETURNED_SCALE,
                RoundingMode.HALF_UP);
    return degressiveDepreciation.max(linearDepreciation);
  }

  protected BigDecimal computeDegressiveDepreciation(BigDecimal baseValue, FixedAsset fixedAsset) {
    BigDecimal ddRate = fixedAsset.getDegressiveCoef();
    return computeDepreciationNumerator(baseValue, fixedAsset.getNumberOfDepreciation())
        .multiply(ddRate)
        .setScale(RETURNED_SCALE, RoundingMode.HALF_UP);
  }

  protected BigDecimal computeInitialDepreciationNumerator(
      BigDecimal baseValue, FixedAsset fixedAsset) {
    BigDecimal prorataTemporis = this.computeProrataTemporis(fixedAsset);
    return computeDepreciationNumerator(baseValue, fixedAsset.getNumberOfDepreciation())
        .multiply(prorataTemporis);
  }

  protected BigDecimal computeDepreciationNumerator(
      BigDecimal baseValue, int numberOfDepreciation) {
    BigDecimal depreciationRate = computeDepreciationRate(numberOfDepreciation);
    return baseValue.multiply(depreciationRate);
  }

  protected BigDecimal computeDepreciationRate(int numberOfDepreciation) {
    return numberOfDepreciation == 0
        ? BigDecimal.ZERO
        : BigDecimal.ONE.divide(
            BigDecimal.valueOf(numberOfDepreciation), CALCULATION_SCALE, RoundingMode.HALF_UP);
  }

  protected BigDecimal computeProrataTemporis(FixedAsset fixedAsset) {
    BigDecimal prorataTemporis = BigDecimal.ONE;
    if (fixedAsset.getFixedAssetCategory().getIsProrataTemporis()
        && !fixedAsset.getAcquisitionDate().equals(fixedAsset.getFirstDepreciationDate())) {

      LocalDate acquisitionDate = fixedAsset.getAcquisitionDate().minusDays(1);
      LocalDate depreciationDate = fixedAsset.getFirstDepreciationDate();

      int acquisitionYear = acquisitionDate.getYear();
      Month acquisitionMonth = acquisitionDate.getMonth();
      int acquisitionDay = acquisitionDate.getDayOfMonth();
      int depreciationYear = depreciationDate.getYear();
      Month depreciationMonth = depreciationDate.getMonth();
      int depreciationDay = depreciationDate.getDayOfMonth();

      // US way
      if (fixedAsset.getFixedAssetCategory().getIsUSProrataTemporis()) {

        if (acquisitionMonth == Month.FEBRUARY
            && depreciationMonth == Month.FEBRUARY
            && isLastDayOfFebruary(acquisitionYear, acquisitionDay)
            && isLastDayOfFebruary(depreciationYear, depreciationDay)) {
          depreciationDay = 30;
        }

        if (acquisitionMonth == Month.FEBRUARY
            && isLastDayOfFebruary(acquisitionYear, acquisitionDay)) {
          acquisitionDay = 30;
        }

        if (acquisitionDay >= 30 && depreciationDay > 30) {
          depreciationDay = 30;
        }

        if (acquisitionDay > 30) {
          acquisitionDay = 30;
        }

      } else { // European way

        if (acquisitionDay == 31) {
          acquisitionDay = 30;
        }

        if (depreciationDay == 31) {
          depreciationDay = 30;
        }
      }

      BigDecimal nbDaysBetweenAcqAndFirstDepDate =
          BigDecimal.valueOf(
                  360 * (depreciationYear - acquisitionYear)
                      + 30 * (depreciationMonth.getValue() - acquisitionMonth.getValue())
                      + (depreciationDay - acquisitionDay))
              .setScale(CALCULATION_SCALE, RoundingMode.HALF_UP);
      BigDecimal nbDaysOfPeriod =
          BigDecimal.valueOf(fixedAsset.getPeriodicityInMonth() * 30)
              .setScale(CALCULATION_SCALE, RoundingMode.HALF_UP);
      prorataTemporis =
          nbDaysBetweenAcqAndFirstDepDate.divide(
              nbDaysOfPeriod, CALCULATION_SCALE, RoundingMode.HALF_UP);
    }
    return prorataTemporis;
  }

  protected boolean isLastDayOfFebruary(int year, int day) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(year, Calendar.FEBRUARY, 1);
    int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    return maxDays == day;
  }

  @Override
  public FixedAssetLine computePlannedFixedAssetLine(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine) {

    BigDecimal depreciation = computeDepreciation(fixedAsset, previousFixedAssetLine);
    BigDecimal cumulativeDepreciation =
        previousFixedAssetLine.getCumulativeDepreciation().add(depreciation);
    BigDecimal residualValue = fixedAsset.getGrossValue().subtract(cumulativeDepreciation);

    LocalDate depreciationDate;
    if (!fixedAsset.getFixedAssetCategory().getIsProrataTemporis()
        || fixedAsset.getAcquisitionDate().equals(fixedAsset.getFirstDepreciationDate())
        || residualValue.signum() != 0) {
      depreciationDate = computeDepreciationDate(fixedAsset, previousFixedAssetLine);
    } else {
      depreciationDate = computeLastProrataDepreciationDate(fixedAsset);
    }

    return createPlannedFixedAssetLine(
        depreciationDate, depreciation, cumulativeDepreciation, residualValue);
  }

  protected FixedAssetLine createPlannedFixedAssetLine(
      LocalDate depreciationDate,
      BigDecimal depreciation,
      BigDecimal cumulativeDepreciation,
      BigDecimal residualValue) {
    FixedAssetLine fixedAssetLine = new FixedAssetLine();
    fixedAssetLine.setStatusSelect(FixedAssetLineRepository.STATUS_PLANNED);
    fixedAssetLine.setDepreciationDate(depreciationDate);
    fixedAssetLine.setDepreciation(depreciation);
    fixedAssetLine.setCumulativeDepreciation(cumulativeDepreciation);
    fixedAssetLine.setResidualValue(residualValue);
    return fixedAssetLine;
  }

  protected LocalDate computeDepreciationDate(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine) {
    LocalDate depreciationDate;
    depreciationDate =
        DateTool.plusMonths(
            previousFixedAssetLine.getDepreciationDate(), fixedAsset.getPeriodicityInMonth());

    return depreciationDate;
  }

  protected LocalDate computeLastProrataDepreciationDate(FixedAsset fixedAsset) {
    LocalDate d =
        DateTool.plusMonths(fixedAsset.getAcquisitionDate(), fixedAsset.getDurationInMonth());
    if (FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE.equals(
        fixedAsset.getComputationMethodSelect())) {
      d = DateTool.minusMonths(d, fixedAsset.getPeriodicityInMonth());
    }
    return d.minusDays(1);
  }
}
