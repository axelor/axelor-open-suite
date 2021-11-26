package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.service.AnalyticFixedAssetService;
import com.axelor.apps.tool.date.DateTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation class {@link FixedAssetLineComputationService}. This class purpose is to update a
 * economic list of a fixed asset using corrected accouting value of {@link FixedAsset}. If fixed
 * asset doesn't have that value or that is equals or lesser than 0, do not use this implementation.
 * Also unlike others implementations like {@link FixedAssetLineEconomicComputationServiceImpl} or
 * {@link FixedAssetLineFiscalComputationServiceImpl} this implementations will clear planned lines
 * from the {@link FixedAsset#getFixedAssetLineList()} except the first one, then do the
 * recomputation starting from it.
 */
@RequestScoped
public class FixedAssetLineEconomicUpdateComputationServiceImpl
    extends AbstractFixedAssetLineComputationServiceImpl {

  protected FixedAssetLineService fixedAssetLineService;
  protected AnalyticFixedAssetService analyticFixedAssetService;
  private boolean canGenerateLines = false;
  private FixedAssetLine firstPlannedFixedAssetLine;
  private int listSizeAfterClear;

  @Inject
  public FixedAssetLineEconomicUpdateComputationServiceImpl(
      FixedAssetFailOverControlService fixedAssetFailOverControlService,
      FixedAssetLineService fixedAssetLineService,
      AnalyticFixedAssetService analyticFixedAssetService) {
    super(fixedAssetFailOverControlService);
    this.fixedAssetLineService = fixedAssetLineService;
    this.analyticFixedAssetService = analyticFixedAssetService;
  }

  @Override
  /**
   * {@inheritDoc}
   *
   * <p>You can not call this method implementation without calling {@link
   * FixedAssetLineEconomicUpdateComputationServiceImpl#computeInitialPlannedFixedAssetLine(FixedAsset)}
   * at least once.
   */
  public FixedAssetLine computePlannedFixedAssetLine(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine) throws AxelorException {
    if (!canGenerateLines) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          "You can not generate lines with this implementation without calling computeInitialFixedAssetLine");
    }
    return super.computePlannedFixedAssetLine(fixedAsset, previousFixedAssetLine);
  }

  /**
   * Recompute first planned line of fixedAssetLineList, therefore This method requires that {@link
   * FixedAsset#getFixedAssetLineList()} is not null or empty.
   */
  @Override
  public Optional<FixedAssetLine> computeInitialPlannedFixedAssetLine(FixedAsset fixedAsset)
      throws AxelorException {
    prepareRecomputation(fixedAsset);
    LocalDate firstDepreciationDate;
    firstDepreciationDate = computeStartDepreciationDate(fixedAsset);
    BigDecimal depreciationBase = computeInitialDepreciationBase(fixedAsset);
    BigDecimal depreciation = computeInitialDepreciation(fixedAsset, depreciationBase);
    BigDecimal accountingValue = depreciationBase.subtract(depreciation);

    return Optional.ofNullable(
        createPlannedFixedAssetLine(
            fixedAsset,
            firstDepreciationDate,
            depreciation,
            depreciation.add(this.firstPlannedFixedAssetLine.getCumulativeDepreciation()),
            accountingValue,
            depreciationBase,
            getTypeSelect()));
  }

  @Override
  protected LocalDate computeStartDepreciationDate(FixedAsset fixedAsset) {
    return analyticFixedAssetService.computeFirstDepreciationDate(
        fixedAsset,
        DateTool.plusMonths(
            firstPlannedFixedAssetLine.getDepreciationDate(), fixedAsset.getPeriodicityInMonth()));
  }

  @Override
  protected BigDecimal computeInitialDepreciationBase(FixedAsset fixedAsset) {
    return fixedAsset.getCorrectedAccountingValue();
  }

  @Override
  protected List<FixedAssetLine> getFixedAssetLineList(FixedAsset fixedAsset) {
    return fixedAsset.getFixedAssetLineList();
  }

  @Override
  protected Integer getNumberOfDepreciation(FixedAsset fixedAsset) {
    return fixedAsset.getNumberOfDepreciation() - this.listSizeAfterClear;
  }

  @Override
  protected String getComputationMethodSelect(FixedAsset fixedAsset) {
    return fixedAsset.getComputationMethodSelect();
  }

  @Override
  protected BigDecimal getDegressiveCoef(FixedAsset fixedAsset) {
    return fixedAsset.getDegressiveCoef();
  }

  @Override
  protected LocalDate computeProrataTemporisFirstDepreciationDate(FixedAsset fixedAsset) {
    return analyticFixedAssetService.computeFirstDepreciationDate(
        fixedAsset,
        DateTool.plusMonths(
            firstPlannedFixedAssetLine.getDepreciationDate(), fixedAsset.getPeriodicityInMonth()));
  }

  @Override
  protected LocalDate computeProrataTemporisAcquisitionDate(FixedAsset fixedAsset) {
    return analyticFixedAssetService.computeFirstDepreciationDate(
        fixedAsset,
        DateTool.plusMonths(
            firstPlannedFixedAssetLine.getDepreciationDate(), fixedAsset.getPeriodicityInMonth()));
  }

  @Override
  protected Integer getPeriodicityInMonth(FixedAsset fixedAsset) {
    return fixedAsset.getPeriodicityInMonth();
  }

  @Override
  protected Integer getTypeSelect() {
    return FixedAssetLineRepository.TYPE_SELECT_ECONOMIC;
  }

  @Override
  protected int numberOfDepreciationDone(FixedAsset fixedAsset) {
    return getFixedAssetLineList(fixedAsset).size()
        - listSizeAfterClear
        + fixedAsset.getNbrOfPastDepreciations();
  }

  @Override
  protected Boolean isProrataTemporis(FixedAsset fixedAsset) {
    return false;
  }

  private void prepareRecomputation(FixedAsset fixedAsset) throws AxelorException {
    BigDecimal correctedAccountingValue = fixedAsset.getCorrectedAccountingValue();
    if (fixedAsset.getCorrectedAccountingValue() == null
        || fixedAsset.getCorrectedAccountingValue().signum() <= 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          "You can not call this implementation without a corrected accounting value");
    }
    List<FixedAssetLine> fixedAssetLineList = fixedAsset.getFixedAssetLineList();
    Optional<FixedAssetLine> optFixedAssetLine =
        fixedAssetLineService.findOldestFixedAssetLine(
            fixedAssetLineList, FixedAssetLineRepository.STATUS_PLANNED, 0);

    if (!optFixedAssetLine.isPresent()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          "There is no planned fixed asset line to call this function");
    }

    // We can proceed the next part.
    FixedAssetLine firstPlannedFixedAssetLine = optFixedAssetLine.get();
    clearPlannedFixedAssetLineListExcept(fixedAssetLineList, firstPlannedFixedAssetLine);
    this.listSizeAfterClear = fixedAssetLineList.size();
    if (fixedAsset.getNumberOfDepreciation() <= this.listSizeAfterClear) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          "You can not declare a number of depreciation smaller or equal than number of realized lines + 1 (The first planned line)");
    }
    recomputeFirstPlannedLine(
        fixedAssetLineList, firstPlannedFixedAssetLine, correctedAccountingValue);
    this.firstPlannedFixedAssetLine = firstPlannedFixedAssetLine;
    this.canGenerateLines = true;
  }

  private void clearPlannedFixedAssetLineListExcept(
      List<FixedAssetLine> fixedAssetLineList, FixedAssetLine firstPlannedFixedAssetLine) {
    // We remove all fixedAssetLine that are not realized but we keep first planned line.
    List<FixedAssetLine> linesToRemove =
        fixedAssetLineList.stream()
            .filter(
                fixedAssetLine ->
                    fixedAssetLine.getStatusSelect() == FixedAssetLineRepository.STATUS_PLANNED
                        && !fixedAssetLine.equals(firstPlannedFixedAssetLine))
            .collect(Collectors.toList());

    fixedAssetLineList.removeIf(
        fixedAssetLine ->
            fixedAssetLine.getStatusSelect() == FixedAssetLineRepository.STATUS_PLANNED
                && !fixedAssetLine.equals(firstPlannedFixedAssetLine));
    fixedAssetLineService.clear(linesToRemove);
  }

  private void recomputeFirstPlannedLine(
      List<FixedAssetLine> fixedAssetLineList,
      FixedAssetLine firstPlannedFixedAssetLine,
      BigDecimal correctedAccountingValue) {
    firstPlannedFixedAssetLine.setCorrectedAccountingValue(correctedAccountingValue);
    firstPlannedFixedAssetLine.setImpairmentValue(
        firstPlannedFixedAssetLine
            .getAccountingValue()
            .subtract(firstPlannedFixedAssetLine.getCorrectedAccountingValue()));
    Optional<FixedAssetLine> previousLastRealizedFAL =
        fixedAssetLineService.findNewestFixedAssetLine(
            fixedAssetLineList, FixedAssetLineRepository.STATUS_REALIZED, 0);
    if (previousLastRealizedFAL.isPresent()) {
      firstPlannedFixedAssetLine.setCumulativeDepreciation(
          previousLastRealizedFAL
              .get()
              .getCumulativeDepreciation()
              .add(firstPlannedFixedAssetLine.getDepreciation())
              .add(firstPlannedFixedAssetLine.getImpairmentValue().abs()));
    } else {
      firstPlannedFixedAssetLine.setCumulativeDepreciation(
          BigDecimal.ZERO
              .add(firstPlannedFixedAssetLine.getDepreciation())
              .add(firstPlannedFixedAssetLine.getImpairmentValue().abs()));
    }
  }

  @Override
  protected BigDecimal getAlreadyDepreciatedAmount(FixedAsset fixedAsset) {
    return BigDecimal.ZERO;
  }

  @Override
  protected Integer getNumberOfPastDepreciation(FixedAsset fixedAsset) {
    return fixedAsset.getNbrOfPastDepreciations();
  }
}
