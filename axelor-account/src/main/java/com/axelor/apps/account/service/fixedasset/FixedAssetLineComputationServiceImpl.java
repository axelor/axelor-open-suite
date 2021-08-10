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

import static com.axelor.apps.account.service.fixedasset.FixedAssetServiceImpl.CALCULATION_SCALE;
import static com.axelor.apps.account.service.fixedasset.FixedAssetServiceImpl.RETURNED_SCALE;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.service.AnalyticFixedAssetService;
import com.axelor.apps.tool.date.DateTool;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.Calendar;

public class FixedAssetLineComputationServiceImpl implements FixedAssetLineComputationService {

  protected AnalyticFixedAssetService analyticFixedAssetService;

  @Inject
  public FixedAssetLineComputationServiceImpl(AnalyticFixedAssetService analyticFixedAssetService) {
    this.analyticFixedAssetService = analyticFixedAssetService;
  }

  @Override
  public FixedAssetLine computeInitialPlannedFixedAssetLine(FixedAsset fixedAsset, int typeSelect) {
    LocalDate firstDepreciationDate = fixedAsset.getFirstDepreciationDate();
    BigDecimal depreciation = computeInitialDepreciation(fixedAsset, typeSelect);
    BigDecimal accountingValue = fixedAsset.getGrossValue().subtract(depreciation);
    BigDecimal depreciationBase = computeDepreciationBase(fixedAsset, typeSelect, accountingValue);
    return createPlannedFixedAssetLine(
        fixedAsset,
        firstDepreciationDate,
        depreciation,
        depreciation,
        accountingValue,
        depreciationBase,
        typeSelect);
  }

  protected BigDecimal computeDepreciationBase(
      FixedAsset fixedAsset, int typeSelect, BigDecimal accountingValue) {
    // Default value is if typeSelect is fiscal.
    BigDecimal depreciationBase = fixedAsset.getGrossValue();
    if (typeSelect == FixedAssetLineRepository.TYPE_SELECT_ECONOMIC) {
      if (fixedAsset
          .getComputationMethodSelect()
          .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
        depreciationBase = fixedAsset.getGrossValue().subtract(accountingValue);
      } else {
        depreciationBase = fixedAsset.getGrossValue().subtract(fixedAsset.getResidualValue());
      }
    }
    return depreciationBase;
  }

  protected BigDecimal computeInitialDepreciation(FixedAsset fixedAsset, int typeSelect) {
    // Case of fiscal
    if (typeSelect == FixedAssetLineRepository.TYPE_SELECT_FISCAL) {
      // We always look at fiscal computation method for type select fiscal.
      if (fixedAsset.getFiscalComputationMethodSelect() != null
          && fixedAsset
              .getFiscalComputationMethodSelect()
              .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
        return computeInitialDegressiveDepreciation(fixedAsset, true);
      } else {
        return computeInitialLinearDepreciation(fixedAsset, true);
      }

    }
    // Case of Type Economic
    else {
      if (fixedAsset.getComputationMethodSelect() != null
          && fixedAsset
              .getComputationMethodSelect()
              .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
        // In case of economic type, boolean argument is always false, since there is copy before.
        return computeInitialDegressiveDepreciation(fixedAsset, false);
      } else {
        return computeInitialLinearDepreciation(fixedAsset, false);
      }
    }
  }

  protected BigDecimal computeInitialLinearDepreciation(
      FixedAsset fixedAsset, boolean isFiscalComputeMethod) {
    return computeInitialDepreciationNumerator(
            fixedAsset.getGrossValue(), fixedAsset, isFiscalComputeMethod)
        .setScale(RETURNED_SCALE, RoundingMode.HALF_UP);
  }

  protected BigDecimal computeInitialDegressiveDepreciation(
      FixedAsset fixedAsset, boolean isFiscalComputationMethod) {
    BigDecimal ddRate =
        isFiscalComputationMethod
            ? fixedAsset.getFiscalDegressiveCoef()
            : fixedAsset.getDegressiveCoef();
    return computeInitialDepreciationNumerator(
            fixedAsset.getGrossValue(), fixedAsset, isFiscalComputationMethod)
        .multiply(ddRate)
        .setScale(RETURNED_SCALE, RoundingMode.HALF_UP);
  }

  protected BigDecimal computeDepreciation(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine, int typeSelect) {
    BigDecimal depreciation;
    // First part
    // Case of fiscal
    if (typeSelect == FixedAssetLineRepository.TYPE_SELECT_FISCAL) {
      // We always look at fiscalComputationMethodSelect
      if (fixedAsset.getFiscalComputationMethodSelect() != null
          && fixedAsset
              .getFiscalComputationMethodSelect()
              .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
        depreciation =
            computeOnGoingDegressiveDepreciation(
                fixedAsset, previousFixedAssetLine, typeSelect, true);
      } else {
        depreciation = computeLinearDepreciation(fixedAsset, true);
      }
    }
    // case of economic type
    else {
      if (fixedAsset
          .getComputationMethodSelect()
          .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
        // In case of economic type, boolean argument is always false, since there is copy before,
        // we can pick values from non fiscal field.
        depreciation =
            computeOnGoingDegressiveDepreciation(
                fixedAsset, previousFixedAssetLine, typeSelect, false);
      } else {
        depreciation = computeLinearDepreciation(fixedAsset, false);
      }
    }
    // Second part
    if (depreciation
            .add(previousFixedAssetLine.getCumulativeDepreciation())
            .compareTo(fixedAsset.getGrossValue())
        > 0) {
      depreciation =
          fixedAsset.getGrossValue().subtract(previousFixedAssetLine.getCumulativeDepreciation());
    }
    return depreciation;
  }

  protected BigDecimal computeLinearDepreciation(
      FixedAsset fixedAsset, boolean isFiscalComputationMethod) {
    return computeDepreciationNumerator(
            fixedAsset.getGrossValue(),
            isFiscalComputationMethod
                ? fixedAsset.getFiscalNumberOfDepreciation()
                : fixedAsset.getNumberOfDepreciation())
        .setScale(RETURNED_SCALE, RoundingMode.HALF_UP);
  }

  protected BigDecimal computeOnGoingDegressiveDepreciation(
      FixedAsset fixedAsset,
      FixedAssetLine previousFixedAssetLine,
      int typeSelect,
      boolean isFiscalComputationMethod) {
    BigDecimal degressiveDepreciation =
        computeDegressiveDepreciation(
            previousFixedAssetLine.getAccountingValue(), fixedAsset, isFiscalComputationMethod);
    BigDecimal linearDepreciation =
        previousFixedAssetLine
            .getAccountingValue()
            .divide(
                BigDecimal.valueOf(
                    isFiscalComputationMethod
                        ? (fixedAsset.getFiscalNumberOfDepreciation()
                            - fixedAsset.getFiscalFixedAssetLineList().size())
                        : (fixedAsset.getNumberOfDepreciation()
                            - fixedAsset.getFixedAssetLineList().size())),
                RETURNED_SCALE,
                RoundingMode.HALF_UP);
    return degressiveDepreciation.max(linearDepreciation);
  }

  protected BigDecimal computeDegressiveDepreciation(
      BigDecimal baseValue, FixedAsset fixedAsset, boolean isFiscalComputationMethod) {
    BigDecimal ddRate =
        isFiscalComputationMethod
            ? fixedAsset.getFiscalDegressiveCoef()
            : fixedAsset.getDegressiveCoef();
    return computeDepreciationNumerator(
            baseValue,
            isFiscalComputationMethod
                ? fixedAsset.getFiscalNumberOfDepreciation()
                : fixedAsset.getNumberOfDepreciation())
        .multiply(ddRate)
        .setScale(RETURNED_SCALE, RoundingMode.HALF_UP);
  }

  protected BigDecimal computeInitialDepreciationNumerator(
      BigDecimal baseValue, FixedAsset fixedAsset, boolean isFiscalComputationMethod) {
    BigDecimal prorataTemporis = this.computeProrataTemporis(fixedAsset);
    return computeDepreciationNumerator(
            baseValue,
            isFiscalComputationMethod
                ? fixedAsset.getFiscalNumberOfDepreciation()
                : fixedAsset.getNumberOfDepreciation())
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
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine, int typeSelect) {

    BigDecimal depreciation = computeDepreciation(fixedAsset, previousFixedAssetLine, typeSelect);
    BigDecimal cumulativeDepreciation =
        previousFixedAssetLine.getCumulativeDepreciation().add(depreciation);
    BigDecimal accountingValue = fixedAsset.getGrossValue().subtract(cumulativeDepreciation);

    LocalDate depreciationDate;
    if (!fixedAsset.getFixedAssetCategory().getIsProrataTemporis()
        || fixedAsset.getAcquisitionDate().equals(fixedAsset.getFirstDepreciationDate())
        || accountingValue.signum() != 0) {
      depreciationDate = computeDepreciationDate(fixedAsset, previousFixedAssetLine, typeSelect);
    } else {
      depreciationDate = computeLastProrataDepreciationDate(fixedAsset, typeSelect);
    }
    BigDecimal depreciationBase = computeDepreciationBase(fixedAsset, typeSelect, accountingValue);
    return createPlannedFixedAssetLine(
        fixedAsset,
        depreciationDate,
        depreciation,
        cumulativeDepreciation,
        accountingValue,
        depreciationBase,
        typeSelect);
  }

  protected FixedAssetLine createPlannedFixedAssetLine(
      FixedAsset fixedAsset,
      LocalDate depreciationDate,
      BigDecimal depreciation,
      BigDecimal cumulativeDepreciation,
      BigDecimal accountingValue,
      BigDecimal depreciationBase,
      int typeSelect) {
    FixedAssetLine fixedAssetLine = new FixedAssetLine();
    fixedAssetLine.setFixedAsset(fixedAsset);
    fixedAssetLine.setStatusSelect(FixedAssetLineRepository.STATUS_PLANNED);
    fixedAssetLine.setDepreciationDate(depreciationDate);
    fixedAssetLine.setDepreciation(depreciation);
    fixedAssetLine.setCumulativeDepreciation(cumulativeDepreciation);
    fixedAssetLine.setAccountingValue(accountingValue);
    fixedAssetLine.setDepreciationBase(depreciationBase);
    fixedAssetLine.setTypeSelect(typeSelect);
    return fixedAssetLine;
  }

  protected LocalDate computeDepreciationDate(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine, int typeSelect) {
    LocalDate depreciationDate;
    depreciationDate =
        DateTool.plusMonths(
            previousFixedAssetLine.getDepreciationDate(),
            typeSelect == FixedAssetLineRepository.TYPE_SELECT_ECONOMIC
                ? fixedAsset.getPeriodicityInMonth()
                : fixedAsset.getFiscalPeriodicityInMonth());

    return depreciationDate;
  }

  protected LocalDate computeLastProrataDepreciationDate(FixedAsset fixedAsset, int typeSelect) {
    LocalDate d =
        DateTool.plusMonths(
            fixedAsset.getAcquisitionDate(),
            typeSelect == FixedAssetLineRepository.TYPE_SELECT_ECONOMIC
                ? fixedAsset.getDurationInMonth()
                : fixedAsset.getFiscalDurationInMonth());
    if (typeSelect == FixedAssetLineRepository.TYPE_SELECT_FISCAL) {
      if (FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE.equals(
          fixedAsset.getFiscalComputationMethodSelect())) {
        d = DateTool.minusMonths(d, fixedAsset.getFiscalPeriodicityInMonth());
      }
    } else {
      if (FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE.equals(
          fixedAsset.getComputationMethodSelect())) {
        d = DateTool.minusMonths(d, fixedAsset.getPeriodicityInMonth());
      }
    }

    return d.minusDays(1);
  }
}
