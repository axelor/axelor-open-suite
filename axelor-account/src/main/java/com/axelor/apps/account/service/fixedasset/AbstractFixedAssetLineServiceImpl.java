/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.YearService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class of FixedAssetLineComputationService. This class is not supposed to be directly
 * used. Please use {@link FixedAssetLineEconomicServiceImpl} or {@link
 * FixedAssetLineFiscalServiceImpl}.
 */
public abstract class AbstractFixedAssetLineServiceImpl implements FixedAssetLineService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected FixedAssetLineRepository fixedAssetLineRepository;
  protected YearService yearService;
  protected PeriodService periodService;

  protected abstract int getPeriodicityTypeSelect(FixedAsset fixedAsset);

  protected abstract int getPeriodicityInMonth(FixedAsset fixedAsset);

  protected abstract List<FixedAssetLine> getFixedAssetLineList(FixedAsset fixedAsset);

  protected abstract BigDecimal computeProrataBetween(
      FixedAsset fixedAsset,
      LocalDate previousRealizedDate,
      LocalDate disposalDate,
      LocalDate nextPlannedDate);

  protected abstract int getTypeSelect();

  @Inject
  public AbstractFixedAssetLineServiceImpl(
      FixedAssetLineRepository fixedAssetLineRepository,
      YearService yearService,
      PeriodService periodService) {
    this.fixedAssetLineRepository = fixedAssetLineRepository;
    this.yearService = yearService;
    this.periodService = periodService;
  }

  @Override
  public FixedAssetLine generateProrataDepreciationLine(
      FixedAsset fixedAsset,
      LocalDate disposalDate,
      FixedAssetLine previousRealizedLine,
      FixedAssetLine previousPlannedLine)
      throws AxelorException {
    FixedAssetLine fixedAssetLine = getLineFromDate(fixedAsset, disposalDate);
    if (fixedAssetLine == null) {
      fixedAssetLine = new FixedAssetLine();
      fixedAssetLine.setDepreciationDate(disposalDate);
      fixedAssetLine.setTypeSelect(getTypeSelect());
      fixedAssetLine.setStatusSelect(FixedAssetRepository.STATUS_DRAFT);
      fixedAssetLine.setDepreciationBase(
          previousRealizedLine != null
              ? previousRealizedLine.getDepreciationBase()
              : fixedAsset.getGrossValue());
      fixedAsset.addFixedAssetLineListItem(fixedAssetLine);
    }
    computeDepreciationWithProrata(
        fixedAsset, fixedAssetLine, previousRealizedLine, previousPlannedLine, disposalDate);
    return fixedAssetLine;
  }

  @Override
  public void computeDepreciationWithProrata(
      FixedAsset fixedAsset,
      FixedAssetLine fixedAssetLine,
      FixedAssetLine previousRealizedLine,
      FixedAssetLine nextPlannedLine,
      LocalDate disposalDate) {
    BigDecimal deprecationValue;
    LocalDate firstServiceDate =
        fixedAsset.getFirstServiceDate() == null
            ? fixedAsset.getAcquisitionDate()
            : fixedAsset.getFirstServiceDate();
    LocalDate nextPlannedDate =
        nextPlannedLine != null ? nextPlannedLine.getDepreciationDate() : null;
    if (nextPlannedDate != null && nextPlannedDate.equals(disposalDate)) {
      deprecationValue = nextPlannedLine.getDepreciation();
    } else {
      LocalDate previousRealizedDate =
          previousRealizedLine != null
              ? previousRealizedLine.getDepreciationDate()
              : firstServiceDate;
      if (nextPlannedDate != null
          && ChronoUnit.DAYS.between(firstServiceDate, nextPlannedDate) >= 360) {
        nextPlannedDate = null;
      }
      BigDecimal prorataTemporis =
          computeProrataBetween(
              fixedAsset, previousRealizedDate, disposalDate.minusDays(1), nextPlannedDate);
      deprecationValue =
          fixedAssetLine
              .getDepreciation()
              .multiply(prorataTemporis)
              .setScale(FixedAssetServiceImpl.RETURNED_SCALE, RoundingMode.HALF_UP);
    }
    fixedAssetLine.setDepreciation(deprecationValue);
    BigDecimal cumulativeValue =
        previousRealizedLine != null
            ? previousRealizedLine.getCumulativeDepreciation().add(deprecationValue)
            : deprecationValue;
    fixedAssetLine.setCumulativeDepreciation(cumulativeValue);
    fixedAssetLine.setAccountingValue(
        fixedAsset.getGrossValue().subtract(fixedAssetLine.getCumulativeDepreciation()));
    fixedAssetLine.setDepreciationDate(disposalDate);
  }

  @Transactional
  @Override
  public void copyFixedAssetLineList(FixedAsset fixedAsset, FixedAsset newFixedAsset) {
    if (newFixedAsset.getFixedAssetLineList() == null) {
      if (fixedAsset.getFixedAssetLineList() != null) {
        fixedAsset
            .getFixedAssetLineList()
            .forEach(
                line -> {
                  FixedAssetLine copy = fixedAssetLineRepository.copy(line, false);
                  newFixedAsset.addFixedAssetLineListItem(fixedAssetLineRepository.save(copy));
                });
      }
    }
    if (newFixedAsset.getFiscalFixedAssetLineList() == null) {
      if (fixedAsset.getFiscalFixedAssetLineList() != null) {
        fixedAsset
            .getFiscalFixedAssetLineList()
            .forEach(
                line -> {
                  FixedAssetLine copy = fixedAssetLineRepository.copy(line, false);
                  newFixedAsset.addFiscalFixedAssetLineListItem(
                      fixedAssetLineRepository.save(copy));
                });
      }
    }
    if (newFixedAsset.getIfrsFixedAssetLineList() == null) {
      if (fixedAsset.getIfrsFixedAssetLineList() != null) {
        fixedAsset
            .getIfrsFixedAssetLineList()
            .forEach(
                line -> {
                  FixedAssetLine copy = fixedAssetLineRepository.copy(line, false);
                  newFixedAsset.addIfrsFixedAssetLineListItem(fixedAssetLineRepository.save(copy));
                });
      }
    }
  }

  @Override
  public Optional<FixedAssetLine> findOldestFixedAssetLine(
      FixedAsset fixedAsset, int status, int nbLineToSkip) {
    List<FixedAssetLine> fixedAssetLineList = getFixedAssetLineList(fixedAsset);
    if (fixedAssetLineList == null || fixedAssetLineList.isEmpty()) {
      return Optional.empty();
    }
    fixedAssetLineList.sort(
        (fa1, fa2) -> fa1.getDepreciationDate().compareTo(fa2.getDepreciationDate()));
    return fixedAssetLineList.stream()
        .filter(fixedAssetLine -> fixedAssetLine.getStatusSelect() == status)
        .findFirst();
  }

  @Override
  public Optional<FixedAssetLine> findNewestFixedAssetLine(
      List<FixedAssetLine> fixedAssetLineList, int status, int nbLineToSkip) {
    if (fixedAssetLineList == null || fixedAssetLineList.isEmpty()) {
      return Optional.empty();
    }
    fixedAssetLineList.sort(
        (fa1, fa2) -> fa2.getDepreciationDate().compareTo(fa1.getDepreciationDate()));
    Optional<FixedAssetLine> optFixedAssetLine =
        fixedAssetLineList.stream()
            .filter(fixedAssetLine -> fixedAssetLine.getStatusSelect() == status)
            .skip(nbLineToSkip)
            .findFirst();
    return optFixedAssetLine;
  }

  /**
   * {@inheritDoc}
   *
   * @throws NullPointerException if fixedAssetDerogatoryLineList is null
   */
  @Override
  public void clear(List<FixedAssetLine> fixedAssetLineList) {
    Objects.requireNonNull(fixedAssetLineList);
    fixedAssetLineList.forEach(
        line -> {
          remove(line);
        });
    fixedAssetLineList.clear();
  }

  @Override
  @Transactional
  public void remove(FixedAssetLine line) {
    Objects.requireNonNull(line);
    fixedAssetLineRepository.remove(line);
  }

  @Override
  public void filterListByStatus(List<FixedAssetLine> fixedAssetLineList, int status) {

    List<FixedAssetLine> linesToRemove = new ArrayList<>();
    if (fixedAssetLineList != null) {
      fixedAssetLineList.stream()
          .filter(line -> line.getStatusSelect() == status)
          .forEach(line -> linesToRemove.add(line));
      fixedAssetLineList.removeIf(line -> line.getStatusSelect() == status);
    }
    clear(linesToRemove);
  }

  @Override
  public FixedAssetLine computeCessionLine(FixedAsset fixedAsset, LocalDate disposalDate)
      throws AxelorException {
    FixedAssetLine correspondingFixedAssetLine;
    correspondingFixedAssetLine = getLineFromDate(fixedAsset, disposalDate);
    if (correspondingFixedAssetLine != null
        && !correspondingFixedAssetLine.getDepreciationDate().equals(disposalDate)) {
      computeLineProrata(fixedAsset, disposalDate, correspondingFixedAssetLine);
    }

    return correspondingFixedAssetLine;
  }

  protected void computeLineProrata(
      FixedAsset fixedAsset, LocalDate disposalDate, FixedAssetLine correspondingFixedAssetLine)
      throws AxelorException {
    FixedAssetLine previousRealizedLine =
        findNewestFixedAssetLine(
                getFixedAssetLineList(fixedAsset), FixedAssetLineRepository.STATUS_REALIZED, 0)
            .orElse(null);
    if (previousRealizedLine != null
        && disposalDate.isBefore(previousRealizedLine.getDepreciationDate())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.FIXED_ASSET_DISPOSAL_DATE_ERROR_1));
    }
    FixedAssetLine previousPlannedLine =
        findNewestFixedAssetLine(
                getFixedAssetLineList(fixedAsset), FixedAssetLineRepository.STATUS_PLANNED, 0)
            .orElse(null);
    if (correspondingFixedAssetLine != null) {
      computeDepreciationWithProrata(
          fixedAsset,
          correspondingFixedAssetLine,
          previousRealizedLine,
          previousPlannedLine,
          disposalDate);
    }
  }

  protected FixedAssetLine getLineFromDate(FixedAsset fixedAsset, LocalDate disposalDate)
      throws AxelorException {
    FixedAssetLine correspondingFixedAssetLine;
    if (getPeriodicityTypeSelect(fixedAsset) == FixedAssetRepository.PERIODICITY_TYPE_YEAR) {
      Year year =
          yearService.getYear(disposalDate, fixedAsset.getCompany(), YearRepository.TYPE_FISCAL);
      if (year == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.PERIOD_1),
            fixedAsset.getCompany().getName(),
            disposalDate);
      }
      correspondingFixedAssetLine =
          getFirstLineWithinInterval(
              getFixedAssetLineList(fixedAsset),
              FixedAssetLineRepository.STATUS_PLANNED,
              year.getFromDate(),
              year.getToDate());

    } else {

      if (getPeriodicityInMonth(fixedAsset) > 1) {
        correspondingFixedAssetLine =
            getFirstLineWithinInterval(
                getFixedAssetLineList(fixedAsset),
                FixedAssetLineRepository.STATUS_PLANNED,
                disposalDate.minusMonths(getPeriodicityInMonth(fixedAsset)),
                disposalDate.plusMonths(getPeriodicityInMonth(fixedAsset)));
      } else {
        correspondingFixedAssetLine =
            getExistingLineWithSameMonth(
                getFixedAssetLineList(fixedAsset),
                disposalDate,
                FixedAssetLineRepository.STATUS_PLANNED);
      }
    }
    return correspondingFixedAssetLine;
  }

  protected FixedAssetLine getFirstLineWithinInterval(
      List<FixedAssetLine> fixedAssetLineList,
      int lineStatus,
      LocalDate fromDate,
      LocalDate toDate) {

    if (fixedAssetLineList != null) {
      return fixedAssetLineList.stream()
          .filter(
              line ->
                  (line.getDepreciationDate().isAfter(fromDate)
                          || line.getDepreciationDate().equals(fromDate))
                      && (line.getDepreciationDate().isBefore(toDate)
                          || line.getDepreciationDate().equals(toDate))
                      && line.getStatusSelect() == lineStatus)
          .sorted((fa1, fa2) -> fa1.getDepreciationDate().compareTo(fa2.getDepreciationDate()))
          .findFirst()
          .orElse(null);
    }
    return null;
  }

  protected boolean isLastDayOfTheYear(LocalDate disposalDate) {
    return disposalDate.getMonthValue() == 12 && disposalDate.getDayOfMonth() == 31;
  }

  protected FixedAssetLine getExistingLineWithSameMonth(
      List<FixedAssetLine> fixedAssetLineList, LocalDate disposalDate, int lineStatus) {
    if (fixedAssetLineList != null) {
      return fixedAssetLineList.stream()
          .filter(
              line ->
                  line.getDepreciationDate().getMonth() == disposalDate.getMonth()
                      && line.getDepreciationDate().getYear() == disposalDate.getYear()
                      && line.getStatusSelect() == lineStatus)
          .findAny()
          .orElse(null);
    }
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @throws AxelorException
   * @throws NullPointerException if fixedAssetLine is null.
   */
  @Override
  public FixedAsset getFixedAsset(FixedAssetLine fixedAssetLine) throws AxelorException {
    Objects.requireNonNull(fixedAssetLine);
    switch (fixedAssetLine.getTypeSelect()) {
      case FixedAssetLineRepository.TYPE_SELECT_ECONOMIC:
        return fixedAssetLine.getFixedAsset();
      case FixedAssetLineRepository.TYPE_SELECT_FISCAL:
        return fixedAssetLine.getFiscalFixedAsset();
      case FixedAssetLineRepository.TYPE_SELECT_IFRS:
        return fixedAssetLine.getIfrsFixedAsset();
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            "Fixed asset line type is not recognized to get fixed asset");
    }
  }

  @Override
  public void setFixedAsset(FixedAsset fixedAsset, FixedAssetLine fixedAssetLine)
      throws AxelorException {
    Objects.requireNonNull(fixedAssetLine);
    switch (fixedAssetLine.getTypeSelect()) {
      case FixedAssetLineRepository.TYPE_SELECT_ECONOMIC:
        fixedAssetLine.setFixedAsset(fixedAsset);
        break;
      case FixedAssetLineRepository.TYPE_SELECT_FISCAL:
        fixedAssetLine.setFiscalFixedAsset(fixedAsset);
        break;
      case FixedAssetLineRepository.TYPE_SELECT_IFRS:
        fixedAssetLine.setIfrsFixedAsset(fixedAsset);
        break;
      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            "Fixed asset line type is not recognized to set fixed asset");
    }
  }

  @Override
  public Map<Integer, List<FixedAssetLine>> getFixedAssetLineListByStatus(FixedAsset fixedAsset) {
    return getFixedAssetLineList(fixedAsset).stream()
        .collect(Collectors.groupingBy(FixedAssetLine::getStatusSelect));
  }
}
