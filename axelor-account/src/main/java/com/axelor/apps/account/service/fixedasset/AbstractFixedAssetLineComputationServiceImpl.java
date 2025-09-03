/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

import static com.axelor.apps.account.service.fixedasset.FixedAssetServiceImpl.CALCULATION_SCALE;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetCategory;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.utils.helpers.date.LocalDateHelper;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class of FixedAssetLineComputationService. This class is not supposed to be directly
 * used. Please use {@link FixedAssetLineEconomicComputationServiceImpl}, {@link
 * FixedAssetLineFiscalComputationServiceImpl} or {@link FixedAssetLineIfrsComputationServiceImpl}.
 */
public abstract class AbstractFixedAssetLineComputationServiceImpl
    implements FixedAssetLineComputationService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected FixedAssetFailOverControlService fixedAssetFailOverControlService;
  protected AppBaseService appBaseService;
  protected FixedAssetLineToolService fixedAssetLineToolService;

  protected abstract LocalDate computeStartDepreciationDate(FixedAsset fixedAsset);

  protected abstract BigDecimal computeInitialDepreciationBase(FixedAsset fixedAsset);

  protected abstract List<FixedAssetLine> getFixedAssetLineList(FixedAsset fixedAsset);

  protected abstract BigDecimal getNumberOfDepreciation(FixedAsset fixedAsset);

  protected abstract BigDecimal getNumberOfPastDepreciation(FixedAsset fixedAsset);

  protected abstract BigDecimal getAlreadyDepreciatedAmount(FixedAsset fixedAsset);

  protected abstract String getComputationMethodSelect(FixedAsset fixedAsset);

  protected abstract Integer getDurationInMonth(FixedAsset fixedAsset);

  protected abstract BigDecimal getDegressiveCoef(FixedAsset fixedAsset);

  protected abstract LocalDate computeProrataTemporisFirstDepreciationDate(FixedAsset fixedAsset);

  protected abstract Integer getPeriodicityInMonth(FixedAsset fixedAsset);

  protected abstract Integer getTypeSelect();

  protected abstract Boolean isProrataTemporis(FixedAsset fixedAsset);

  protected abstract BigDecimal getDepreciatedAmountCurrentYear(FixedAsset fixedAsset);

  protected abstract LocalDate getFailOverDepreciationEndDate(FixedAsset fixedAsset);

  protected abstract int getFirstDateDepreciationInitSelect(FixedAsset fixedAsset);

  @Inject
  protected AbstractFixedAssetLineComputationServiceImpl(
      FixedAssetFailOverControlService fixedAssetFailOverControlService,
      AppBaseService appBaseService,
      FixedAssetLineToolService fixedAssetLineToolService) {
    this.fixedAssetFailOverControlService = fixedAssetFailOverControlService;
    this.appBaseService = appBaseService;
    this.fixedAssetLineToolService = fixedAssetLineToolService;
  }

  @Override
  public FixedAssetLine computePlannedFixedAssetLine(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine) throws AxelorException {

    BigDecimal depreciation =
        computeDepreciation(
            fixedAsset, previousFixedAssetLine, previousFixedAssetLine.getDepreciationBase());
    BigDecimal depreciationBase = computeDepreciationBase(fixedAsset, previousFixedAssetLine);
    BigDecimal cumulativeDepreciation =
        previousFixedAssetLine.getCumulativeDepreciation().add(depreciation);
    BigDecimal accountingValue = getAccountingValue(previousFixedAssetLine).subtract(depreciation);

    LocalDate depreciationDate = computeDepreciationDate(fixedAsset, previousFixedAssetLine);

    return createFixedAssetLine(
        fixedAsset,
        depreciationDate,
        depreciation,
        fixedAssetLineToolService.getCompanyScaledValue(cumulativeDepreciation, fixedAsset),
        fixedAssetLineToolService.getCompanyScaledValue(accountingValue, fixedAsset),
        depreciationBase,
        getTypeSelect(),
        FixedAssetLineRepository.STATUS_PLANNED);
  }

  @Override
  public Optional<FixedAssetLine> computeInitialPlannedFixedAssetLine(FixedAsset fixedAsset)
      throws AxelorException {

    LocalDate firstDepreciationDate = computeStartDepreciationDate(fixedAsset);
    BigDecimal depreciationBase = computeInitialDepreciationBase(fixedAsset);
    BigDecimal depreciation = BigDecimal.ZERO;
    BigDecimal accountingValue = BigDecimal.ZERO;
    if (!isAlreadyDepreciated(fixedAsset)) {
      depreciation = computeInitialDepreciation(fixedAsset, depreciationBase);
      accountingValue =
          fixedAssetLineToolService.getCompanyScaledValue(
              depreciationBase, depreciation, fixedAsset, BigDecimal::subtract);
    }

    FixedAssetLine line =
        createFixedAssetLine(
            fixedAsset,
            firstDepreciationDate,
            depreciation,
            depreciation,
            accountingValue,
            depreciationBase,
            getTypeSelect(),
            FixedAssetLineRepository.STATUS_PLANNED);

    if (fixedAssetFailOverControlService.isFailOver(fixedAsset)) {
      if (isAlreadyDepreciated(fixedAsset)
          || fixedAssetLineToolService.equals(
              line.getDepreciationBase(), getAlreadyDepreciatedAmount(fixedAsset), fixedAsset)) {

        // Instead of producing 0 line, we will produce one line with the depreciation of the
        // current year
        // if the depreciation ended this year.
        if (getFailOverDepreciationEndDate(fixedAsset) != null
            && appBaseService.getTodayDate(fixedAsset.getCompany()).getYear()
                == getFailOverDepreciationEndDate(fixedAsset).getYear()) {
          return Optional.ofNullable(
              createFixedAssetLine(
                  fixedAsset,
                  getFailOverDepreciationEndDate(fixedAsset),
                  getDepreciatedAmountCurrentYear(fixedAsset),
                  depreciationBase,
                  BigDecimal.ZERO,
                  depreciationBase,
                  getTypeSelect(),
                  FixedAssetLineRepository.STATUS_REALIZED));
        } else {
          LocalDate depreciationEndDate = getFailOverDepreciationEndDate(fixedAsset);
          if (depreciationEndDate == null) {
            depreciationEndDate =
                LocalDateHelper.plusMonths(
                    fixedAsset.getFirstDepreciationDate(), getDurationInMonth(fixedAsset));
          }
          return Optional.ofNullable(
              createFixedAssetLine(
                  fixedAsset,
                  depreciationEndDate,
                  BigDecimal.ZERO,
                  depreciationBase,
                  BigDecimal.ZERO,
                  depreciationBase,
                  getTypeSelect(),
                  FixedAssetLineRepository.STATUS_REALIZED));
        }
      }
      line.setCumulativeDepreciation(
          fixedAssetLineToolService.getCompanyScaledValue(
              line.getCumulativeDepreciation(),
              getAlreadyDepreciatedAmount(fixedAsset),
              fixedAsset,
              BigDecimal::add));
      if (getComputationMethodSelect(fixedAsset)
          .equals(FixedAssetRepository.COMPUTATION_METHOD_LINEAR)) {
        line.setAccountingValue(
            fixedAssetLineToolService.getCompanyScaledValue(
                line.getAccountingValue(),
                getAlreadyDepreciatedAmount(fixedAsset),
                fixedAsset,
                BigDecimal::subtract));
      }
      if (fixedAssetLineToolService.equals(
          line.getDepreciationBase(), getAlreadyDepreciatedAmount(fixedAsset), fixedAsset)) {
        return Optional.empty();
      }
    }

    return Optional.ofNullable(line);
  }

  protected boolean isAlreadyDepreciated(FixedAsset fixedAsset) {
    BigDecimal expectedDepreciation =
        fixedAssetLineToolService.getCompanyScaledValue(
            fixedAsset.getGrossValue(),
            fixedAsset.getResidualValue(),
            fixedAsset,
            BigDecimal::subtract);

    BigDecimal actualDepreciation = getAlreadyDepreciatedAmount(fixedAsset);
    boolean isProrataTemporis =
        Optional.ofNullable(fixedAsset)
            .map(FixedAsset::getFixedAssetCategory)
            .map(FixedAssetCategory::getIsProrataTemporis)
            .orElse(false);
    if (isProrataTemporis) {
      return fixedAssetLineToolService.equals(expectedDepreciation, actualDepreciation, fixedAsset);
    }

    return fixedAssetLineToolService.equals(
        getNumberOfDepreciation(fixedAsset), getNumberOfPastDepreciation(fixedAsset), fixedAsset);
  }

  @Override
  public void multiplyLineBy(FixedAssetLine line, BigDecimal prorata) throws AxelorException {
    FixedAsset fixedAsset = line.getFixedAsset();
    line.setDepreciationBase(
        fixedAssetLineToolService.getCompanyScaledValue(
            line.getDepreciationBase(), prorata, fixedAsset, BigDecimal::multiply));
    line.setDepreciation(
        fixedAssetLineToolService.getCompanyScaledValue(
            line.getDepreciation(), prorata, fixedAsset, BigDecimal::multiply));
    line.setCumulativeDepreciation(
        fixedAssetLineToolService.getCompanyScaledValue(
            line.getCumulativeDepreciation(), prorata, fixedAsset, BigDecimal::multiply));
    line.setAccountingValue(
        fixedAssetLineToolService.getCompanyScaledValue(
            line.getAccountingValue(), prorata, fixedAsset, BigDecimal::multiply));
    line.setCorrectedAccountingValue(
        fixedAssetLineToolService.getCompanyScaledValue(
            line.getCorrectedAccountingValue(), prorata, fixedAsset, BigDecimal::multiply));
    line.setImpairmentValue(
        fixedAssetLineToolService.getCompanyScaledValue(
            line.getImpairmentValue(), prorata, fixedAsset, BigDecimal::multiply));
  }

  @Override
  public void multiplyLinesBy(List<FixedAssetLine> fixedAssetLineList, BigDecimal prorata)
      throws AxelorException {
    if (fixedAssetLineList != null) {
      for (FixedAssetLine fixedAssetLine : fixedAssetLineList) {
        multiplyLineBy(fixedAssetLine, prorata);
      }
    }
  }

  protected BigDecimal computeInitialDepreciation(FixedAsset fixedAsset, BigDecimal baseValue)
      throws AxelorException {

    if (fixedAsset == null) {
      return baseValue;
    }

    boolean hasLines = getFixedAssetLineList(fixedAsset) != null;
    boolean isProrata =
        Optional.ofNullable(fixedAsset)
            .map(FixedAsset::getFixedAssetCategory)
            .map(FixedAssetCategory::getIsProrataTemporis)
            .orElse(false);
    boolean isFailOver = fixedAssetFailOverControlService.isFailOver(fixedAsset);

    // How many periods remain (could be zero or negative if past schedule)
    BigDecimal remainingDepreciations =
        getNumberOfDepreciation(fixedAsset).subtract(numberOfDepreciationDone(fixedAsset));

    // Treat any remaining ≤ 1 as the final period
    boolean isLastDepreciation = remainingDepreciations.compareTo(BigDecimal.ONE) <= 0;

    String computationMethod = getComputationMethodSelect(fixedAsset);
    boolean isLinear = FixedAssetRepository.COMPUTATION_METHOD_LINEAR.equals(computationMethod);
    boolean isDegressive =
        FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE.equals(computationMethod);

    // Existing lines, last period, non‑prorata schedule
    if (hasLines && isLastDepreciation && !isProrata) {
      if (isFailOver && isLinear) {
        return getDepreciationDifference(fixedAsset, baseValue);
      }
      return baseValue;
    }

    // No lines yet, last period, not in fail‑over → scale difference
    if (!hasLines && isLastDepreciation && !isFailOver) {
      return getDepreciationDifference(fixedAsset, baseValue);
    }

    // Prorata + fail‑over + last or overdue → scale difference
    if (isProrata
        && isFailOver
        && isLastDepreciation
        && remainingDepreciations.compareTo(BigDecimal.ZERO) <= 0) {
      return getDepreciationDifference(fixedAsset, baseValue);
    }

    // Fallback: apply degressive or linear computation
    if (isDegressive) {
      return computeInitialDegressiveDepreciation(fixedAsset, baseValue);
    }
    return computeInitialLinearDepreciation(fixedAsset, baseValue);
  }

  protected BigDecimal getDepreciationDifference(FixedAsset fixedAsset, BigDecimal baseValue) {
    return fixedAssetLineToolService.getCompanyScaledValue(
        baseValue, getAlreadyDepreciatedAmount(fixedAsset), fixedAsset, BigDecimal::subtract);
  }

  protected BigDecimal computeInitialLinearDepreciation(
      FixedAsset fixedAsset, BigDecimal baseValue) {
    return fixedAssetLineToolService.getCompanyScaledValue(
        computeInitialDepreciationNumerator(baseValue, fixedAsset), fixedAsset);
  }

  protected BigDecimal computeDepreciationNumerator(
      BigDecimal baseValue, BigDecimal numberOfDepreciation) {
    BigDecimal depreciationRate = computeDepreciationRate(numberOfDepreciation);
    return baseValue.multiply(depreciationRate);
  }

  protected BigDecimal computeDepreciationRate(BigDecimal numberOfDepreciation) {
    return numberOfDepreciation.signum() == 0
        ? BigDecimal.ZERO
        : BigDecimal.ONE.divide(numberOfDepreciation, CALCULATION_SCALE, RoundingMode.HALF_UP);
  }

  protected BigDecimal computeProrataTemporis(FixedAsset fixedAsset) {
    BigDecimal prorataTemporis = BigDecimal.ONE;

    LocalDate acquisitionDate = computeProrataTemporisAcquisitionDate(fixedAsset);
    LocalDate depreciationDate = computeProrataTemporisFirstDepreciationDate(fixedAsset);

    if (isProrataTemporis(fixedAsset)) {
      prorataTemporis = computeProrataBetween(fixedAsset, acquisitionDate, depreciationDate, null);
    }
    return prorataTemporis;
  }

  protected BigDecimal computeProrataBetween(
      FixedAsset fixedAsset,
      LocalDate acquisitionDate,
      LocalDate depreciationDate,
      LocalDate nextDate) {
    int DEFAULT_DAYS_PER_MONTH = 30;
    BigDecimal prorataTemporis;

    boolean isUSProrataTemporis = fixedAsset.getFixedAssetCategory().getIsUSProrataTemporis();

    BigDecimal nbDaysBetweenAcqAndFirstDepDate;
    if (FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE.equals(
        fixedAsset.getComputationMethodSelect())) {
      nextDate = null;
      int endDayOfMonth =
          depreciationDate.getMonth() == Month.FEBRUARY
              ? depreciationDate.getDayOfMonth()
              : DEFAULT_DAYS_PER_MONTH;
      nbDaysBetweenAcqAndFirstDepDate =
          nbDaysBetween(
              isUSProrataTemporis,
              acquisitionDate.withDayOfMonth(1),
              depreciationDate.withDayOfMonth(endDayOfMonth));
    } else {
      nbDaysBetweenAcqAndFirstDepDate =
          nbDaysBetween(isUSProrataTemporis, acquisitionDate, depreciationDate);
    }

    BigDecimal maxNbDaysOfPeriod =
        BigDecimal.valueOf(
                getPeriodicityInMonthProrataTemporis(fixedAsset) * DEFAULT_DAYS_PER_MONTH)
            .setScale(CALCULATION_SCALE, RoundingMode.HALF_UP);
    BigDecimal nbDaysOfPeriod;
    if (nextDate != null) {
      nbDaysOfPeriod = nbDaysBetween(isUSProrataTemporis, acquisitionDate, nextDate);
      if (nbDaysOfPeriod.compareTo(maxNbDaysOfPeriod) > 0) {
        nbDaysOfPeriod = maxNbDaysOfPeriod;
      }
    } else {
      nbDaysOfPeriod = maxNbDaysOfPeriod;
    }

    prorataTemporis =
        nbDaysBetweenAcqAndFirstDepDate.divide(
            nbDaysOfPeriod, CALCULATION_SCALE, RoundingMode.HALF_UP);
    return prorataTemporis;
  }

  /**
   * Method only use in method computeProrataBetween
   *
   * @return
   */
  protected Integer getPeriodicityInMonthProrataTemporis(FixedAsset fixedAsset) {
    return getPeriodicityInMonth(fixedAsset);
  }

  /**
   * Method only use in method computeProrataBetween
   *
   * @return
   */
  protected BigDecimal nbDaysBetween(
      boolean isUsProrataTemporis, LocalDate startDate, LocalDate endDate) {
    int acquisitionYear = startDate.getYear();
    Month acquisitionMonth = startDate.getMonth();
    int acquisitionDay = startDate.getDayOfMonth();
    int depreciationYear = endDate.getYear();
    Month depreciationMonth = endDate.getMonth();
    int depreciationDay = endDate.getDayOfMonth();

    // US way
    if (isUsProrataTemporis) {

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

      if (depreciationMonth == Month.FEBRUARY
          && isLastDayOfFebruary(depreciationYear, depreciationDay)) {
        depreciationDay = 30;
      }

      if (acquisitionDay == 31) {
        acquisitionDay = 30;
      }

      if (depreciationDay == 31) {
        depreciationDay = 30;
      }
    }

    return BigDecimal.valueOf(
            360 * (depreciationYear - acquisitionYear)
                + 30 * (depreciationMonth.getValue() - acquisitionMonth.getValue())
                + (depreciationDay - acquisitionDay)
                + 1)
        .setScale(CALCULATION_SCALE, RoundingMode.HALF_UP);
  }

  protected boolean isLastDayOfFebruary(int year, int day) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(year, Calendar.FEBRUARY, 1);
    int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    return maxDays == day;
  }

  @Override
  public FixedAssetLine createFixedAssetLine(
      FixedAsset fixedAsset,
      LocalDate depreciationDate,
      BigDecimal depreciation,
      BigDecimal cumulativeDepreciation,
      BigDecimal accountingValue,
      BigDecimal depreciationBase,
      int typeSelect,
      int statusSelect) {
    FixedAssetLine fixedAssetLine = new FixedAssetLine();
    fixedAssetLine.setDepreciationDate(depreciationDate);
    fixedAssetLine.setDepreciation(depreciation);
    fixedAssetLine.setCumulativeDepreciation(cumulativeDepreciation);
    fixedAssetLine.setAccountingValue(accountingValue);
    fixedAssetLine.setDepreciationBase(depreciationBase);
    fixedAssetLine.setTypeSelect(typeSelect);
    fixedAssetLine.setStatusSelect(statusSelect);
    return fixedAssetLine;
  }

  protected BigDecimal computeInitialDegressiveDepreciation(
      FixedAsset fixedAsset, BigDecimal baseValue) throws AxelorException {
    BigDecimal ddRate = getDegressiveCoef(fixedAsset);
    return fixedAssetLineToolService.getCompanyScaledValue(
        computeInitialDepreciationNumerator(baseValue, fixedAsset),
        ddRate,
        fixedAsset,
        BigDecimal::multiply);
  }

  protected BigDecimal computeInitialDepreciationNumerator(
      BigDecimal baseValue, FixedAsset fixedAsset) {
    BigDecimal prorataTemporis = this.computeProrataTemporis(fixedAsset);
    return fixedAssetLineToolService.getCompanyScaledValue(
        computeDepreciationNumerator(baseValue, getNumberOfDepreciation(fixedAsset)),
        prorataTemporis,
        fixedAsset,
        BigDecimal::multiply);
  }

  protected BigDecimal computeLinearDepreciation(FixedAsset fixedAsset, BigDecimal baseValue) {
    return fixedAssetLineToolService.getCompanyScaledValue(
        computeDepreciationNumerator(baseValue, getNumberOfDepreciation(fixedAsset)), fixedAsset);
  }

  protected BigDecimal computeOnGoingDegressiveDepreciation(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine) throws AxelorException {
    BigDecimal previousAccountingValue = getAccountingValue(previousFixedAssetLine);
    BigDecimal degressiveDepreciation =
        computeDegressiveDepreciation(previousAccountingValue, fixedAsset);

    BigDecimal remainingNumberOfDepreciation =
        getNumberOfDepreciation(fixedAsset).subtract(numberOfDepreciationDone(fixedAsset));
    BigDecimal linearDepreciation =
        fixedAssetLineToolService.getCompanyDivideScaledValue(
            previousAccountingValue, remainingNumberOfDepreciation, fixedAsset);
    BigDecimal depreciation;
    if (fixedAsset.getGrossValue().signum() > 0) {
      depreciation = degressiveDepreciation.max(linearDepreciation);
    } else {
      depreciation = degressiveDepreciation.min(linearDepreciation);
    }
    return fixedAssetLineToolService.getCompanyScaledValue(depreciation, fixedAsset);
  }

  protected BigDecimal numberOfDepreciationDone(FixedAsset fixedAsset) {
    List<FixedAssetLine> fixedAssetLineList = getFixedAssetLineList(fixedAsset);
    if (fixedAssetFailOverControlService.isFailOver(fixedAsset)) {
      if (fixedAssetLineList == null) {
        return getNumberOfPastDepreciation(fixedAsset);
      }
      return BigDecimal.valueOf(fixedAssetLineList.size())
          .add(getNumberOfPastDepreciation(fixedAsset));
    }
    if (fixedAssetLineList == null) {
      return BigDecimal.ZERO;
    }
    return BigDecimal.valueOf(fixedAssetLineList.size());
  }

  protected BigDecimal computeDegressiveDepreciation(BigDecimal baseValue, FixedAsset fixedAsset) {
    BigDecimal ddRate = getDegressiveCoef(fixedAsset);
    return fixedAssetLineToolService.getCompanyScaledValue(
        computeDepreciationNumerator(baseValue, getNumberOfDepreciation(fixedAsset)),
        ddRate,
        fixedAsset,
        BigDecimal::multiply);
  }

  protected long countNotCorrectedPlannedLines(List<FixedAssetLine> fixedAssetLineList) {
    return fixedAssetLineList.stream()
        .filter(
            line ->
                line.getCorrectedAccountingValue().signum() == 0
                    && line.getStatusSelect() == FixedAssetLineRepository.STATUS_PLANNED)
        .count();
  }

  protected LocalDate computeDepreciationDate(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine) {
    LocalDate depreciationDate;
    // In prorataTemporis, the system will generate one additional line.
    // This check if we are generating the additional line, and in this case, the depreciation date
    // is different.
    if (isProrataTemporis(fixedAsset)
        && numberOfDepreciationDone(fixedAsset)
            .equals(getNumberOfDepreciation(fixedAsset).setScale(0, RoundingMode.DOWN))) {
      depreciationDate = computeLastProrataDepreciationDate(fixedAsset, previousFixedAssetLine);
    } else {
      depreciationDate =
          LocalDateHelper.plusMonths(
              previousFixedAssetLine.getDepreciationDate(), getPeriodicityInMonth(fixedAsset));
    }

    return depreciationDate;
  }

  protected BigDecimal computeDepreciation(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine, BigDecimal baseValue)
      throws AxelorException {
    BigDecimal depreciation;
    BigDecimal previousAccountingValue = getAccountingValue(previousFixedAssetLine);
    // If we are at the last line, we depreciate the remaining amount
    if (!fixedAssetFailOverControlService.isFailOver(fixedAsset)
        && !isProrataTemporis(fixedAsset)
        && getNumberOfDepreciation(fixedAsset)
            .equals(numberOfDepreciationDone(fixedAsset).add(BigDecimal.ONE))) {
      return previousAccountingValue;
    }
    if (getComputationMethodSelect(fixedAsset)
        .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
      depreciation = computeOnGoingDegressiveDepreciation(fixedAsset, previousFixedAssetLine);

    } else {
      depreciation = computeLinearDepreciation(fixedAsset, baseValue);
    }
    if (fixedAsset.getGrossValue().signum() > 0) {
      if (fixedAssetLineToolService.isGreaterThan(
          depreciation, previousAccountingValue, fixedAsset)) {
        depreciation = previousAccountingValue;
      }
    } else {
      if (fixedAssetLineToolService.isGreaterThan(
          previousAccountingValue, depreciation, fixedAsset)) {
        depreciation = previousAccountingValue;
      }
    }
    return fixedAssetLineToolService.getCompanyScaledValue(depreciation, fixedAsset);
  }

  protected BigDecimal computeDepreciationBase(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine) throws AxelorException {

    if (getComputationMethodSelect(fixedAsset)
        .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
      return getAccountingValue(previousFixedAssetLine);
    }
    return fixedAssetLineToolService.getCompanyScaledValue(
        previousFixedAssetLine.getDepreciationBase(), fixedAsset);
  }

  protected LocalDate computeLastProrataDepreciationDate(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine) {

    LocalDate firstServiceDate =
        fixedAsset.getFirstServiceDate() == null
            ? fixedAsset.getAcquisitionDate()
            : fixedAsset.getFirstServiceDate();
    LocalDate d = LocalDateHelper.plusMonths(firstServiceDate, getDurationInMonth(fixedAsset));
    if (FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE.equals(
        getComputationMethodSelect(fixedAsset))) {
      d = LocalDateHelper.minusMonths(d, getPeriodicityInMonth(fixedAsset));
    } else {
      d =
          LocalDateHelper.plusMonths(
              previousFixedAssetLine.getDepreciationDate(), getPeriodicityInMonth(fixedAsset));
    }
    return d;
  }

  protected BigDecimal getAccountingValue(FixedAssetLine fixedAssetLine) throws AxelorException {
    if (fixedAssetLine == null) {
      return BigDecimal.ZERO;
    }
    return fixedAssetLineToolService.getCompanyScaledValue(
        (fixedAssetLine.getCorrectedAccountingValue().signum() != 0
            ? fixedAssetLine.getCorrectedAccountingValue()
            : fixedAssetLine.getAccountingValue()),
        fixedAssetLine);
  }

  protected LocalDate computeProrataTemporisAcquisitionDate(FixedAsset fixedAsset) {
    LocalDate date;
    if (getFirstDateDepreciationInitSelect(fixedAsset)
            == FixedAssetRepository.REFERENCE_FIRST_DEPRECIATION_FIRST_SERVICE_DATE
        && fixedAsset.getFirstServiceDate() != null) {
      date = fixedAsset.getFirstServiceDate();
    } else {
      date = fixedAsset.getAcquisitionDate();
    }

    if (getComputationMethodSelect(fixedAsset)
        .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
      return date.withDayOfMonth(1);
    }
    return date;
  }
}
