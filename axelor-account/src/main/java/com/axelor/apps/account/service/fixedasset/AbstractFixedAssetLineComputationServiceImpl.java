package com.axelor.apps.account.service.fixedasset;

import static com.axelor.apps.account.service.fixedasset.FixedAssetServiceImpl.CALCULATION_SCALE;
import static com.axelor.apps.account.service.fixedasset.FixedAssetServiceImpl.RETURNED_SCALE;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.tool.date.DateTool;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

/**
 * Abstract class of FixedAssetLineComputationService. This class is not supposed to be directly
 * used. Please use {@link FixedAssetLineEconomicComputationServiceImpl} or {@link
 * FixedAssetLineFiscalComputationServiceImpl}.
 */
public abstract class AbstractFixedAssetLineComputationServiceImpl
    implements FixedAssetLineComputationService {
  protected FixedAssetFailOverControlService fixedAssetFailOverControlService;

  protected abstract LocalDate computeStartDepreciationDate(FixedAsset fixedAsset);

  protected abstract BigDecimal computeInitialDepreciationBase(FixedAsset fixedAsset);

  protected abstract List<FixedAssetLine> getFixedAssetLineList(FixedAsset fixedAsset);

  protected abstract Integer getNumberOfDepreciation(FixedAsset fixedAsset);

  protected abstract Integer getNumberOfPastDepreciation(FixedAsset fixedAsset);

  protected abstract BigDecimal getAlreadyDepreciatedAmount(FixedAsset fixedAsset);

  protected abstract String getComputationMethodSelect(FixedAsset fixedAsset);

  protected abstract BigDecimal getDegressiveCoef(FixedAsset fixedAsset);

  protected abstract LocalDate computeProrataTemporisFirstDepreciationDate(FixedAsset fixedAsset);

  protected abstract LocalDate computeProrataTemporisAcquisitionDate(FixedAsset fixedAsset);

  protected abstract Integer getPeriodicityInMonth(FixedAsset fixedAsset);

  protected abstract Integer getTypeSelect();

  protected abstract Boolean isProrataTemporis(FixedAsset fixedAsset);

  @Inject
  public AbstractFixedAssetLineComputationServiceImpl(
      FixedAssetFailOverControlService fixedAssetFailOverControlService) {
    this.fixedAssetFailOverControlService = fixedAssetFailOverControlService;
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
    BigDecimal accountingValue = previousFixedAssetLine.getAccountingValue().subtract(depreciation);

    LocalDate depreciationDate = computeDepreciationDate(fixedAsset, previousFixedAssetLine);

    return createPlannedFixedAssetLine(
        fixedAsset,
        depreciationDate,
        depreciation,
        cumulativeDepreciation,
        accountingValue,
        depreciationBase,
        getTypeSelect());
  }

  @Override
  public Optional<FixedAssetLine> computeInitialPlannedFixedAssetLine(FixedAsset fixedAsset)
      throws AxelorException {

    if (isAlreadyDepreciated(fixedAsset)) {
      return Optional.empty();
    }
    LocalDate firstDepreciationDate;
    firstDepreciationDate = computeStartDepreciationDate(fixedAsset);
    BigDecimal depreciationBase = computeInitialDepreciationBase(fixedAsset);
    BigDecimal depreciation = computeInitialDepreciation(fixedAsset, depreciationBase);
    BigDecimal accountingValue = depreciationBase.subtract(depreciation);

    FixedAssetLine line =
        createPlannedFixedAssetLine(
            fixedAsset,
            firstDepreciationDate,
            depreciation,
            depreciation,
            accountingValue,
            depreciationBase,
            getTypeSelect());

    if (fixedAssetFailOverControlService.isFailOver(fixedAsset)) {
      line.setCumulativeDepreciation(
          line.getCumulativeDepreciation().add(getAlreadyDepreciatedAmount(fixedAsset)));
      if (getComputationMethodSelect(fixedAsset)
          .equals(FixedAssetRepository.COMPUTATION_METHOD_LINEAR)) {
        line.setAccountingValue(
            line.getAccountingValue().subtract(getAlreadyDepreciatedAmount(fixedAsset)));
      }
    }

    return Optional.ofNullable(line);
  }

  protected boolean isAlreadyDepreciated(FixedAsset fixedAsset) {

    return getNumberOfDepreciation(fixedAsset) == getNumberOfPastDepreciation(fixedAsset);
  }

  @Override
  public void multiplyLineBy(FixedAssetLine line, BigDecimal prorata) {
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

  @Override
  public void multiplyLinesBy(List<FixedAssetLine> fixedAssetLineList, BigDecimal prorata) {
    if (fixedAssetLineList != null) {
      fixedAssetLineList.forEach(line -> multiplyLineBy(line, prorata));
    }
  }

  protected BigDecimal computeInitialDepreciation(FixedAsset fixedAsset, BigDecimal baseValue) {
    // Theses cases is for when user want to depreciate in one year.
    // This case is if list is not empty when calling this method
    if (getFixedAssetLineList(fixedAsset) != null
        && numberOfDepreciationDone(fixedAsset) == getNumberOfDepreciation(fixedAsset) - 1) {
      if (fixedAssetFailOverControlService.isFailOver(fixedAsset)
          && getComputationMethodSelect(fixedAsset)
              .equals(FixedAssetRepository.COMPUTATION_METHOD_LINEAR)) {
        return baseValue.subtract(getAlreadyDepreciatedAmount(fixedAsset));
      }
      return baseValue;
    }
    if (getFixedAssetLineList(fixedAsset) == null
        && getNumberOfDepreciation(fixedAsset) - numberOfDepreciationDone(fixedAsset) == 1) {
      return baseValue.subtract(getAlreadyDepreciatedAmount(fixedAsset));
    }
    if (getComputationMethodSelect(fixedAsset) != null
        && getComputationMethodSelect(fixedAsset)
            .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {

      return computeInitialDegressiveDepreciation(fixedAsset, baseValue);
    } else {
      return computeInitialLinearDepreciation(fixedAsset, baseValue);
    }
  }

  protected BigDecimal computeInitialLinearDepreciation(
      FixedAsset fixedAsset, BigDecimal baseValue) {
    return computeInitialDepreciationNumerator(baseValue, fixedAsset)
        .setScale(RETURNED_SCALE, RoundingMode.HALF_UP);
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

    LocalDate acquisitionDate = computeProrataTemporisAcquisitionDate(fixedAsset);
    LocalDate depreciationDate = computeProrataTemporisFirstDepreciationDate(fixedAsset);

    if (isProrataTemporis(fixedAsset) && !acquisitionDate.equals(depreciationDate)) {
      prorataTemporis = computeProrataBetween(fixedAsset, acquisitionDate, depreciationDate);
    }
    return prorataTemporis;
  }

  protected BigDecimal computeProrataBetween(
      FixedAsset fixedAsset, LocalDate acquisitionDate, LocalDate depreciationDate) {
    BigDecimal prorataTemporis;
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
        BigDecimal.valueOf(getPeriodicityInMonth(fixedAsset) * 30)
            .setScale(CALCULATION_SCALE, RoundingMode.HALF_UP);
    prorataTemporis =
        nbDaysBetweenAcqAndFirstDepDate.divide(
            nbDaysOfPeriod, CALCULATION_SCALE, RoundingMode.HALF_UP);
    return prorataTemporis;
  }

  protected boolean isLastDayOfFebruary(int year, int day) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(year, Calendar.FEBRUARY, 1);
    int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    return maxDays == day;
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
    fixedAssetLine.setStatusSelect(FixedAssetLineRepository.STATUS_PLANNED);
    fixedAssetLine.setDepreciationDate(depreciationDate);
    fixedAssetLine.setDepreciation(depreciation);
    fixedAssetLine.setCumulativeDepreciation(cumulativeDepreciation);
    fixedAssetLine.setAccountingValue(accountingValue);
    fixedAssetLine.setDepreciationBase(depreciationBase);
    fixedAssetLine.setTypeSelect(typeSelect);
    return fixedAssetLine;
  }

  protected BigDecimal computeInitialDegressiveDepreciation(
      FixedAsset fixedAsset, BigDecimal baseValue) {
    BigDecimal ddRate = getDegressiveCoef(fixedAsset);
    return computeInitialDepreciationNumerator(baseValue, fixedAsset)
        .multiply(ddRate)
        .setScale(RETURNED_SCALE, RoundingMode.HALF_UP);
  }

  protected BigDecimal computeInitialDepreciationNumerator(
      BigDecimal baseValue, FixedAsset fixedAsset) {
    BigDecimal prorataTemporis = this.computeProrataTemporis(fixedAsset);
    return computeDepreciationNumerator(baseValue, getNumberOfDepreciation(fixedAsset))
        .multiply(prorataTemporis);
  }

  protected BigDecimal computeLinearDepreciation(FixedAsset fixedAsset, BigDecimal baseValue) {
    return computeDepreciationNumerator(baseValue, getNumberOfDepreciation(fixedAsset))
        .setScale(RETURNED_SCALE, RoundingMode.HALF_UP);
  }

  protected BigDecimal computeOnGoingDegressiveDepreciation(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine) {
    BigDecimal degressiveDepreciation =
        computeDegressiveDepreciation(previousFixedAssetLine.getAccountingValue(), fixedAsset);

    int remainingNumberOfDepreciation =
        getNumberOfDepreciation(fixedAsset) - numberOfDepreciationDone(fixedAsset);
    BigDecimal linearDepreciation =
        previousFixedAssetLine
            .getAccountingValue()
            .divide(
                BigDecimal.valueOf(remainingNumberOfDepreciation),
                RETURNED_SCALE,
                RoundingMode.HALF_UP);
    return degressiveDepreciation.max(linearDepreciation);
  }

  protected int numberOfDepreciationDone(FixedAsset fixedAsset) {
    List<FixedAssetLine> fixedAssetLineList = getFixedAssetLineList(fixedAsset);
    if (fixedAssetFailOverControlService.isFailOver(fixedAsset)) {
      if (fixedAssetLineList == null) {
        return getNumberOfPastDepreciation(fixedAsset);
      }
      return fixedAssetLineList.size() + getNumberOfPastDepreciation(fixedAsset);
    }
    if (fixedAssetLineList == null) {
      return 0;
    }
    return fixedAssetLineList.size();
  }

  protected BigDecimal computeDegressiveDepreciation(BigDecimal baseValue, FixedAsset fixedAsset) {
    BigDecimal ddRate = getDegressiveCoef(fixedAsset);
    return computeDepreciationNumerator(baseValue, getNumberOfDepreciation(fixedAsset))
        .multiply(ddRate)
        .setScale(RETURNED_SCALE, RoundingMode.HALF_UP);
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
    depreciationDate =
        DateTool.plusMonths(
            previousFixedAssetLine.getDepreciationDate(), getPeriodicityInMonth(fixedAsset));

    return depreciationDate;
  }

  protected BigDecimal computeDepreciation(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine, BigDecimal baseValue) {
    BigDecimal depreciation;
    if (getFixedAssetLineList(fixedAsset) != null
        && numberOfDepreciationDone(fixedAsset) == getNumberOfDepreciation(fixedAsset) - 1) {
      depreciation = previousFixedAssetLine.getAccountingValue();
    } else if (getComputationMethodSelect(fixedAsset)
        .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
      depreciation = computeOnGoingDegressiveDepreciation(fixedAsset, previousFixedAssetLine);

    } else {
      depreciation = computeLinearDepreciation(fixedAsset, baseValue);
    }
    if (BigDecimal.ZERO.compareTo(
            previousFixedAssetLine.getAccountingValue().subtract(depreciation))
        > 0) {
      depreciation = previousFixedAssetLine.getAccountingValue();
    }
    return depreciation;
  }

  protected BigDecimal computeDepreciationBase(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine) {

    if (getComputationMethodSelect(fixedAsset)
        .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
      return previousFixedAssetLine.getAccountingValue();
    }
    return previousFixedAssetLine.getDepreciationBase();
  }
}
