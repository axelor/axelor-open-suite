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

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.utils.date.DateTool;
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
  protected FixedAssetDateService fixedAssetDateService;
  private boolean canGenerateLines = false;
  private FixedAssetLine firstPlannedFixedAssetLine;
  private int listSizeAfterClear;

  @Inject
  public FixedAssetLineEconomicUpdateComputationServiceImpl(
      FixedAssetFailOverControlService fixedAssetFailOverControlService,
      FixedAssetLineService fixedAssetLineService,
      FixedAssetDateService fixedAssetDateService,
      AppBaseService appBaseService) {
    super(fixedAssetFailOverControlService, appBaseService);
    this.fixedAssetLineService = fixedAssetLineService;
    this.fixedAssetDateService = fixedAssetDateService;
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
        createFixedAssetLine(
            fixedAsset,
            firstDepreciationDate,
            depreciation,
            depreciation.add(this.firstPlannedFixedAssetLine.getCumulativeDepreciation()),
            accountingValue,
            depreciationBase,
            getTypeSelect(),
            FixedAssetLineRepository.STATUS_PLANNED));
  }

  @Override
  protected LocalDate computeStartDepreciationDate(FixedAsset fixedAsset) {
    return fixedAssetDateService.computeLastDayOfPeriodicity(
        fixedAsset.getPeriodicityTypeSelect(),
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
  protected BigDecimal getNumberOfDepreciation(FixedAsset fixedAsset) {
    return BigDecimal.valueOf(fixedAsset.getNumberOfDepreciation())
        .subtract(BigDecimal.valueOf(this.listSizeAfterClear));
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
    return fixedAssetDateService.computeLastDayOfPeriodicity(
        fixedAsset.getPeriodicityTypeSelect(),
        DateTool.plusMonths(
            firstPlannedFixedAssetLine.getDepreciationDate(), fixedAsset.getPeriodicityInMonth()));
  }

  @Override
  protected LocalDate computeProrataTemporisAcquisitionDate(FixedAsset fixedAsset) {
    return fixedAssetDateService.computeLastDayOfPeriodicity(
        fixedAsset.getPeriodicityTypeSelect(),
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
  protected BigDecimal numberOfDepreciationDone(FixedAsset fixedAsset) {
    return BigDecimal.valueOf(
        getFixedAssetLineList(fixedAsset).size()
            - listSizeAfterClear
            + fixedAsset.getNbrOfPastDepreciations());
  }

  @Override
  protected Boolean isProrataTemporis(FixedAsset fixedAsset) {
    return false;
  }

  protected void prepareRecomputation(FixedAsset fixedAsset) throws AxelorException {
    BigDecimal correctedAccountingValue = fixedAsset.getCorrectedAccountingValue();
    if (fixedAsset.getCorrectedAccountingValue() == null
        || fixedAsset.getCorrectedAccountingValue().signum() == 0) {
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

  protected void clearPlannedFixedAssetLineListExcept(
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

  protected void recomputeFirstPlannedLine(
      List<FixedAssetLine> fixedAssetLineList,
      FixedAssetLine firstPlannedFixedAssetLine,
      BigDecimal correctedAccountingValue) {
    firstPlannedFixedAssetLine.setCorrectedAccountingValue(correctedAccountingValue);
    BigDecimal impairmentValue =
        firstPlannedFixedAssetLine
            .getAccountingValue()
            .subtract(firstPlannedFixedAssetLine.getCorrectedAccountingValue());
    firstPlannedFixedAssetLine.setImpairmentValue(impairmentValue);
    BigDecimal depreciation = firstPlannedFixedAssetLine.getDepreciation();
    Optional<FixedAssetLine> previousLastRealizedFAL =
        fixedAssetLineService.findNewestFixedAssetLine(
            fixedAssetLineList, FixedAssetLineRepository.STATUS_REALIZED, 0);
    if (previousLastRealizedFAL.isPresent()) {
      firstPlannedFixedAssetLine.setCumulativeDepreciation(
          previousLastRealizedFAL
              .get()
              .getCumulativeDepreciation()
              .add(depreciation)
              .add(impairmentValue));
    } else {
      firstPlannedFixedAssetLine.setCumulativeDepreciation(
          BigDecimal.ZERO.add(depreciation).add(impairmentValue));
    }
  }

  @Override
  protected BigDecimal getAlreadyDepreciatedAmount(FixedAsset fixedAsset) {
    return BigDecimal.ZERO;
  }

  @Override
  protected BigDecimal getNumberOfPastDepreciation(FixedAsset fixedAsset) {
    return BigDecimal.valueOf(fixedAsset.getNbrOfPastDepreciations());
  }

  @Override
  protected Integer getDurationInMonth(FixedAsset fixedAsset) {

    return fixedAsset.getDurationInMonth();
  }

  @Override
  protected BigDecimal getDepreciatedAmountCurrentYear(FixedAsset fixedAsset) {
    return fixedAsset.getDepreciatedAmountCurrentYear();
  }

  @Override
  protected LocalDate getFailOverDepreciationEndDate(FixedAsset fixedAsset) {
    return fixedAsset.getFailOverDepreciationEndDate();
  }

  @Override
  protected int getFirstDateDepreciationInitSelect(FixedAsset fixedAsset) {

    return fixedAsset.getFirstDepreciationDateInitSelect();
  }
}
