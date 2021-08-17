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
import com.axelor.apps.account.db.FixedAssetDerogatoryLine;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.service.AnalyticFixedAssetService;
import com.axelor.apps.tool.date.DateTool;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.Calendar;
import java.util.List;

public class FixedAssetLineComputationServiceImpl implements FixedAssetLineComputationService {

  protected AnalyticFixedAssetService analyticFixedAssetService;
  protected FixedAssetDerogatoryLineService fixedAssetDerogatoryLineService;
  protected FixedAssetDerogatoryLineMoveService fixedAssetDerogatoryLineMoveService;

  @Inject
  public FixedAssetLineComputationServiceImpl(
      AnalyticFixedAssetService analyticFixedAssetService,
      FixedAssetDerogatoryLineService fixedAssetDerogatoryLineService,
      FixedAssetDerogatoryLineMoveService fixedAssetDerogatoryLineMoveService) {
    this.analyticFixedAssetService = analyticFixedAssetService;
    this.fixedAssetDerogatoryLineService = fixedAssetDerogatoryLineService;
    this.fixedAssetDerogatoryLineMoveService = fixedAssetDerogatoryLineMoveService;
  }

  @Override
  public FixedAssetLine computeInitialPlannedFixedAssetLine(FixedAsset fixedAsset, int typeSelect) {
    LocalDate firstDepreciationDate;
    firstDepreciationDate = computeStartDepreciationDate(fixedAsset, typeSelect);
    BigDecimal depreciationBase = computeInitialDepreciationBase(fixedAsset, typeSelect);
    BigDecimal depreciation = computeInitialDepreciation(fixedAsset, typeSelect, depreciationBase);
    BigDecimal accountingValue = depreciationBase.subtract(depreciation);

    return createPlannedFixedAssetLine(
        fixedAsset,
        firstDepreciationDate,
        depreciation,
        depreciation,
        accountingValue,
        depreciationBase,
        typeSelect);
  }

  private LocalDate computeStartDepreciationDate(FixedAsset fixedAsset, int typeSelect) {
    LocalDate firstDepreciationDate;
    if (typeSelect == FixedAssetLineRepository.TYPE_SELECT_ECONOMIC
        && !fixedAsset.getIsEqualToFiscalDepreciation()) {
      firstDepreciationDate =
          analyticFixedAssetService.computeFirstDepreciationDate(
              fixedAsset, fixedAsset.getFirstServiceDate());
    } else {
      firstDepreciationDate = fixedAsset.getFirstDepreciationDate();
    }
    return firstDepreciationDate;
  }

  protected BigDecimal computeInitialDepreciationBase(FixedAsset fixedAsset, int typeSelect) {
    if (typeSelect == FixedAssetLineRepository.TYPE_SELECT_ECONOMIC
        && !fixedAsset.getIsEqualToFiscalDepreciation()) {
      if (fixedAsset
          .getComputationMethodSelect()
          .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
        return fixedAsset.getGrossValue();
      } else {
        return fixedAsset.getGrossValue().subtract(fixedAsset.getResidualValue());
      }
    } else {
      return fixedAsset.getGrossValue();
    }
  }

  protected BigDecimal computeDepreciationBase(
      FixedAsset fixedAsset, int typeSelect, FixedAssetLine previousFixedAssetLine) {
    if (typeSelect == FixedAssetLineRepository.TYPE_SELECT_ECONOMIC
        && !fixedAsset.getIsEqualToFiscalDepreciation()) {
      if (fixedAsset
          .getComputationMethodSelect()
          .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
        return previousFixedAssetLine
            .getDepreciationBase()
            .subtract(previousFixedAssetLine.getDepreciation());
      } else {
        return previousFixedAssetLine.getDepreciationBase();
      }
    } else {
      if (fixedAsset
          .getFiscalComputationMethodSelect()
          .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
        return previousFixedAssetLine.getAccountingValue();
      }
      return previousFixedAssetLine.getDepreciationBase();
    }
  }

  protected BigDecimal computeInitialDepreciation(
      FixedAsset fixedAsset, int typeSelect, BigDecimal baseValue) {
    // Case of fiscal
    if (typeSelect == FixedAssetLineRepository.TYPE_SELECT_FISCAL) {
      // We always look at fiscal computation method for type select fiscal.
      if (fixedAsset.getFiscalComputationMethodSelect() != null
          && fixedAsset
              .getFiscalComputationMethodSelect()
              .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
        return computeInitialDegressiveDepreciation(fixedAsset, true, baseValue);
      } else {
        return computeInitialLinearDepreciation(fixedAsset, true, baseValue);
      }

    }
    // Case of Type Economic
    else {
      if (fixedAsset.getComputationMethodSelect() != null
          && fixedAsset
              .getComputationMethodSelect()
              .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
        // In case of economic type, boolean argument is always false, since there is copy before.
        return computeInitialDegressiveDepreciation(fixedAsset, false, baseValue);
      } else {
        return computeInitialLinearDepreciation(fixedAsset, false, baseValue);
      }
    }
  }

  protected BigDecimal computeInitialLinearDepreciation(
      FixedAsset fixedAsset, boolean isFiscalComputeMethod, BigDecimal baseValue) {
    return computeInitialDepreciationNumerator(baseValue, fixedAsset, isFiscalComputeMethod)
        .setScale(RETURNED_SCALE, RoundingMode.HALF_UP);
  }

  protected BigDecimal computeInitialDegressiveDepreciation(
      FixedAsset fixedAsset, boolean isFiscalComputationMethod, BigDecimal baseValue) {
    BigDecimal ddRate =
        isFiscalComputationMethod
            ? fixedAsset.getFiscalDegressiveCoef()
            : fixedAsset.getDegressiveCoef();
    return computeInitialDepreciationNumerator(baseValue, fixedAsset, isFiscalComputationMethod)
        .multiply(ddRate)
        .setScale(RETURNED_SCALE, RoundingMode.HALF_UP);
  }

  protected BigDecimal computeDepreciation(
      FixedAsset fixedAsset,
      FixedAssetLine previousFixedAssetLine,
      int typeSelect,
      BigDecimal baseValue) {
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
        depreciation = computeLinearDepreciation(fixedAsset, true, baseValue);
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
        depreciation = computeLinearDepreciation(fixedAsset, false, baseValue);
      }
    }
    if (BigDecimal.ZERO.compareTo(
            previousFixedAssetLine.getAccountingValue().subtract(depreciation))
        > 0) {
      depreciation = previousFixedAssetLine.getAccountingValue();
    }
    return depreciation;
  }

  protected BigDecimal computeLinearDepreciation(
      FixedAsset fixedAsset, boolean isFiscalComputationMethod, BigDecimal baseValue) {
    return computeDepreciationNumerator(
            baseValue,
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
    BigDecimal prorataTemporis = this.computeProrataTemporis(fixedAsset, isFiscalComputationMethod);
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

  protected BigDecimal computeProrataTemporis(
      FixedAsset fixedAsset, boolean isFiscalComputationMethod) {
    BigDecimal prorataTemporis = BigDecimal.ONE;

    LocalDate acquisitionDate;
    LocalDate depreciationDate;
    // If we are in economic lines && and economic is not equal to fiscal
    if (!isFiscalComputationMethod && !fixedAsset.getIsEqualToFiscalDepreciation()) {
      // Then acquisitionDate is first service date AND firstDateDepreciation is based on
      // firstServiceDate.
      acquisitionDate = fixedAsset.getFirstServiceDate();
      depreciationDate =
          analyticFixedAssetService.computeFirstDepreciationDate(
              fixedAsset, fixedAsset.getFirstServiceDate());
    } else {
      acquisitionDate = fixedAsset.getAcquisitionDate();
      depreciationDate = fixedAsset.getFirstDepreciationDate();
    }

    if (fixedAsset.getFixedAssetCategory().getIsProrataTemporis()
        && !acquisitionDate.equals(depreciationDate)) {

      acquisitionDate = acquisitionDate.minusDays(1);
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

    BigDecimal depreciation =
        computeDepreciation(
            fixedAsset,
            previousFixedAssetLine,
            typeSelect,
            previousFixedAssetLine.getDepreciationBase());
    BigDecimal depreciationBase =
        computeDepreciationBase(fixedAsset, typeSelect, previousFixedAssetLine);
    BigDecimal cumulativeDepreciation =
        previousFixedAssetLine.getCumulativeDepreciation().add(depreciation);
    BigDecimal accountingValue = previousFixedAssetLine.getAccountingValue().subtract(depreciation);

    LocalDate depreciationDate =
        computeDepreciationDate(fixedAsset, previousFixedAssetLine, typeSelect);

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
    LocalDate startDepreciationDate = computeStartDepreciationDate(fixedAsset, typeSelect);
    LocalDate d =
        DateTool.plusMonths(
            startDepreciationDate,
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

    return d;
  }

  @Override
  public void multiplyLinesBy(FixedAsset fixedAsset, BigDecimal prorata) throws AxelorException {

    List<FixedAssetLine> fixedAssetLineList = fixedAsset.getFixedAssetLineList();
    List<FixedAssetLine> fiscalAssetLineList = fixedAsset.getFiscalFixedAssetLineList();
    List<FixedAssetDerogatoryLine> fixedAssetDerogatoryLineList =
        fixedAsset.getFixedAssetDerogatoryLineList();
    if (fixedAssetLineList != null) {
      fixedAssetLineList.forEach(line -> multiplyLineBy(line, prorata));
    }
    if (fiscalAssetLineList != null) {
      fiscalAssetLineList.forEach(line -> multiplyLineBy(line, prorata));
    }
    if (fixedAsset
        .getDepreciationPlanSelect()
        .contains(FixedAssetRepository.DEPRECIATION_PLAN_DEROGATION)) {
      if (fixedAssetDerogatoryLineList != null) {
        fixedAssetDerogatoryLineList.clear();
        fixedAssetDerogatoryLineList.addAll(
            fixedAssetDerogatoryLineService.computeFixedAssetDerogatoryLineList(fixedAsset));
        for (FixedAssetDerogatoryLine line : fixedAssetDerogatoryLineList) {
          fixedAssetDerogatoryLineMoveService.realize(line);
        }
      }
    }
  }

  private void multiplyLineBy(FixedAssetLine line, BigDecimal prorata) {
    line.setDepreciationBase(
        prorata
            .multiply(line.getDepreciationBase())
            .setScale(RETURNED_SCALE, RoundingMode.HALF_UP));
    line.setDepreciation(
        prorata.multiply(line.getDepreciation()).setScale(RETURNED_SCALE, RoundingMode.HALF_UP));
    line.setCumulativeDepreciation(
        prorata
            .multiply(line.getCumulativeDepreciation())
            .setScale(RETURNED_SCALE, RoundingMode.HALF_UP));
    line.setAccountingValue(
        prorata.multiply(line.getAccountingValue()).setScale(RETURNED_SCALE, RoundingMode.HALF_UP));
    line.setCorrectedAccountingValue(
        prorata
            .multiply(line.getCorrectedAccountingValue())
            .setScale(RETURNED_SCALE, RoundingMode.HALF_UP));
    line.setImpairmentValue(
        prorata.multiply(line.getImpairmentValue()).setScale(RETURNED_SCALE, RoundingMode.HALF_UP));
  }
}
