package com.axelor.apps.account.service.fixedasset;

import static com.axelor.apps.account.service.fixedasset.FixedAssetServiceImpl.CALCULATION_SCALE;
import static com.axelor.apps.account.service.fixedasset.FixedAssetServiceImpl.RETURNED_SCALE;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.Calendar;
import java.util.List;

public abstract class AbstractFixedAssetLineComputationServiceImpl
    implements FixedAssetLineComputationService {

  protected abstract LocalDate computeStartDepreciationDate(FixedAsset fixedAsset);

  protected abstract BigDecimal computeInitialDepreciationBase(FixedAsset fixedAsset);

  protected abstract BigDecimal computeInitialDepreciation(
      FixedAsset fixedAsset, BigDecimal baseValue);

  protected abstract BigDecimal computeDepreciationBase(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine);

  protected abstract BigDecimal computeDepreciation(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine, BigDecimal baseValue);

  protected abstract LocalDate computeDepreciationDate(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine);

  @Override
  public FixedAssetLine computePlannedFixedAssetLine(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine, int typeSelect) {

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
        typeSelect);
  }

  @Override
  public FixedAssetLine computeInitialPlannedFixedAssetLine(FixedAsset fixedAsset, int typeSelect) {
    LocalDate firstDepreciationDate;
    firstDepreciationDate = computeStartDepreciationDate(fixedAsset);
    BigDecimal depreciationBase = computeInitialDepreciationBase(fixedAsset);
    BigDecimal depreciation = computeInitialDepreciation(fixedAsset, depreciationBase);
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

  protected LocalDate computeProrataFirstDepreciationDate(FixedAsset fixedAsset) {
    return fixedAsset.getFirstDepreciationDate();
  }

  protected LocalDate computeProrataAcquisitionDate(FixedAsset fixedAsset) {
    return fixedAsset.getAcquisitionDate();
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

    LocalDate acquisitionDate = computeProrataAcquisitionDate(fixedAsset);
    LocalDate depreciationDate = computeProrataFirstDepreciationDate(fixedAsset);

    if (fixedAsset.getFixedAssetCategory().getIsProrataTemporis()
        && !acquisitionDate.equals(depreciationDate)) {
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
        BigDecimal.valueOf(fixedAsset.getPeriodicityInMonth() * 30)
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

  protected BigDecimal computeInitialDegressiveDepreciation(
      FixedAsset fixedAsset, BigDecimal baseValue) {
    BigDecimal ddRate = fixedAsset.getDegressiveCoef();
    return computeInitialDepreciationNumerator(baseValue, fixedAsset)
        .multiply(ddRate)
        .setScale(RETURNED_SCALE, RoundingMode.HALF_UP);
  }

  protected BigDecimal computeInitialDepreciationNumerator(
      BigDecimal baseValue, FixedAsset fixedAsset) {
    BigDecimal prorataTemporis = this.computeProrataTemporis(fixedAsset);
    return computeDepreciationNumerator(baseValue, fixedAsset.getNumberOfDepreciation())
        .multiply(prorataTemporis);
  }

  protected BigDecimal computeLinearDepreciation(FixedAsset fixedAsset, BigDecimal baseValue) {
    return computeDepreciationNumerator(baseValue, fixedAsset.getNumberOfDepreciation())
        .setScale(RETURNED_SCALE, RoundingMode.HALF_UP);
  }

  protected BigDecimal computeOnGoingDegressiveDepreciation(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine) {
    BigDecimal degressiveDepreciation =
        computeDegressiveDepreciation(previousFixedAssetLine.getAccountingValue(), fixedAsset);
    BigDecimal linearDepreciation =
        previousFixedAssetLine
            .getAccountingValue()
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

  protected long countNotCorrectedPlannedLines(List<FixedAssetLine> fixedAssetLineList) {
    return fixedAssetLineList.stream()
        .filter(
            line ->
                line.getCorrectedAccountingValue().signum() == 0
                    && line.getStatusSelect() == FixedAssetLineRepository.STATUS_PLANNED)
        .count();
  }
}
