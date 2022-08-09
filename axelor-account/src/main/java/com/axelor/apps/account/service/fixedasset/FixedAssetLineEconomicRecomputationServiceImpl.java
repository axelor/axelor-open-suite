package com.axelor.apps.account.service.fixedasset;

import static com.axelor.apps.account.service.fixedasset.FixedAssetServiceImpl.RETURNED_SCALE;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
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
  protected Integer getNumberOfDepreciation(FixedAsset fixedAsset) {
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
    return fixedAsset.getNumberOfDepreciation() - nbRealizedLines;
  }

  @Override
  protected int numberOfDepreciationDone(FixedAsset fixedAsset) {
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
      return fixedAssetLineList.size() + getNumberOfPastDepreciation(fixedAsset) - nbRealizedLines;
    }
    if (fixedAssetLineList == null) {
      return 0;
    }
    return fixedAssetLineList.size() - nbRealizedLines;
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
            baseValue, getNumberOfDepreciation(fixedAsset) + nbRealizedLines)
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
}
