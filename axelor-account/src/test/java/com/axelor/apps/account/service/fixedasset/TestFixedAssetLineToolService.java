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
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TestFixedAssetLineToolService {

  private static FixedAssetLineToolService fixedAssetLineToolService;

  @BeforeAll
  static void prepare() {
    fixedAssetLineToolService = new FixedAssetLineToolServiceImpl();
  }

  @Test
  void groupAndSortByDateFixedAssetLineEmpty() {
    FixedAsset fixedAsset = createFixedAsset(12, new ArrayList<>(), new ArrayList<>());
    Assertions.assertTrue(
        fixedAssetLineToolService.groupAndSortByDateFixedAssetLine(fixedAsset).isEmpty());
  }

  @Test
  void groupAndSortByDateFixedAssetLineSimpleCasePeriodicityMonth() {
    List<LocalDate> fiscalDateList =
        Lists.newArrayList(
            LocalDate.of(2022, 3, 31),
            LocalDate.of(2022, 4, 30),
            LocalDate.of(2022, 5, 31),
            LocalDate.of(2022, 6, 30));
    List<LocalDate> dateList =
        Lists.newArrayList(
            LocalDate.of(2022, 3, 31),
            LocalDate.of(2022, 4, 30),
            LocalDate.of(2022, 5, 31),
            LocalDate.of(2022, 6, 30));
    FixedAsset fixedAsset = createFixedAsset(1, fiscalDateList, dateList);
    Set<LocalDate> localDateResults =
        fixedAssetLineToolService.groupAndSortByDateFixedAssetLine(fixedAsset).keySet();
    Set<LocalDate> expectedSet =
        Sets.newHashSet(
            LocalDate.of(2022, 3, 31),
            LocalDate.of(2022, 4, 30),
            LocalDate.of(2022, 5, 31),
            LocalDate.of(2022, 6, 30));
    Assertions.assertEquals(expectedSet, localDateResults);
  }

  @Test
  void groupAndSortByDateFixedAssetLineRealCasePeriodicityYear() {
    List<LocalDate> fiscalDateList =
        Lists.newArrayList(
            LocalDate.of(2023, 1, 31),
            LocalDate.of(2024, 1, 31),
            LocalDate.of(2025, 1, 31),
            LocalDate.of(2026, 1, 31),
            LocalDate.of(2026, 9, 20));
    List<LocalDate> dateList =
        Lists.newArrayList(
            LocalDate.of(2023, 1, 31),
            LocalDate.of(2024, 1, 31),
            LocalDate.of(2025, 1, 31),
            LocalDate.of(2025, 9, 20));
    FixedAsset fixedAsset = createFixedAsset(12, fiscalDateList, dateList);
    Set<LocalDate> localDateResults =
        fixedAssetLineToolService.groupAndSortByDateFixedAssetLine(fixedAsset).keySet();
    Set<LocalDate> expectedSet =
        Sets.newHashSet(
            LocalDate.of(2023, 1, 31),
            LocalDate.of(2024, 1, 31),
            LocalDate.of(2025, 1, 31),
            LocalDate.of(2026, 1, 31),
            LocalDate.of(2026, 9, 20));
    Assertions.assertEquals(expectedSet, localDateResults);
  }

  protected FixedAsset createFixedAsset(
      int periodicity, List<LocalDate> fiscalLocalDateList, List<LocalDate> localDateList) {
    FixedAsset fixedAsset = new FixedAsset();
    fixedAsset.setPeriodicityInMonth(periodicity);
    fixedAsset.setFiscalFixedAssetLineList(createFixedAssetLineList(fiscalLocalDateList));
    fixedAsset.setFixedAssetLineList(createFixedAssetLineList(localDateList));
    return fixedAsset;
  }

  protected List<FixedAssetLine> createFixedAssetLineList(List<LocalDate> localDateList) {
    return localDateList.stream().map(this::createFixedAssetLine).collect(Collectors.toList());
  }

  protected FixedAssetLine createFixedAssetLine(LocalDate localDate) {
    FixedAssetLine fixedAssetLine = new FixedAssetLine();
    fixedAssetLine.setDepreciationDate(localDate);
    fixedAssetLine.setStatusSelect(FixedAssetLineRepository.STATUS_PLANNED);
    return fixedAssetLine;
  }
}
