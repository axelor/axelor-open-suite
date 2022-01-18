/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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

import static com.axelor.apps.account.service.fixedasset.FixedAssetTestTool.assertFixedAssetLineEquals;
import static com.axelor.apps.account.service.fixedasset.FixedAssetTestTool.createFixedAsset;
import static com.axelor.apps.account.service.fixedasset.FixedAssetTestTool.createFixedAssetCategoryFromIsProrataTemporis;
import static com.axelor.apps.account.service.fixedasset.FixedAssetTestTool.createFixedAssetLine;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

public class TestFixedAssetLineComputationService {

  protected FixedAssetLineComputationService fixedAssetLineComputationService;

  @Before
  public void prepare() {
    fixedAssetLineComputationService = new FixedAssetLineComputationServiceImpl();
  }

  @Test
  public void testComputeInitialPlannedFixedAssetLineWithoutProrata() {
    FixedAsset fixedAsset =
        createFixedAsset(
            FixedAssetRepository.COMPUTATION_METHOD_LINEAR,
            LocalDate.of(2020, 10, 4),
            LocalDate.of(2020, 12, 31),
            5,
            12,
            createFixedAssetCategoryFromIsProrataTemporis(false),
            new BigDecimal("500.00"));
    FixedAssetLine fixedAssetLine =
        fixedAssetLineComputationService.computeInitialPlannedFixedAssetLine(fixedAsset);

    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2020, 12, 31),
            new BigDecimal("100.00"),
            new BigDecimal("100.00"),
            new BigDecimal("400.00")),
        fixedAssetLine);
  }

  @Test
  public void testComputeInitialPlannedFixedAssetLineWithProrata() {
    FixedAsset fixedAsset =
        createFixedAsset(
            FixedAssetRepository.COMPUTATION_METHOD_LINEAR,
            LocalDate.of(2020, 10, 5),
            LocalDate.of(2020, 12, 31),
            5,
            12,
            createFixedAssetCategoryFromIsProrataTemporis(true),
            new BigDecimal("500.00"));
    FixedAssetLine fixedAssetLine =
        fixedAssetLineComputationService.computeInitialPlannedFixedAssetLine(fixedAsset);

    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2020, 12, 31),
            new BigDecimal("23.89"),
            new BigDecimal("23.89"),
            new BigDecimal("476.11")),
        fixedAssetLine);
  }

  @Test
  public void testComputePlannedFixedAssetLineWithoutProrata() {
    FixedAsset fixedAsset =
        createFixedAsset(
            FixedAssetRepository.COMPUTATION_METHOD_LINEAR,
            LocalDate.of(2020, 10, 4),
            LocalDate.of(2020, 12, 31),
            5,
            12,
            createFixedAssetCategoryFromIsProrataTemporis(false),
            new BigDecimal("500.00"));
    FixedAssetLine firstFixedAssetLine =
        createFixedAssetLine(
            LocalDate.of(2020, 12, 31),
            new BigDecimal("100.00"),
            new BigDecimal("100.00"),
            new BigDecimal("400.00"));
    FixedAssetLine fixedAssetLine =
        fixedAssetLineComputationService.computePlannedFixedAssetLine(
            fixedAsset, firstFixedAssetLine);

    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2021, 12, 31),
            new BigDecimal("100.00"),
            new BigDecimal("200.00"),
            new BigDecimal("300.00")),
        fixedAssetLine);
  }

  @Test
  public void testComputePlannedFixedAssetLineWithProrata() {
    FixedAsset fixedAsset =
        createFixedAsset(
            FixedAssetRepository.COMPUTATION_METHOD_LINEAR,
            LocalDate.of(2020, 10, 4),
            LocalDate.of(2020, 12, 31),
            5,
            12,
            createFixedAssetCategoryFromIsProrataTemporis(false),
            new BigDecimal("500.00"));
    FixedAssetLine firstFixedAssetLine =
        createFixedAssetLine(
            LocalDate.of(2020, 12, 31),
            new BigDecimal("23.89"),
            new BigDecimal("23.89"),
            new BigDecimal("476.11"));
    FixedAssetLine fixedAssetLine =
        fixedAssetLineComputationService.computePlannedFixedAssetLine(
            fixedAsset, firstFixedAssetLine);

    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2021, 12, 31),
            new BigDecimal("100.00"),
            new BigDecimal("123.89"),
            new BigDecimal("376.11")),
        fixedAssetLine);
  }

  @Test
  public void testComputeLastFixedAssetLineWithoutProrata() {
    FixedAsset fixedAsset =
        createFixedAsset(
            FixedAssetRepository.COMPUTATION_METHOD_LINEAR,
            LocalDate.of(2020, 10, 4),
            LocalDate.of(2020, 12, 31),
            5,
            12,
            createFixedAssetCategoryFromIsProrataTemporis(false),
            new BigDecimal("500.00"));
    FixedAssetLine previousFixedAssetLine =
        createFixedAssetLine(
            LocalDate.of(2023, 12, 31),
            new BigDecimal("100.00"),
            new BigDecimal("400.00"),
            new BigDecimal("100.00"));
    FixedAssetLine fixedAssetLine =
        fixedAssetLineComputationService.computePlannedFixedAssetLine(
            fixedAsset, previousFixedAssetLine);

    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2024, 12, 31),
            new BigDecimal("100.00"),
            new BigDecimal("500.00"),
            new BigDecimal("0.00")),
        fixedAssetLine);
  }

  @Test
  public void testComputeLastFixedAssetLineWithProrata() {
    FixedAsset fixedAsset =
        createFixedAsset(
            FixedAssetRepository.COMPUTATION_METHOD_LINEAR,
            LocalDate.of(2020, 10, 4),
            LocalDate.of(2020, 12, 31),
            5,
            12,
            createFixedAssetCategoryFromIsProrataTemporis(true),
            new BigDecimal("500.00"));
    FixedAssetLine previousFixedAssetLine =
        createFixedAssetLine(
            LocalDate.of(2024, 12, 31),
            new BigDecimal("100.00"),
            new BigDecimal("423.89"),
            new BigDecimal("76.11"));
    FixedAssetLine fixedAssetLine =
        fixedAssetLineComputationService.computePlannedFixedAssetLine(
            fixedAsset, previousFixedAssetLine);

    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2025, 10, 3),
            new BigDecimal("76.11"),
            new BigDecimal("500.00"),
            new BigDecimal("0.00")),
        fixedAssetLine);
  }

  @Test
  public void testComputeLastFixedAssetLineWithUsProrata() {
    FixedAsset fixedAsset =
        createFixedAsset(
            FixedAssetRepository.COMPUTATION_METHOD_LINEAR,
            LocalDate.of(2020, 7, 1),
            LocalDate.of(2020, 12, 31),
            7,
            12,
            createFixedAssetCategoryFromIsProrataTemporis(true, true),
            new BigDecimal("102638.35"));
    FixedAssetLine previousFixedAssetLine =
        createFixedAssetLine(
            LocalDate.of(2026, 12, 31),
            new BigDecimal("14662.62"),
            new BigDecimal("95307.03"),
            new BigDecimal("7331.32"));
    FixedAssetLine fixedAssetLine =
        fixedAssetLineComputationService.computePlannedFixedAssetLine(
            fixedAsset, previousFixedAssetLine);

    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2027, 6, 30),
            new BigDecimal("7331.32"),
            new BigDecimal("102638.35"),
            new BigDecimal("0.00")),
        fixedAssetLine);
  }

  @Test
  public void testComputeFirstDegressiveAssetLine() {
    FixedAsset fixedAsset =
        createFixedAsset(
            FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE,
            new BigDecimal("1.75"),
            LocalDate.of(2020, 3, 31),
            LocalDate.of(2020, 12, 31),
            5,
            12,
            createFixedAssetCategoryFromIsProrataTemporis(true, false),
            new BigDecimal("20000.00"));

    FixedAssetLine fixedAssetLine =
        fixedAssetLineComputationService.computeInitialPlannedFixedAssetLine(fixedAsset);

    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2020, 12, 31),
            new BigDecimal("5250.00"),
            new BigDecimal("5250.00"),
            new BigDecimal("14750.00")),
        fixedAssetLine);
  }

  @Test
  public void testComputeOngoingDegressiveAssetLine() {
    FixedAsset fixedAsset =
        createFixedAsset(
            FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE,
            new BigDecimal("1.75"),
            LocalDate.of(2020, 3, 31),
            LocalDate.of(2020, 12, 31),
            5,
            12,
            createFixedAssetCategoryFromIsProrataTemporis(true, false),
            new BigDecimal("20000.00"));
    FixedAssetLine previousFixedAssetLine =
        createFixedAssetLine(
            LocalDate.of(2020, 12, 31),
            new BigDecimal("5250.00"),
            new BigDecimal("5250.00"),
            new BigDecimal("14750.00"));
    fixedAsset.addFixedAssetLineListItem(previousFixedAssetLine);
    FixedAssetLine fixedAssetLine =
        fixedAssetLineComputationService.computePlannedFixedAssetLine(
            fixedAsset, previousFixedAssetLine);

    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2021, 12, 31),
            new BigDecimal("5162.50"),
            new BigDecimal("10412.50"),
            new BigDecimal("9587.50")),
        fixedAssetLine);
  }

  @Test
  public void testComputeOngoingDegressiveAssetLineSwitchToLinear() {
    FixedAsset fixedAsset =
        createFixedAsset(
            FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE,
            new BigDecimal("1.75"),
            LocalDate.of(2020, 3, 31),
            LocalDate.of(2020, 12, 31),
            5,
            12,
            createFixedAssetCategoryFromIsProrataTemporis(true, false),
            new BigDecimal("20000.00"));

    // fill with empty previous line, should not change the result
    for (int i = 0; i < 2; i++) {
      fixedAsset.addFixedAssetLineListItem(
          createFixedAssetLine(LocalDate.now(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    FixedAssetLine previousFixedAssetLine =
        createFixedAssetLine(
            LocalDate.of(2020, 12, 31),
            new BigDecimal("3356.00"),
            new BigDecimal("13769.00"),
            new BigDecimal("6231.00"));
    fixedAsset.addFixedAssetLineListItem(previousFixedAssetLine);
    FixedAssetLine fixedAssetLine =
        fixedAssetLineComputationService.computePlannedFixedAssetLine(
            fixedAsset, previousFixedAssetLine);

    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2021, 12, 31),
            new BigDecimal("3115.50"),
            new BigDecimal("16884.50"),
            new BigDecimal("3115.50")),
        fixedAssetLine);
  }
}
