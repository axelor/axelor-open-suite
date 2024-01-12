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

import static com.axelor.apps.account.service.fixedasset.FixedAssetTestTool.assertFixedAssetLineEquals;
import static com.axelor.apps.account.service.fixedasset.FixedAssetTestTool.createFixedAsset;
import static com.axelor.apps.account.service.fixedasset.FixedAssetTestTool.createFixedAssetCategoryFromIsProrataTemporis;
import static com.axelor.apps.account.service.fixedasset.FixedAssetTestTool.createFixedAssetLine;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.service.CurrencyScaleServiceAccount;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.fixedasset.factory.FixedAssetLineComputationServiceFactory;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

class TestFixedAssetGenerationService {

  private static FixedAssetGenerationService fixedAssetGenerationService;
  private static FixedAssetDateService fixedAssetDateService;
  private static FixedAssetRepository fixedAssetRepo;

  /*
   * Prepare dependencies by mocking them
   */
  @BeforeAll
  static void prepare() throws AxelorException {

    fixedAssetRepo = mock(FixedAssetRepository.class);
    AccountConfigService accountConfigService = mock(AccountConfigService.class);
    FixedAssetDerogatoryLineService fixedAssetDerogatoryLineService =
        mock(FixedAssetDerogatoryLineService.class);
    fixedAssetDateService = mock(FixedAssetDateService.class);
    SequenceService sequenceService = mock(SequenceService.class);
    AppBaseService appBaseService = mock(AppBaseService.class);
    FixedAssetLineService fixedAssetLineService = mock(FixedAssetLineService.class);
    FixedAssetLineComputationServiceFactory fixedAssetLineComputationServiceFactory =
        mock(FixedAssetLineComputationServiceFactory.class);
    FixedAssetFailOverControlService fixedAssetFailOverControlService =
        mock(FixedAssetFailOverControlService.class);
    FixedAssetValidateService fixedAssetValidateService = mock(FixedAssetValidateService.class);
    FixedAssetImportService fixedAssetImportService = mock(FixedAssetImportService.class);
    CurrencyScaleServiceAccount currencyScaleServiceAccount =
        mock(CurrencyScaleServiceAccount.class);

    FixedAssetLineComputationService fixedAssetLineComputationService =
        new FixedAssetLineEconomicComputationServiceImpl(
            fixedAssetDateService,
            fixedAssetFailOverControlService,
            appBaseService,
            currencyScaleServiceAccount);
    when(fixedAssetLineComputationServiceFactory.getFixedAssetComputationService(
            any(FixedAsset.class), any(Integer.TYPE)))
        .thenReturn(fixedAssetLineComputationService);
    FixedAssetLineGenerationService fixedAssetLineGenerationService =
        new FixedAssetLineGenerationServiceImpl(
            fixedAssetLineService,
            fixedAssetDerogatoryLineService,
            fixedAssetLineComputationServiceFactory);
    fixedAssetGenerationService =
        new FixedAssetGenerationServiceImpl(
            fixedAssetLineGenerationService,
            fixedAssetImportService,
            fixedAssetDateService,
            fixedAssetLineService,
            fixedAssetRepo,
            sequenceService,
            accountConfigService,
            appBaseService,
            fixedAssetValidateService,
            currencyScaleServiceAccount);

    prepareFixedAssetRepo();
  }

  protected static void prepareFixedAssetRepo() {
    when(fixedAssetRepo.save(any(FixedAsset.class)))
        .then((Answer<FixedAsset>) invocation -> (FixedAsset) invocation.getArguments()[0]);
  }

  /*
   * =================================
   * ==  Simple linear fixed asset  ==
   * =================================
   */
  protected FixedAsset generateAndComputeLineSimpleLinearFixedAsset() throws AxelorException {
    FixedAsset fixedAsset =
        createFixedAsset(
            FixedAssetRepository.COMPUTATION_METHOD_LINEAR,
            LocalDate.of(2020, 10, 4),
            LocalDate.of(2020, 12, 31),
            5,
            12,
            createFixedAssetCategoryFromIsProrataTemporis(false),
            new BigDecimal("500.000"));
    when(fixedAssetDateService.computeLastDayOfPeriodicity(
            fixedAsset.getPeriodicityTypeSelect(), fixedAsset.getFirstServiceDate()))
        .thenReturn(LocalDate.of(2020, 12, 31));
    fixedAssetGenerationService.generateAndComputeLines(fixedAsset);
    return fixedAsset;
  }

  @Test
  void testGenerateAndComputeLinesSimpleLinearFixedAsset() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineSimpleLinearFixedAsset();
    Assertions.assertTrue(
        fixedAsset.getFixedAssetLineList() != null
            && fixedAsset.getFixedAssetLineList().size() == 5);
  }

  @Test
  void testGenerateAndComputeLinesSimpleLinearFixedAssetFirstLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineSimpleLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2020, 12, 31),
            new BigDecimal("500.000"),
            new BigDecimal("100.000"),
            new BigDecimal("100.000"),
            new BigDecimal("400.000")),
        fixedAsset.getFixedAssetLineList().get(0));
  }

  @Test
  void testGenerateAndComputeLinesSimpleLinearFixedAssetSecondLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineSimpleLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2021, 12, 31),
            new BigDecimal("500.000"),
            new BigDecimal("100.000"),
            new BigDecimal("200.000"),
            new BigDecimal("300.000")),
        fixedAsset.getFixedAssetLineList().get(1));
  }

  @Test
  void testGenerateAndComputeLinesSimpleLinearFixedAssetThirdLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineSimpleLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2022, 12, 31),
            new BigDecimal("500.000"),
            new BigDecimal("100.000"),
            new BigDecimal("300.000"),
            new BigDecimal("200.000")),
        fixedAsset.getFixedAssetLineList().get(2));
  }

  @Test
  void testGenerateAndComputeLinesSimpleLinearFixedAssetFourthLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineSimpleLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2023, 12, 31),
            new BigDecimal("500.000"),
            new BigDecimal("100.000"),
            new BigDecimal("400.000"),
            new BigDecimal("100.000")),
        fixedAsset.getFixedAssetLineList().get(3));
  }

  @Test
  void testGenerateAndComputeLinesSimpleLinearFixedAssetFifthLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineSimpleLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2024, 12, 31),
            new BigDecimal("500.000"),
            new BigDecimal("100.000"),
            new BigDecimal("500.000"),
            new BigDecimal("0.000")),
        fixedAsset.getFixedAssetLineList().get(4));
  }

  /*
   * ================================================================================================
   * ==  Linear fixed asset with prorata but acquisition date is equal to first depreciation date  ==
   * ================================================================================================
   */

  protected FixedAsset generateAndComputeLineProrataLinearFixedAssetNoProrataNeeded()
      throws AxelorException {
    FixedAsset fixedAsset =
        createFixedAsset(
            FixedAssetRepository.COMPUTATION_METHOD_LINEAR,
            LocalDate.of(2020, 12, 31),
            LocalDate.of(2020, 12, 31),
            5,
            12,
            createFixedAssetCategoryFromIsProrataTemporis(true),
            new BigDecimal("500.000"));
    when(fixedAssetDateService.computeLastDayOfPeriodicity(
            fixedAsset.getPeriodicityTypeSelect(), fixedAsset.getFirstServiceDate()))
        .thenReturn(LocalDate.of(2020, 12, 31));
    fixedAssetGenerationService.generateAndComputeLines(fixedAsset);
    return fixedAsset;
  }

  @Test
  void testGenerateAndComputeLinesProrataLinearFixedAssetNoProrataNeeded() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAssetNoProrataNeeded();
    Assertions.assertTrue(
        fixedAsset.getFixedAssetLineList() != null
            && fixedAsset.getFixedAssetLineList().size() == 6);
  }

  @Test
  void testGenerateAndComputeLinesProrataLinearFixedAssetNoProrataNeededFirstLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAssetNoProrataNeeded();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2020, 12, 31),
            new BigDecimal("500.000"),
            new BigDecimal("0.280"),
            new BigDecimal("0.280"),
            new BigDecimal("499.720")),
        fixedAsset.getFixedAssetLineList().get(0));
  }

  @Test
  void testGenerateAndComputeLinesProrataLinearFixedAssetNoProrataNeededSecondLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAssetNoProrataNeeded();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2021, 12, 31),
            new BigDecimal("500.000"),
            new BigDecimal("100.000"),
            new BigDecimal("100.280"),
            new BigDecimal("399.720")),
        fixedAsset.getFixedAssetLineList().get(1));
  }

  @Test
  void testGenerateAndComputeLinesProrataLinearFixedAssetNoProrataNeededThirdLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAssetNoProrataNeeded();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2022, 12, 31),
            new BigDecimal("500.000"),
            new BigDecimal("100.000"),
            new BigDecimal("200.280"),
            new BigDecimal("299.720")),
        fixedAsset.getFixedAssetLineList().get(2));
  }

  @Test
  void testGenerateAndComputeLinesProrataLinearFixedAssetNoProrataNeededFourthLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAssetNoProrataNeeded();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2023, 12, 31),
            new BigDecimal("500.000"),
            new BigDecimal("100.000"),
            new BigDecimal("300.280"),
            new BigDecimal("199.720")),
        fixedAsset.getFixedAssetLineList().get(3));
  }

  @Test
  void testGenerateAndComputeLinesProrataLinearFixedAssetNoProrataNeededFifthLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAssetNoProrataNeeded();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2024, 12, 31),
            new BigDecimal("500.000"),
            new BigDecimal("100.000"),
            new BigDecimal("400.280"),
            new BigDecimal("99.720")),
        fixedAsset.getFixedAssetLineList().get(4));
  }

  @Test
  void testGenerateAndComputeLinesProrataLinearFixedAssetNoProrataNeededSixthLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAssetNoProrataNeeded();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2025, 12, 31),
            new BigDecimal("500.000"),
            new BigDecimal("99.720"),
            new BigDecimal("500.000"),
            new BigDecimal("0.000")),
        fixedAsset.getFixedAssetLineList().get(5));
  }

  /*
   * ==================================
   * ==  Prorata linear fixed asset  ==
   * ==================================
   */

  protected FixedAsset generateAndComputeLineProrataLinearFixedAsset() throws AxelorException {
    FixedAsset fixedAsset =
        createFixedAsset(
            FixedAssetRepository.COMPUTATION_METHOD_LINEAR,
            LocalDate.of(2020, 10, 5),
            LocalDate.of(2020, 12, 31),
            5,
            12,
            createFixedAssetCategoryFromIsProrataTemporis(true),
            new BigDecimal("500.000"));
    when(fixedAssetDateService.computeLastDayOfPeriodicity(
            fixedAsset.getPeriodicityTypeSelect(), fixedAsset.getFirstServiceDate()))
        .thenReturn(LocalDate.of(2020, 12, 31));
    fixedAssetGenerationService.generateAndComputeLines(fixedAsset);
    return fixedAsset;
  }

  @Test
  void testGenerateAndComputeLinesProrataLinearFixedAsset() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAsset();
    Assertions.assertTrue(
        fixedAsset.getFixedAssetLineList() != null
            && fixedAsset.getFixedAssetLineList().size() == 6);
  }

  @Test
  void testGenerateAndComputeLinesProrataLinearFixedAssetFirstLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2020, 12, 31),
            new BigDecimal("500.000"),
            new BigDecimal("23.890"),
            new BigDecimal("23.890"),
            new BigDecimal("476.110")),
        fixedAsset.getFixedAssetLineList().get(0));
  }

  @Test
  void testGenerateAndComputeLinesProrataLinearFixedAssetSecondLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2021, 12, 31),
            new BigDecimal("500.000"),
            new BigDecimal("100.000"),
            new BigDecimal("123.890"),
            new BigDecimal("376.110")),
        fixedAsset.getFixedAssetLineList().get(1));
  }

  @Test
  void testGenerateAndComputeLinesProrataLinearFixedAssetThirdLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2022, 12, 31),
            new BigDecimal("500.000"),
            new BigDecimal("100.000"),
            new BigDecimal("223.890"),
            new BigDecimal("276.110")),
        fixedAsset.getFixedAssetLineList().get(2));
  }

  @Test
  void testGenerateAndComputeLinesProrataLinearFixedAssetFourthLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2023, 12, 31),
            new BigDecimal("500.000"),
            new BigDecimal("100.000"),
            new BigDecimal("323.890"),
            new BigDecimal("176.110")),
        fixedAsset.getFixedAssetLineList().get(3));
  }

  @Test
  void testGenerateAndComputeLinesProrataLinearFixedAssetFifthLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2024, 12, 31),
            new BigDecimal("500.000"),
            new BigDecimal("100.000"),
            new BigDecimal("423.890"),
            new BigDecimal("76.110")),
        fixedAsset.getFixedAssetLineList().get(4));
  }

  /*
   * =====================================
   * ==  US prorata linear fixed asset  ==
   * =====================================
   */

  protected FixedAsset generateAndComputeLineUsProrataLinearFixedAsset() throws AxelorException {
    FixedAsset fixedAsset =
        createFixedAsset(
            FixedAssetRepository.COMPUTATION_METHOD_LINEAR,
            LocalDate.of(2020, 7, 1),
            LocalDate.of(2020, 12, 31),
            7,
            12,
            createFixedAssetCategoryFromIsProrataTemporis(true, true),
            new BigDecimal("102638.350"));
    when(fixedAssetDateService.computeLastDayOfPeriodicity(
            fixedAsset.getPeriodicityTypeSelect(), fixedAsset.getFirstServiceDate()))
        .thenReturn(LocalDate.of(2020, 12, 31));
    fixedAssetGenerationService.generateAndComputeLines(fixedAsset);
    return fixedAsset;
  }

  @Test
  void testGenerateAndComputeLinesUsProrataLinearFixedAsset() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineUsProrataLinearFixedAsset();
    Assertions.assertTrue(
        fixedAsset.getFixedAssetLineList() != null
            && fixedAsset.getFixedAssetLineList().size() == 8);
  }

  @Test
  void testGenerateAndComputeLinesUsProrataLinearFixedAssetFirstLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineUsProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2020, 12, 31),
            new BigDecimal("500.000"),
            new BigDecimal("7372.040"),
            new BigDecimal("7372.040"),
            new BigDecimal("95266.310")),
        fixedAsset.getFixedAssetLineList().get(0));
  }

  @Test
  void testGenerateAndComputeLinesUsProrataLinearFixedAssetSecondLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineUsProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2021, 12, 31),
            new BigDecimal("500.000"),
            new BigDecimal("14662.620"),
            new BigDecimal("22034.660"),
            new BigDecimal("80603.690")),
        fixedAsset.getFixedAssetLineList().get(1));
  }

  @Test
  void testGenerateAndComputeLinesUsProrataLinearFixedAssetThirdLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineUsProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2022, 12, 31),
            new BigDecimal("500.000"),
            new BigDecimal("14662.620"),
            new BigDecimal("36697.280"),
            new BigDecimal("65941.070")),
        fixedAsset.getFixedAssetLineList().get(2));
  }

  @Test
  void testGenerateAndComputeLinesUsProrataLinearFixedAssetFourthLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineUsProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2023, 12, 31),
            new BigDecimal("500.000"),
            new BigDecimal("14662.620"),
            new BigDecimal("51359.900"),
            new BigDecimal("51278.450")),
        fixedAsset.getFixedAssetLineList().get(3));
  }

  @Test
  void testGenerateAndComputeLinesUsProrataLinearFixedAssetFifthLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineUsProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2024, 12, 31),
            new BigDecimal("500.000"),
            new BigDecimal("14662.620"),
            new BigDecimal("66022.520"),
            new BigDecimal("36615.830")),
        fixedAsset.getFixedAssetLineList().get(4));
  }

  @Test
  void testGenerateAndComputeLinesUsProrataLinearFixedAssetSixthLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineUsProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2025, 12, 31),
            new BigDecimal("500.000"),
            new BigDecimal("14662.620"),
            new BigDecimal("80685.140"),
            new BigDecimal("21953.210")),
        fixedAsset.getFixedAssetLineList().get(5));
  }

  @Test
  void testGenerateAndComputeLinesUsProrataLinearFixedAssetSeventhLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineUsProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2026, 12, 31),
            new BigDecimal("102638.350"),
            new BigDecimal("14662.620"),
            new BigDecimal("95347.760"),
            new BigDecimal("7290.590")),
        fixedAsset.getFixedAssetLineList().get(6));
  }

  /*
   * =====================================
   * ==  Prorata degressive fixed asset ==
   * =====================================
   */

  protected FixedAsset generateAndComputeLinesProrataDegressiveFixedAsset() throws AxelorException {
    FixedAsset fixedAsset =
        createFixedAsset(
            FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE,
            new BigDecimal("2.250"),
            LocalDate.of(2021, 7, 1),
            LocalDate.of(2021, 12, 31),
            7,
            12,
            createFixedAssetCategoryFromIsProrataTemporis(true, false),
            new BigDecimal("102638.350"));
    when(fixedAssetDateService.computeLastDayOfPeriodicity(
            fixedAsset.getPeriodicityTypeSelect(), fixedAsset.getFirstServiceDate()))
        .thenReturn(LocalDate.of(2021, 12, 31));
    fixedAssetGenerationService.generateAndComputeLines(fixedAsset);
    return fixedAsset;
  }

  @Test
  void testGenerateAndComputeLinesProrataDegressiveFixedAsset() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLinesProrataDegressiveFixedAsset();
    Assertions.assertTrue(
        fixedAsset.getFixedAssetLineList() != null
            && fixedAsset.getFixedAssetLineList().size() == 7);
  }

  @Test
  void testGenerateAndComputeLinesProrataDegressiveFixedAssetFirstLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLinesProrataDegressiveFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2021, 12, 31),
            new BigDecimal("102638.350"),
            new BigDecimal("16495.450"),
            new BigDecimal("16495.450"),
            new BigDecimal("86142.900")),
        fixedAsset.getFixedAssetLineList().get(0));
  }

  @Test
  void testGenerateAndComputeLinesProrataDegressiveFixedAssetSecondLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLinesProrataDegressiveFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2022, 12, 31),
            new BigDecimal("102638.350"),
            new BigDecimal("27688.790"),
            new BigDecimal("44184.240"),
            new BigDecimal("58454.110")),
        fixedAsset.getFixedAssetLineList().get(1));
  }

  @Test
  void testGenerateAndComputeLinesProrataDegressiveFixedAssetThirdLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLinesProrataDegressiveFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2023, 12, 31),
            new BigDecimal("102638.350"),
            new BigDecimal("18788.820"),
            new BigDecimal("62973.060"),
            new BigDecimal("39665.290")),
        fixedAsset.getFixedAssetLineList().get(2));
  }

  @Test
  void testGenerateAndComputeLinesProrataDegressiveFixedAssetFourthLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLinesProrataDegressiveFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2024, 12, 31),
            new BigDecimal("102638.350"),
            new BigDecimal("12749.560"),
            new BigDecimal("75722.620"),
            new BigDecimal("26915.730")),
        fixedAsset.getFixedAssetLineList().get(3));
  }

  @Test
  void testGenerateAndComputeLinesProrataDegressiveFixedAssetFifthLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLinesProrataDegressiveFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2025, 12, 31),
            new BigDecimal("102638.350"),
            new BigDecimal("8971.910"),
            new BigDecimal("84694.530"),
            new BigDecimal("17943.820")),
        fixedAsset.getFixedAssetLineList().get(4));
  }

  @Test
  void testGenerateAndComputeLinesProrataDegressiveFixedAssetSixthLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLinesProrataDegressiveFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2026, 12, 31),
            new BigDecimal("102638.350"),
            new BigDecimal("8971.910"),
            new BigDecimal("93666.440"),
            new BigDecimal("8971.910")),
        fixedAsset.getFixedAssetLineList().get(5));
  }

  @Test
  void testGenerateAndComputeLinesProrataDegressiveFixedAssetLastLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLinesProrataDegressiveFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2027, 12, 31),
            new BigDecimal("102638.350"),
            new BigDecimal("8971.910"),
            new BigDecimal("102638.350"),
            new BigDecimal("0.000")),
        fixedAsset.getFixedAssetLineList().get(6));
  }

  @Test
  void testGenerateAndComputeLinesNoProrataDegressiveFixedAssetLastLine() throws AxelorException {
    FixedAsset fixedAsset =
        createFixedAsset(
            FixedAssetRepository.COMPUTATION_METHOD_DEGRESSIVE,
            new BigDecimal("2.250"),
            LocalDate.of(2021, 12, 31),
            LocalDate.of(2021, 12, 31),
            7,
            12,
            createFixedAssetCategoryFromIsProrataTemporis(true, false),
            new BigDecimal("102638.350"));
    when(fixedAssetDateService.computeLastDayOfPeriodicity(
            fixedAsset.getPeriodicityTypeSelect(), fixedAsset.getFirstServiceDate()))
        .thenReturn(LocalDate.of(2021, 12, 31));
    fixedAssetGenerationService.generateAndComputeLines(fixedAsset);
    Assertions.assertEquals(
        LocalDate.of(2027, 12, 31),
        fixedAsset.getFixedAssetLineList().get(6).getDepreciationDate());
  }
}
