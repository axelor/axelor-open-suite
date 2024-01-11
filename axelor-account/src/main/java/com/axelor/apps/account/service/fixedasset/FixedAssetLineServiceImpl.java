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
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.YearService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedAssetLineServiceImpl implements FixedAssetLineService {

  protected FixedAssetLineRepository fixedAssetLineRepository;
  protected FixedAssetDerogatoryLineService fixedAssetDerogatoryLineService;
  protected YearService yearService;
  protected PeriodService periodService;

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public FixedAssetLineServiceImpl(
      FixedAssetLineRepository fixedAssetLineRepository,
      FixedAssetDerogatoryLineService fixedAssetDerogatoryLineService,
      YearService yearService,
      PeriodService periodService) {
    this.fixedAssetLineRepository = fixedAssetLineRepository;
    this.fixedAssetDerogatoryLineService = fixedAssetDerogatoryLineService;
    this.yearService = yearService;
    this.periodService = periodService;
  }

  @Override
  public FixedAssetLine generateProrataDepreciationLine(
      FixedAsset fixedAsset, LocalDate disposalDate) throws AxelorException {
    LocalDate firstServiceDate =
        fixedAsset.getFirstServiceDate() == null
            ? fixedAsset.getAcquisitionDate()
            : fixedAsset.getFirstServiceDate();
    FixedAssetLine firstPlannedLine =
        findOldestFixedAssetLine(
                fixedAsset.getFixedAssetLineList(), FixedAssetLineRepository.STATUS_PLANNED, 0)
            .orElse(null);

    if (firstPlannedLine == null) {
      return null;
    }

    LocalDate nextPlannedDate = firstPlannedLine.getDepreciationDate();

    FixedAssetLine lastRealizedLine =
        findNewestFixedAssetLine(
                fixedAsset.getFixedAssetLineList(), FixedAssetLineRepository.STATUS_REALIZED, 0)
            .orElse(null);
    LocalDate previousRealizedDate =
        lastRealizedLine != null ? lastRealizedLine.getDepreciationDate() : firstServiceDate;

    if (!nextPlannedDate.equals(disposalDate)) {
      if (ChronoUnit.DAYS.between(firstServiceDate, nextPlannedDate) >= 360) {
        nextPlannedDate = null;
      }

      BigDecimal prorataTemporis =
          Beans.get(FixedAssetLineEconomicComputationServiceImpl.class)
              .computeProrataBetween(
                  fixedAsset, previousRealizedDate, disposalDate.minusDays(1), nextPlannedDate);
      BigDecimal deprecationValue =
          computeDepreciationValue(fixedAsset, prorataTemporis, firstPlannedLine);

      firstPlannedLine.setDepreciation(deprecationValue);
      BigDecimal cumulativeValue =
          lastRealizedLine != null
              ? lastRealizedLine.getCumulativeDepreciation().add(deprecationValue)
              : deprecationValue;
      firstPlannedLine.setCumulativeDepreciation(cumulativeValue);
      firstPlannedLine.setAccountingValue(
          fixedAsset.getGrossValue().subtract(firstPlannedLine.getCumulativeDepreciation()));
      firstPlannedLine.setDepreciationDate(disposalDate);
    }
    return firstPlannedLine;
  }

  protected BigDecimal computeDepreciationValue(
      FixedAsset fixedAsset, BigDecimal prorataTemporis, FixedAssetLine firstPlannedLine) {
    BigDecimal deprecationValue;
    if (FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE.equals(
        fixedAsset.getComputationMethodSelect())) {
      deprecationValue =
          fixedAsset
              .getGrossValue()
              .divide(BigDecimal.valueOf(fixedAsset.getNumberOfDepreciation()))
              .multiply(prorataTemporis)
              .setScale(FixedAssetServiceImpl.RETURNED_SCALE, RoundingMode.HALF_UP);
    } else {
      deprecationValue =
          firstPlannedLine
              .getDepreciation()
              .multiply(prorataTemporis)
              .setScale(FixedAssetServiceImpl.RETURNED_SCALE, RoundingMode.HALF_UP);
    }
    return deprecationValue;
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
    fixedAssetDerogatoryLineService.copyFixedAssetDerogatoryLineList(fixedAsset, newFixedAsset);
  }

  @Override
  public Optional<FixedAssetLine> findOldestFixedAssetLine(
      List<FixedAssetLine> fixedAssetLineList, int status, int nbLineToSkip) {
    return findFixedAssetLine(
        fixedAssetLineList,
        status,
        nbLineToSkip,
        Comparator.comparing(FixedAssetLine::getDepreciationDate));
  }

  @Override
  public Optional<FixedAssetLine> findNewestFixedAssetLine(
      List<FixedAssetLine> fixedAssetLineList, int status, int nbLineToSkip) {

    return findFixedAssetLine(
        fixedAssetLineList,
        status,
        nbLineToSkip,
        Comparator.comparing(FixedAssetLine::getDepreciationDate).reversed());
  }

  protected Optional<FixedAssetLine> findFixedAssetLine(
      List<FixedAssetLine> fixedAssetLineList,
      int status,
      int nbLineToSkip,
      Comparator comparator) {
    if (CollectionUtils.isEmpty(fixedAssetLineList)) {
      return Optional.empty();
    }
    fixedAssetLineList.sort(comparator);

    return fixedAssetLineList.stream()
        .filter(fixedAssetLine -> fixedAssetLine.getStatusSelect() == status)
        .skip(nbLineToSkip)
        .findFirst();
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
}
