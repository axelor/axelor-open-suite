package com.axelor.apps.account.service.fixedasset;

import static com.axelor.apps.account.service.fixedasset.FixedAssetServiceImpl.RETURNED_SCALE;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.service.AnalyticFixedAssetService;
import com.axelor.apps.tool.date.DateTool;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

@RequestScoped
public class FixedAssetLineFiscalComputationServiceImpl
    extends AbstractFixedAssetLineComputationServiceImpl {

  protected AnalyticFixedAssetService analyticFixedAssetService;
  protected FixedAssetDerogatoryLineService fixedAssetDerogatoryLineService;
  protected FixedAssetDerogatoryLineMoveService fixedAssetDerogatoryLineMoveService;

  @Inject
  public FixedAssetLineFiscalComputationServiceImpl(
      AnalyticFixedAssetService analyticFixedAssetService,
      FixedAssetDerogatoryLineService fixedAssetDerogatoryLineService,
      FixedAssetDerogatoryLineMoveService fixedAssetDerogatoryLineMoveService) {
    this.analyticFixedAssetService = analyticFixedAssetService;
    this.fixedAssetDerogatoryLineService = fixedAssetDerogatoryLineService;
    this.fixedAssetDerogatoryLineMoveService = fixedAssetDerogatoryLineMoveService;
  }

  @Override
  protected LocalDate computeStartDepreciationDate(FixedAsset fixedAsset) {
    return fixedAsset.getFirstDepreciationDate();
  }

  @Override
  protected BigDecimal computeInitialDepreciationBase(FixedAsset fixedAsset) {
    return fixedAsset.getGrossValue();
  }

  @Override
  protected BigDecimal computeInitialDepreciation(FixedAsset fixedAsset, BigDecimal baseValue) {
    Objects.requireNonNull(fixedAsset);
    // We always look at fiscal computation method for type select fiscal.
    if (fixedAsset.getFiscalComputationMethodSelect() != null
        && fixedAsset
            .getFiscalComputationMethodSelect()
            .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
      // Theses cases is for when user want to depreciate in one year.
      // This case is if list is not empty when calling this method
      if (fixedAsset.getFiscalFixedAssetLineList() != null
          && fixedAsset.getFiscalFixedAssetLineList().size()
              == fixedAsset.getFiscalNumberOfDepreciation() - 1) {
        return baseValue;
      }
      if (fixedAsset.getFiscalFixedAssetLineList() == null
          && fixedAsset.getFiscalNumberOfDepreciation() == 1) {
        return baseValue;
      }

      return computeInitialDegressiveDepreciation(fixedAsset, baseValue);
    } else {
      return computeInitialLinearDepreciation(fixedAsset, baseValue);
    }
  }

  @Override
  protected BigDecimal computeInitialDegressiveDepreciation(
      FixedAsset fixedAsset, BigDecimal baseValue) {
    BigDecimal ddRate = fixedAsset.getFiscalDegressiveCoef();
    return computeInitialDepreciationNumerator(baseValue, fixedAsset)
        .multiply(ddRate)
        .setScale(RETURNED_SCALE, RoundingMode.HALF_UP);
  }

  @Override
  protected BigDecimal computeInitialDepreciationNumerator(
      BigDecimal baseValue, FixedAsset fixedAsset) {
    BigDecimal prorataTemporis = this.computeProrataTemporis(fixedAsset);
    return computeDepreciationNumerator(baseValue, fixedAsset.getFiscalNumberOfDepreciation())
        .multiply(prorataTemporis);
  }

  @Override
  protected BigDecimal computeDepreciationBase(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine) {

    if (fixedAsset
        .getFiscalComputationMethodSelect()
        .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
      return previousFixedAssetLine.getAccountingValue();
    }
    return previousFixedAssetLine.getDepreciationBase();
  }

  @Override
  protected LocalDate computeDepreciationDate(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine) {
    LocalDate depreciationDate;
    depreciationDate =
        DateTool.plusMonths(
            previousFixedAssetLine.getDepreciationDate(), fixedAsset.getFiscalPeriodicityInMonth());

    return depreciationDate;
  }

  @Override
  protected BigDecimal computeLinearDepreciation(FixedAsset fixedAsset, BigDecimal baseValue) {
    return computeDepreciationNumerator(baseValue, fixedAsset.getFiscalNumberOfDepreciation())
        .setScale(RETURNED_SCALE, RoundingMode.HALF_UP);
  }

  @Override
  protected BigDecimal computeOnGoingDegressiveDepreciation(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine) {
    BigDecimal degressiveDepreciation =
        computeDegressiveDepreciation(previousFixedAssetLine.getAccountingValue(), fixedAsset);
    BigDecimal linearDepreciation =
        previousFixedAssetLine
            .getAccountingValue()
            .divide(
                BigDecimal.valueOf(
                    fixedAsset.getFiscalNumberOfDepreciation()
                        - fixedAsset.getFiscalFixedAssetLineList().size()),
                RETURNED_SCALE,
                RoundingMode.HALF_UP);
    return degressiveDepreciation.max(linearDepreciation);
  }

  @Override
  protected BigDecimal computeDegressiveDepreciation(BigDecimal baseValue, FixedAsset fixedAsset) {
    BigDecimal ddRate = fixedAsset.getFiscalDegressiveCoef();
    return computeDepreciationNumerator(baseValue, fixedAsset.getFiscalNumberOfDepreciation())
        .multiply(ddRate)
        .setScale(RETURNED_SCALE, RoundingMode.HALF_UP);
  }

  @Override
  protected BigDecimal computeDepreciation(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine, BigDecimal baseValue) {
    BigDecimal depreciation;

    // We always look at fiscalComputationMethodSelect
    if (fixedAsset.getFiscalComputationMethodSelect() != null
        && fixedAsset
            .getFiscalComputationMethodSelect()
            .equals(FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE)) {
      // At this stage, if list size == nb of depreciation -1 then it means we are processing the
      // last line.
      if (fixedAsset.getFiscalFixedAssetLineList() != null
          && fixedAsset.getFiscalFixedAssetLineList().size()
              == fixedAsset.getFiscalNumberOfDepreciation() - 1) {
        depreciation = previousFixedAssetLine.getAccountingValue();
      } else {
        depreciation = computeOnGoingDegressiveDepreciation(fixedAsset, previousFixedAssetLine);
      }

    } else {
      // In case of linear, we must filter line that have a correctedAccountingValue and line that
      // are realized and not count them to know if we are computing the last line.
      // Because when recomputing, number of depreciation is overwrite as follow (nbDepreciation -
      // list.size())
      if (fixedAsset.getFiscalFixedAssetLineList() != null
          && super.countNotCorrectedPlannedLines(fixedAsset.getFiscalFixedAssetLineList())
              == fixedAsset.getFiscalNumberOfDepreciation() - 1) {
        // So we must depreciate the remaining accounting value.
        depreciation = previousFixedAssetLine.getAccountingValue();
      } else {
        depreciation = computeLinearDepreciation(fixedAsset, baseValue);
      }
    }
    if (BigDecimal.ZERO.compareTo(
            previousFixedAssetLine.getAccountingValue().subtract(depreciation))
        > 0) {
      depreciation = previousFixedAssetLine.getAccountingValue();
    }
    return depreciation;
  }
}
