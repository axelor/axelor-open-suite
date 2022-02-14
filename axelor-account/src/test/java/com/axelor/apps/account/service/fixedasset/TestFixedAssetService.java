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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveLineService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;

public class TestFixedAssetService {

  protected FixedAssetService fixedAssetService;
  protected FixedAssetRepository fixedAssetRepo;
  protected FixedAssetLineMoveService fixedAssetLineMoveService;
  protected FixedAssetLineComputationService fixedAssetLineComputationService;
  protected MoveLineService moveLineService;
  protected AccountConfigService accountConfigService;

  /*
   * Prepare dependencies by mocking them
   */
  @Before
  public void prepare() {

    fixedAssetRepo = mock(FixedAssetRepository.class);
    fixedAssetLineMoveService = mock(FixedAssetLineMoveService.class);
    moveLineService = mock(MoveLineService.class);
    accountConfigService = mock(AccountConfigService.class);

    fixedAssetLineComputationService = new FixedAssetLineComputationServiceImpl();

    fixedAssetService =
        new FixedAssetServiceImpl(
            fixedAssetRepo,
            fixedAssetLineMoveService,
            fixedAssetLineComputationService,
            moveLineService,
            accountConfigService);

    prepareFixedAssetRepo();
  }

  protected void prepareFixedAssetRepo() {
    when(fixedAssetRepo.save(any(FixedAsset.class)))
        .then((Answer<FixedAsset>) invocation -> (FixedAsset) invocation.getArguments()[0]);
  }

  /*
   * =================================
   * ==  Simple linear fixed asset  ==
   * =================================
   */
  protected FixedAsset generateAndComputeLineSimpleLinearFixedAsset() {
    FixedAsset fixedAsset =
        createFixedAsset(
            FixedAssetRepository.COMPUTATION_METHOD_LINEAR,
            LocalDate.of(2020, 10, 4),
            LocalDate.of(2020, 12, 31),
            5,
            12,
            createFixedAssetCategoryFromIsProrataTemporis(false),
            new BigDecimal("500.00"));
    fixedAssetService.generateAndComputeLines(fixedAsset);
    return fixedAsset;
  }

  @Test
  public void testGenerateAndComputeLinesSimpleLinearFixedAsset() {
    FixedAsset fixedAsset = generateAndComputeLineSimpleLinearFixedAsset();
    Assert.assertTrue(
        fixedAsset.getFixedAssetLineList() != null
            && fixedAsset.getFixedAssetLineList().size() == 5);
  }

  @Test
  public void testGenerateAndComputeLinesSimpleLinearFixedAssetFirstLine() {
    FixedAsset fixedAsset = generateAndComputeLineSimpleLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2020, 12, 31),
            new BigDecimal("100.00"),
            new BigDecimal("100.00"),
            new BigDecimal("400.00")),
        fixedAsset.getFixedAssetLineList().get(0));
  }

  @Test
  public void testGenerateAndComputeLinesSimpleLinearFixedAssetSecondLine() {
    FixedAsset fixedAsset = generateAndComputeLineSimpleLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2021, 12, 31),
            new BigDecimal("100.00"),
            new BigDecimal("200.00"),
            new BigDecimal("300.00")),
        fixedAsset.getFixedAssetLineList().get(1));
  }

  @Test
  public void testGenerateAndComputeLinesSimpleLinearFixedAssetThirdLine() {
    FixedAsset fixedAsset = generateAndComputeLineSimpleLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2022, 12, 31),
            new BigDecimal("100.00"),
            new BigDecimal("300.00"),
            new BigDecimal("200.00")),
        fixedAsset.getFixedAssetLineList().get(2));
  }

  @Test
  public void testGenerateAndComputeLinesSimpleLinearFixedAssetFourthLine() {
    FixedAsset fixedAsset = generateAndComputeLineSimpleLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2023, 12, 31),
            new BigDecimal("100.00"),
            new BigDecimal("400.00"),
            new BigDecimal("100.00")),
        fixedAsset.getFixedAssetLineList().get(3));
  }

  @Test
  public void testGenerateAndComputeLinesSimpleLinearFixedAssetFifthLine() {
    FixedAsset fixedAsset = generateAndComputeLineSimpleLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2024, 12, 31),
            new BigDecimal("100.00"),
            new BigDecimal("500.00"),
            new BigDecimal("0.00")),
        fixedAsset.getFixedAssetLineList().get(4));
  }

  /*
   * ================================================================================================
   * ==  Linear fixed asset with prorata but acquisition date is equal to first depreciation date  ==
   * ================================================================================================
   */

  protected FixedAsset generateAndComputeLineProrataLinearFixedAssetNoProrataNeeded() {
    FixedAsset fixedAsset =
        createFixedAsset(
            FixedAssetRepository.COMPUTATION_METHOD_LINEAR,
            LocalDate.of(2020, 12, 31),
            LocalDate.of(2020, 12, 31),
            5,
            12,
            createFixedAssetCategoryFromIsProrataTemporis(true),
            new BigDecimal("500.00"));
    fixedAssetService.generateAndComputeLines(fixedAsset);
    return fixedAsset;
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAssetNoProrataNeeded() {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAssetNoProrataNeeded();
    Assert.assertTrue(
        fixedAsset.getFixedAssetLineList() != null
            && fixedAsset.getFixedAssetLineList().size() == 5);
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAssetNoProrataNeededFirstLine() {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAssetNoProrataNeeded();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2020, 12, 31),
            new BigDecimal("100.00"),
            new BigDecimal("100.00"),
            new BigDecimal("400.00")),
        fixedAsset.getFixedAssetLineList().get(0));
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAssetNoProrataNeededSecondLine() {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAssetNoProrataNeeded();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2021, 12, 31),
            new BigDecimal("100.00"),
            new BigDecimal("200.00"),
            new BigDecimal("300.00")),
        fixedAsset.getFixedAssetLineList().get(1));
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAssetNoProrataNeededThirdLine() {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAssetNoProrataNeeded();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2022, 12, 31),
            new BigDecimal("100.00"),
            new BigDecimal("300.00"),
            new BigDecimal("200.00")),
        fixedAsset.getFixedAssetLineList().get(2));
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAssetNoProrataNeededFourthLine() {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAssetNoProrataNeeded();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2023, 12, 31),
            new BigDecimal("100.00"),
            new BigDecimal("400.00"),
            new BigDecimal("100.00")),
        fixedAsset.getFixedAssetLineList().get(3));
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAssetNoProrataNeededFifthLine() {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAssetNoProrataNeeded();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2024, 12, 31),
            new BigDecimal("100.00"),
            new BigDecimal("500.00"),
            new BigDecimal("0.00")),
        fixedAsset.getFixedAssetLineList().get(4));
  }

  /*
   * ==================================
   * ==  Prorata linear fixed asset  ==
   * ==================================
   */

  protected FixedAsset generateAndComputeLineProrataLinearFixedAsset() {
    FixedAsset fixedAsset =
        createFixedAsset(
            FixedAssetRepository.COMPUTATION_METHOD_LINEAR,
            LocalDate.of(2020, 10, 5),
            LocalDate.of(2020, 12, 31),
            5,
            12,
            createFixedAssetCategoryFromIsProrataTemporis(true),
            new BigDecimal("500.00"));
    fixedAssetService.generateAndComputeLines(fixedAsset);
    return fixedAsset;
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAsset() {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAsset();
    Assert.assertTrue(
        fixedAsset.getFixedAssetLineList() != null
            && fixedAsset.getFixedAssetLineList().size() == 6);
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAssetFirstLine() {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2020, 12, 31),
            new BigDecimal("23.89"),
            new BigDecimal("23.89"),
            new BigDecimal("476.11")),
        fixedAsset.getFixedAssetLineList().get(0));
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAssetSecondLine() {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2021, 12, 31),
            new BigDecimal("100.00"),
            new BigDecimal("123.89"),
            new BigDecimal("376.11")),
        fixedAsset.getFixedAssetLineList().get(1));
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAssetThirdLine() {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2022, 12, 31),
            new BigDecimal("100.00"),
            new BigDecimal("223.89"),
            new BigDecimal("276.11")),
        fixedAsset.getFixedAssetLineList().get(2));
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAssetFourthLine() {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2023, 12, 31),
            new BigDecimal("100.00"),
            new BigDecimal("323.89"),
            new BigDecimal("176.11")),
        fixedAsset.getFixedAssetLineList().get(3));
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAssetFifthLine() {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2024, 12, 31),
            new BigDecimal("100.00"),
            new BigDecimal("423.89"),
            new BigDecimal("76.11")),
        fixedAsset.getFixedAssetLineList().get(4));
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAssetSixthLine() {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2025, 10, 4),
            new BigDecimal("76.11"),
            new BigDecimal("500.00"),
            new BigDecimal("0.00")),
        fixedAsset.getFixedAssetLineList().get(5));
  }

  /*
   * =====================================
   * ==  US prorata linear fixed asset  ==
   * =====================================
   */

  protected FixedAsset generateAndComputeLineUsProrataLinearFixedAsset() {
    FixedAsset fixedAsset =
        createFixedAsset(
            FixedAssetRepository.COMPUTATION_METHOD_LINEAR,
            LocalDate.of(2020, 7, 1),
            LocalDate.of(2020, 12, 31),
            7,
            12,
            createFixedAssetCategoryFromIsProrataTemporis(true, true),
            new BigDecimal("102638.35"));
    fixedAssetService.generateAndComputeLines(fixedAsset);
    return fixedAsset;
  }

  @Test
  public void testGenerateAndComputeLinesUsProrataLinearFixedAsset() {
    FixedAsset fixedAsset = generateAndComputeLineUsProrataLinearFixedAsset();
    Assert.assertTrue(
        fixedAsset.getFixedAssetLineList() != null
            && fixedAsset.getFixedAssetLineList().size() == 8);
  }

  @Test
  public void testGenerateAndComputeLinesUsProrataLinearFixedAssetFirstLine() {
    FixedAsset fixedAsset = generateAndComputeLineUsProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2020, 12, 31),
            new BigDecimal("7331.31"),
            new BigDecimal("7331.31"),
            new BigDecimal("95307.04")),
        fixedAsset.getFixedAssetLineList().get(0));
  }

  @Test
  public void testGenerateAndComputeLinesUsProrataLinearFixedAssetSecondLine() {
    FixedAsset fixedAsset = generateAndComputeLineUsProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2021, 12, 31),
            new BigDecimal("14662.62"),
            new BigDecimal("21993.93"),
            new BigDecimal("80644.42")),
        fixedAsset.getFixedAssetLineList().get(1));
  }

  @Test
  public void testGenerateAndComputeLinesUsProrataLinearFixedAssetThirdLine() {
    FixedAsset fixedAsset = generateAndComputeLineUsProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2022, 12, 31),
            new BigDecimal("14662.62"),
            new BigDecimal("36656.55"),
            new BigDecimal("65981.80")),
        fixedAsset.getFixedAssetLineList().get(2));
  }

  @Test
  public void testGenerateAndComputeLinesUsProrataLinearFixedAssetFourthLine() {
    FixedAsset fixedAsset = generateAndComputeLineUsProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2023, 12, 31),
            new BigDecimal("14662.62"),
            new BigDecimal("51319.17"),
            new BigDecimal("51319.18")),
        fixedAsset.getFixedAssetLineList().get(3));
  }

  @Test
  public void testGenerateAndComputeLinesUsProrataLinearFixedAssetFifthLine() {
    FixedAsset fixedAsset = generateAndComputeLineUsProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2024, 12, 31),
            new BigDecimal("14662.62"),
            new BigDecimal("65981.79"),
            new BigDecimal("36656.56")),
        fixedAsset.getFixedAssetLineList().get(4));
  }

  @Test
  public void testGenerateAndComputeLinesUsProrataLinearFixedAssetSixthLine() {
    FixedAsset fixedAsset = generateAndComputeLineUsProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2025, 12, 31),
            new BigDecimal("14662.62"),
            new BigDecimal("80644.41"),
            new BigDecimal("21993.94")),
        fixedAsset.getFixedAssetLineList().get(5));
  }

  @Test
  public void testGenerateAndComputeLinesUsProrataLinearFixedAssetSeventhLine() {
    FixedAsset fixedAsset = generateAndComputeLineUsProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2026, 12, 31),
            new BigDecimal("14662.62"),
            new BigDecimal("95307.03"),
            new BigDecimal("7331.32")),
        fixedAsset.getFixedAssetLineList().get(6));
  }

  @Test
  public void testGenerateAndComputeLinesUsProrataLinearFixedAssetLastLine() {
    FixedAsset fixedAsset = generateAndComputeLineUsProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2027, 6, 30),
            new BigDecimal("7331.32"),
            new BigDecimal("102638.35"),
            new BigDecimal("0.00")),
        fixedAsset.getFixedAssetLineList().get(7));
  }

  /*
   * =====================================
   * ==  Prorata degressive fixed asset ==
   * =====================================
   */

  protected FixedAsset generateAndComputeLinesProrataDegressiveFixedAsset() {
    FixedAsset fixedAsset =
        createFixedAsset(
            FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE,
            new BigDecimal("2.25"),
            LocalDate.of(2021, 7, 1),
            LocalDate.of(2021, 12, 31),
            7,
            12,
            createFixedAssetCategoryFromIsProrataTemporis(true, false),
            new BigDecimal("102638.35"));
    fixedAssetService.generateAndComputeLines(fixedAsset);
    return fixedAsset;
  }

  @Test
  public void testGenerateAndComputeLinesProrataDegressiveFixedAsset() {
    FixedAsset fixedAsset = generateAndComputeLinesProrataDegressiveFixedAsset();
    Assert.assertTrue(
        fixedAsset.getFixedAssetLineList() != null
            && fixedAsset.getFixedAssetLineList().size() == 7);
  }

  @Test
  public void testGenerateAndComputeLinesProrataDegressiveFixedAssetFirstLine() {
    FixedAsset fixedAsset = generateAndComputeLinesProrataDegressiveFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2021, 12, 31),
            new BigDecimal("16495.45"),
            new BigDecimal("16495.45"),
            new BigDecimal("86142.90")),
        fixedAsset.getFixedAssetLineList().get(0));
  }

  @Test
  public void testGenerateAndComputeLinesProrataDegressiveFixedAssetSecondLine() {
    FixedAsset fixedAsset = generateAndComputeLinesProrataDegressiveFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2022, 12, 31),
            new BigDecimal("27688.79"),
            new BigDecimal("44184.24"),
            new BigDecimal("58454.11")),
        fixedAsset.getFixedAssetLineList().get(1));
  }

  @Test
  public void testGenerateAndComputeLinesProrataDegressiveFixedAssetThirdLine() {
    FixedAsset fixedAsset = generateAndComputeLinesProrataDegressiveFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2023, 12, 31),
            new BigDecimal("18788.82"),
            new BigDecimal("62973.06"),
            new BigDecimal("39665.29")),
        fixedAsset.getFixedAssetLineList().get(2));
  }

  @Test
  public void testGenerateAndComputeLinesProrataDegressiveFixedAssetFourthLine() {
    FixedAsset fixedAsset = generateAndComputeLinesProrataDegressiveFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2024, 12, 31),
            new BigDecimal("12749.56"),
            new BigDecimal("75722.62"),
            new BigDecimal("26915.73")),
        fixedAsset.getFixedAssetLineList().get(3));
  }

  @Test
  public void testGenerateAndComputeLinesProrataDegressiveFixedAssetFifthLine() {
    FixedAsset fixedAsset = generateAndComputeLinesProrataDegressiveFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2025, 12, 31),
            new BigDecimal("8971.91"),
            new BigDecimal("84694.53"),
            new BigDecimal("17943.82")),
        fixedAsset.getFixedAssetLineList().get(4));
  }

  @Test
  public void testGenerateAndComputeLinesProrataDegressiveFixedAssetSixthLine() {
    FixedAsset fixedAsset = generateAndComputeLinesProrataDegressiveFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2026, 12, 31),
            new BigDecimal("8971.91"),
            new BigDecimal("93666.44"),
            new BigDecimal("8971.91")),
        fixedAsset.getFixedAssetLineList().get(5));
  }

  @Test
  public void testGenerateAndComputeLinesProrataDegressiveFixedAssetLastLine() {
    FixedAsset fixedAsset = generateAndComputeLinesProrataDegressiveFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2027, 6, 30),
            new BigDecimal("8971.91"),
            new BigDecimal("102638.35"),
            new BigDecimal("0.00")),
        fixedAsset.getFixedAssetLineList().get(6));
  }

  @Test
  public void testGenerateAndComputeLinesNoProrataDegressiveFixedAssetLastLine() {
    FixedAsset fixedAsset =
        createFixedAsset(
            FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE,
            new BigDecimal("2.25"),
            LocalDate.of(2021, 12, 31),
            LocalDate.of(2021, 12, 31),
            7,
            12,
            createFixedAssetCategoryFromIsProrataTemporis(true, false),
            new BigDecimal("102638.35"));
    fixedAssetService.generateAndComputeLines(fixedAsset);
    Assert.assertEquals(
        LocalDate.of(2027, 12, 31),
        fixedAsset.getFixedAssetLineList().get(6).getDepreciationDate());
  }
}
