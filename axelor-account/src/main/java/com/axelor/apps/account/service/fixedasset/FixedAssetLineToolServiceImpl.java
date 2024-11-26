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
import com.axelor.apps.account.service.FindFixedAssetService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.interfaces.ArithmeticOperation;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.utils.helpers.date.LocalDateHelper;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FixedAssetLineToolServiceImpl implements FixedAssetLineToolService {

  protected CurrencyScaleService currencyScaleService;
  protected FindFixedAssetService findFixedAssetService;
  protected static final int CURRENCY_MAX_SCALE = 3;

  @Inject
  public FixedAssetLineToolServiceImpl(
      CurrencyScaleService currencyScaleService, FindFixedAssetService findFixedAssetService) {
    this.currencyScaleService = currencyScaleService;
    this.findFixedAssetService = findFixedAssetService;
  }

  @Override
  public LinkedHashMap<LocalDate, List<FixedAssetLine>> groupAndSortByDateFixedAssetLine(
      FixedAsset fixedAsset) {
    Objects.requireNonNull(fixedAsset);

    // Preparation of data needed for computation
    List<FixedAssetLine> allFixedAssetLineList = new ArrayList<>();
    // This method will only compute line that are not realized.
    allFixedAssetLineList.addAll(
        fixedAsset.getFiscalFixedAssetLineList().stream()
            .filter(line -> line.getStatusSelect() == FixedAssetLineRepository.STATUS_PLANNED)
            .collect(Collectors.toList()));
    allFixedAssetLineList.addAll(
        fixedAsset.getFixedAssetLineList().stream()
            .filter(line -> line.getStatusSelect() == FixedAssetLineRepository.STATUS_PLANNED)
            .collect(Collectors.toList()));

    allFixedAssetLineList.sort(Comparator.comparing(FixedAssetLine::getDepreciationDate));

    return groupByPeriodicityInMonth(allFixedAssetLineList, fixedAsset.getPeriodicityInMonth());
  }

  protected LinkedHashMap<LocalDate, List<FixedAssetLine>> groupByPeriodicityInMonth(
      List<FixedAssetLine> fixedAssetLineList, int periodicityInMonth) {
    LinkedHashMap<LocalDate, List<FixedAssetLine>> returnedHashMap = new LinkedHashMap<>();
    if (fixedAssetLineList.isEmpty()) {
      return returnedHashMap;
    }
    // depreciation date is required and fixed asset line list is not empty, so we can get()
    LocalDate startDate =
        fixedAssetLineList.stream().map(FixedAssetLine::getDepreciationDate).findFirst().get();
    LocalDate endDate = startDate;

    while (!fixedAssetLineList.isEmpty()) {
      LocalDate currentStartDate = startDate;
      LocalDate currentEndDate = endDate;
      List<FixedAssetLine> subFixedAssetLineList =
          fixedAssetLineList.stream()
              .filter(
                  fixedAssetLine ->
                      LocalDateHelper.isBetween(
                          currentStartDate, currentEndDate, fixedAssetLine.getDepreciationDate()))
              .collect(Collectors.toList());

      if (!subFixedAssetLineList.isEmpty()) {
        fixedAssetLineList.removeAll(subFixedAssetLineList);
        // depreciation date is required and sub fixed asset line list is not empty, so we can get()
        LocalDate maxDateInSubList =
            subFixedAssetLineList.stream()
                .map(FixedAssetLine::getDepreciationDate)
                .max(Comparator.naturalOrder())
                .get();
        returnedHashMap.put(maxDateInSubList, subFixedAssetLineList);
      }

      startDate = endDate.plusDays(1);
      endDate = startDate.plusMonths(periodicityInMonth).minusDays(1);
    }
    return returnedHashMap;
  }

  @Override
  public BigDecimal getCompanyScaledValue(
      BigDecimal amount1,
      BigDecimal amount2,
      FixedAsset fixedAsset,
      ArithmeticOperation arithmeticOperation) {
    return amount1 == null
        ? BigDecimal.ZERO
        : currencyScaleService.getCompanyScaledValue(
            fixedAsset, arithmeticOperation.operate(amount1, amount2));
  }

  @Override
  public BigDecimal getCurrenciesMaxScaledValue(
      BigDecimal amount1, BigDecimal amount2, ArithmeticOperation arithmeticOperation) {
    return amount1 == null
        ? BigDecimal.ZERO
        : currencyScaleService.getScaledValue(
            arithmeticOperation.operate(amount1, amount2), CURRENCY_MAX_SCALE);
  }

  @Override
  public BigDecimal getCompanyScaledValue(BigDecimal amount1, FixedAsset fixedAsset) {
    return amount1 == null
        ? BigDecimal.ZERO
        : currencyScaleService.getCompanyScaledValue(fixedAsset, amount1);
  }

  @Override
  public BigDecimal getCompanyDivideScaledValue(
      BigDecimal amount1, BigDecimal amount2, FixedAsset fixedAsset) {
    return amount1 == null || amount2 == null || amount2.equals(BigDecimal.ZERO)
        ? BigDecimal.ZERO
        : amount1.divide(
            amount2, currencyScaleService.getCompanyScale(fixedAsset), RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal getCompanyScaledValue(
      BigDecimal amount1,
      BigDecimal amount2,
      FixedAssetLine fixedAssetLine,
      ArithmeticOperation arithmeticOperation)
      throws AxelorException {
    return amount1 == null
        ? BigDecimal.ZERO
        : this.getCompanyScaledValue(arithmeticOperation.operate(amount1, amount2), fixedAssetLine);
  }

  @Override
  public BigDecimal getCompanyScaledValue(BigDecimal amount1, FixedAssetLine fixedAssetLine)
      throws AxelorException {
    return amount1 == null
        ? BigDecimal.ZERO
        : currencyScaleService.getCompanyScaledValue(
            findFixedAssetService.getFixedAsset(fixedAssetLine), amount1);
  }

  @Override
  public boolean isGreaterThan(BigDecimal amount1, BigDecimal amount2, FixedAsset fixedAsset) {
    return currencyScaleService.isGreaterThan(amount1, amount2, fixedAsset, true);
  }

  @Override
  public boolean equals(BigDecimal amount1, BigDecimal amount2, FixedAsset fixedAsset) {
    return currencyScaleService.equals(amount1, amount2, fixedAsset, true);
  }

  @Override
  public int getCompanyScale(FixedAssetLine fixedAssetLine) throws AxelorException {
    FixedAsset fixedAsset = findFixedAssetService.getFixedAsset(fixedAssetLine);
    return currencyScaleService.getCompanyScale(fixedAsset);
  }
}
