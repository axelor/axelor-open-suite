/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.fixedasset.factory.FixedAssetLineComputationServiceFactory;
import com.axelor.apps.account.service.fixedasset.factory.FixedAssetLineServiceFactory;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;

public class TestFixedAssetGenerationService {

  protected FixedAssetGenerationService fixedAssetGenerationService;
  protected FixedAssetRepository fixedAssetRepo;
  protected FixedAssetLineMoveService fixedAssetLineMoveService;
  protected FixedAssetLineComputationService fixedAssetLineComputationService;
  protected AccountConfigService accountConfigService;
  protected FixedAssetDerogatoryLineService fixedAssetDerogatoryLineService;
  protected FixedAssetDateService fixedAssetDateService;
  protected FixedAssetLineGenerationService fixedAssetLineGenerationService;
  protected FixedAssetImportService fixedAssetImportService;
  protected FixedAssetLineRepository fixedAssetLineRepo;
  protected FixedAssetDerogatoryLineMoveService fixedAssetDerogatoryLineMoveService;
  protected SequenceService sequenceService;
  protected FixedAssetLineService fixedAssetLineService;
  protected FixedAssetLineServiceFactory fixedAssetLineServiceFactory;
  protected FixedAssetLineComputationServiceFactory fixedAssetLineComputationServiceFactory;
  protected FixedAssetFailOverControlService fixedAssetFailOverControlService;
  protected FixedAssetValidateService fixedAssetValidateService;
  protected AppBaseService appBaseService;

  /*
   * Prepare dependencies by mocking them
   */
  @Before
  public void prepare() throws AxelorException {

    fixedAssetRepo = mock(FixedAssetRepository.class);
    fixedAssetLineRepo = mock(FixedAssetLineRepository.class);
    fixedAssetLineMoveService = mock(FixedAssetLineMoveService.class);
    accountConfigService = mock(AccountConfigService.class);
    fixedAssetDerogatoryLineService = mock(FixedAssetDerogatoryLineService.class);
    fixedAssetDateService = mock(FixedAssetDateService.class);
    fixedAssetDerogatoryLineMoveService = mock(FixedAssetDerogatoryLineMoveService.class);
    sequenceService = mock(SequenceService.class);
    appBaseService = mock(AppBaseService.class);
    fixedAssetLineService = mock(FixedAssetLineService.class);
    fixedAssetLineServiceFactory = mock(FixedAssetLineServiceFactory.class);
    fixedAssetLineComputationServiceFactory = mock(FixedAssetLineComputationServiceFactory.class);
    fixedAssetFailOverControlService = mock(FixedAssetFailOverControlService.class);
    fixedAssetValidateService = mock(FixedAssetValidateService.class);
    fixedAssetDateService = mock(FixedAssetDateService.class);
    fixedAssetImportService = mock(FixedAssetImportService.class);

    fixedAssetLineComputationService =
        new FixedAssetLineEconomicComputationServiceImpl(
            fixedAssetDateService, fixedAssetFailOverControlService, appBaseService);
    when(fixedAssetLineComputationServiceFactory.getFixedAssetComputationService(
            any(FixedAsset.class), any(Integer.TYPE)))
        .thenReturn(fixedAssetLineComputationService);
    fixedAssetLineGenerationService =
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
            fixedAssetValidateService);

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
  protected FixedAsset generateAndComputeLineSimpleLinearFixedAsset() throws AxelorException {
    FixedAsset fixedAsset =
        createFixedAsset(
            FixedAssetRepository.COMPUTATION_METHOD_LINEAR,
            LocalDate.of(2020, 10, 4),
            LocalDate.of(2020, 12, 31),
            5,
            12,
            createFixedAssetCategoryFromIsProrataTemporis(false),
            new BigDecimal("500.00"));
    when(fixedAssetDateService.computeLastDayOfPeriodicity(
            fixedAsset.getPeriodicityTypeSelect(), fixedAsset.getFirstServiceDate()))
        .thenReturn(LocalDate.of(2020, 12, 31));
    fixedAssetGenerationService.generateAndComputeLines(fixedAsset);
    return fixedAsset;
  }

  @Test
  public void testGenerateAndComputeLinesSimpleLinearFixedAsset() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineSimpleLinearFixedAsset();
    Assert.assertTrue(
        fixedAsset.getFixedAssetLineList() != null
            && fixedAsset.getFixedAssetLineList().size() == 5);
  }

  @Test
  public void testGenerateAndComputeLinesSimpleLinearFixedAssetFirstLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineSimpleLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2020, 12, 31),
            new BigDecimal("500.00"),
            new BigDecimal("100.00"),
            new BigDecimal("100.00"),
            new BigDecimal("400.00")),
        fixedAsset.getFixedAssetLineList().get(0));
  }

  @Test
  public void testGenerateAndComputeLinesSimpleLinearFixedAssetSecondLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineSimpleLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2021, 12, 31),
            new BigDecimal("500.00"),
            new BigDecimal("100.00"),
            new BigDecimal("200.00"),
            new BigDecimal("300.00")),
        fixedAsset.getFixedAssetLineList().get(1));
  }

  @Test
  public void testGenerateAndComputeLinesSimpleLinearFixedAssetThirdLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineSimpleLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2022, 12, 31),
            new BigDecimal("500.00"),
            new BigDecimal("100.00"),
            new BigDecimal("300.00"),
            new BigDecimal("200.00")),
        fixedAsset.getFixedAssetLineList().get(2));
  }

  @Test
  public void testGenerateAndComputeLinesSimpleLinearFixedAssetFourthLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineSimpleLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2023, 12, 31),
            new BigDecimal("500.00"),
            new BigDecimal("100.00"),
            new BigDecimal("400.00"),
            new BigDecimal("100.00")),
        fixedAsset.getFixedAssetLineList().get(3));
  }

  @Test
  public void testGenerateAndComputeLinesSimpleLinearFixedAssetFifthLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineSimpleLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2024, 12, 31),
            new BigDecimal("500.00"),
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
            new BigDecimal("500.00"));
    when(fixedAssetDateService.computeLastDayOfPeriodicity(
            fixedAsset.getPeriodicityTypeSelect(), fixedAsset.getFirstServiceDate()))
        .thenReturn(LocalDate.of(2020, 12, 31));
    fixedAssetGenerationService.generateAndComputeLines(fixedAsset);
    return fixedAsset;
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAssetNoProrataNeeded()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAssetNoProrataNeeded();
    Assert.assertTrue(
        fixedAsset.getFixedAssetLineList() != null
            && fixedAsset.getFixedAssetLineList().size() == 6);
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAssetNoProrataNeededFirstLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAssetNoProrataNeeded();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2020, 12, 31),
            new BigDecimal("500.00"),
            new BigDecimal("0.28"),
            new BigDecimal("0.28"),
            new BigDecimal("499.72")),
        fixedAsset.getFixedAssetLineList().get(0));
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAssetNoProrataNeededSecondLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAssetNoProrataNeeded();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2021, 12, 31),
            new BigDecimal("500.00"),
            new BigDecimal("100.00"),
            new BigDecimal("100.28"),
            new BigDecimal("399.72")),
        fixedAsset.getFixedAssetLineList().get(1));
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAssetNoProrataNeededThirdLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAssetNoProrataNeeded();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2022, 12, 31),
            new BigDecimal("500.00"),
            new BigDecimal("100.00"),
            new BigDecimal("200.28"),
            new BigDecimal("299.72")),
        fixedAsset.getFixedAssetLineList().get(2));
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAssetNoProrataNeededFourthLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAssetNoProrataNeeded();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2023, 12, 31),
            new BigDecimal("500.00"),
            new BigDecimal("100.00"),
            new BigDecimal("300.28"),
            new BigDecimal("199.72")),
        fixedAsset.getFixedAssetLineList().get(3));
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAssetNoProrataNeededFifthLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAssetNoProrataNeeded();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2024, 12, 31),
            new BigDecimal("500.00"),
            new BigDecimal("100.00"),
            new BigDecimal("400.28"),
            new BigDecimal("99.72")),
        fixedAsset.getFixedAssetLineList().get(4));
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAssetNoProrataNeededSixthLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAssetNoProrataNeeded();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2025, 12, 31),
            new BigDecimal("500.00"),
            new BigDecimal("99.72"),
            new BigDecimal("500.00"),
            new BigDecimal("0.00")),
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
            new BigDecimal("500.00"));
    when(fixedAssetDateService.computeLastDayOfPeriodicity(
            fixedAsset.getPeriodicityTypeSelect(), fixedAsset.getFirstServiceDate()))
        .thenReturn(LocalDate.of(2020, 12, 31));
    fixedAssetGenerationService.generateAndComputeLines(fixedAsset);
    return fixedAsset;
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAsset() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAsset();
    Assert.assertTrue(
        fixedAsset.getFixedAssetLineList() != null
            && fixedAsset.getFixedAssetLineList().size() == 6);
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAssetFirstLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2020, 12, 31),
            new BigDecimal("500.00"),
            new BigDecimal("23.89"),
            new BigDecimal("23.89"),
            new BigDecimal("476.11")),
        fixedAsset.getFixedAssetLineList().get(0));
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAssetSecondLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2021, 12, 31),
            new BigDecimal("500.00"),
            new BigDecimal("100.00"),
            new BigDecimal("123.89"),
            new BigDecimal("376.11")),
        fixedAsset.getFixedAssetLineList().get(1));
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAssetThirdLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2022, 12, 31),
            new BigDecimal("500.00"),
            new BigDecimal("100.00"),
            new BigDecimal("223.89"),
            new BigDecimal("276.11")),
        fixedAsset.getFixedAssetLineList().get(2));
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAssetFourthLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2023, 12, 31),
            new BigDecimal("500.00"),
            new BigDecimal("100.00"),
            new BigDecimal("323.89"),
            new BigDecimal("176.11")),
        fixedAsset.getFixedAssetLineList().get(3));
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAssetFifthLine() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2024, 12, 31),
            new BigDecimal("500.00"),
            new BigDecimal("100.00"),
            new BigDecimal("423.89"),
            new BigDecimal("76.11")),
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
            new BigDecimal("102638.35"));
    when(fixedAssetDateService.computeLastDayOfPeriodicity(
            fixedAsset.getPeriodicityTypeSelect(), fixedAsset.getFirstServiceDate()))
        .thenReturn(LocalDate.of(2020, 12, 31));
    fixedAssetGenerationService.generateAndComputeLines(fixedAsset);
    return fixedAsset;
  }

  @Test
  public void testGenerateAndComputeLinesUsProrataLinearFixedAsset() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineUsProrataLinearFixedAsset();
    Assert.assertTrue(
        fixedAsset.getFixedAssetLineList() != null
            && fixedAsset.getFixedAssetLineList().size() == 8);
  }

  @Test
  public void testGenerateAndComputeLinesUsProrataLinearFixedAssetFirstLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineUsProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2020, 12, 31),
            new BigDecimal("500.00"),
            new BigDecimal("7372.04"),
            new BigDecimal("7372.04"),
            new BigDecimal("95266.31")),
        fixedAsset.getFixedAssetLineList().get(0));
  }

  @Test
  public void testGenerateAndComputeLinesUsProrataLinearFixedAssetSecondLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineUsProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2021, 12, 31),
            new BigDecimal("500.00"),
            new BigDecimal("14662.62"),
            new BigDecimal("22034.66"),
            new BigDecimal("80603.69")),
        fixedAsset.getFixedAssetLineList().get(1));
  }

  @Test
  public void testGenerateAndComputeLinesUsProrataLinearFixedAssetThirdLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineUsProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2022, 12, 31),
            new BigDecimal("500.00"),
            new BigDecimal("14662.62"),
            new BigDecimal("36697.28"),
            new BigDecimal("65941.07")),
        fixedAsset.getFixedAssetLineList().get(2));
  }

  @Test
  public void testGenerateAndComputeLinesUsProrataLinearFixedAssetFourthLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineUsProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2023, 12, 31),
            new BigDecimal("500.00"),
            new BigDecimal("14662.62"),
            new BigDecimal("51359.90"),
            new BigDecimal("51278.45")),
        fixedAsset.getFixedAssetLineList().get(3));
  }

  @Test
  public void testGenerateAndComputeLinesUsProrataLinearFixedAssetFifthLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineUsProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2024, 12, 31),
            new BigDecimal("500.00"),
            new BigDecimal("14662.62"),
            new BigDecimal("66022.52"),
            new BigDecimal("36615.83")),
        fixedAsset.getFixedAssetLineList().get(4));
  }

  @Test
  public void testGenerateAndComputeLinesUsProrataLinearFixedAssetSixthLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineUsProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2025, 12, 31),
            new BigDecimal("500.00"),
            new BigDecimal("14662.62"),
            new BigDecimal("80685.14"),
            new BigDecimal("21953.21")),
        fixedAsset.getFixedAssetLineList().get(5));
  }

  @Test
  public void testGenerateAndComputeLinesUsProrataLinearFixedAssetSeventhLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineUsProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2026, 12, 31),
            new BigDecimal("102638.35"),
            new BigDecimal("14662.62"),
            new BigDecimal("95347.76"),
            new BigDecimal("7290.59")),
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
            new BigDecimal("2.25"),
            LocalDate.of(2021, 7, 1),
            LocalDate.of(2021, 12, 31),
            7,
            12,
            createFixedAssetCategoryFromIsProrataTemporis(true, false),
            new BigDecimal("102638.35"));
    when(fixedAssetDateService.computeLastDayOfPeriodicity(
            fixedAsset.getPeriodicityTypeSelect(), fixedAsset.getFirstServiceDate()))
        .thenReturn(LocalDate.of(2021, 12, 31));
    fixedAssetGenerationService.generateAndComputeLines(fixedAsset);
    return fixedAsset;
  }

  @Test
  public void testGenerateAndComputeLinesProrataDegressiveFixedAsset() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLinesProrataDegressiveFixedAsset();
    Assert.assertTrue(
        fixedAsset.getFixedAssetLineList() != null
            && fixedAsset.getFixedAssetLineList().size() == 7);
  }

  @Test
  public void testGenerateAndComputeLinesProrataDegressiveFixedAssetFirstLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLinesProrataDegressiveFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2021, 12, 31),
            new BigDecimal("102638.35"),
            new BigDecimal("16495.45"),
            new BigDecimal("16495.45"),
            new BigDecimal("86142.90")),
        fixedAsset.getFixedAssetLineList().get(0));
  }

  @Test
  public void testGenerateAndComputeLinesProrataDegressiveFixedAssetSecondLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLinesProrataDegressiveFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2022, 12, 31),
            new BigDecimal("102638.35"),
            new BigDecimal("27688.79"),
            new BigDecimal("44184.24"),
            new BigDecimal("58454.11")),
        fixedAsset.getFixedAssetLineList().get(1));
  }

  @Test
  public void testGenerateAndComputeLinesProrataDegressiveFixedAssetThirdLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLinesProrataDegressiveFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2023, 12, 31),
            new BigDecimal("102638.35"),
            new BigDecimal("18788.82"),
            new BigDecimal("62973.06"),
            new BigDecimal("39665.29")),
        fixedAsset.getFixedAssetLineList().get(2));
  }

  @Test
  public void testGenerateAndComputeLinesProrataDegressiveFixedAssetFourthLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLinesProrataDegressiveFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2024, 12, 31),
            new BigDecimal("102638.35"),
            new BigDecimal("12749.56"),
            new BigDecimal("75722.62"),
            new BigDecimal("26915.73")),
        fixedAsset.getFixedAssetLineList().get(3));
  }

  @Test
  public void testGenerateAndComputeLinesProrataDegressiveFixedAssetFifthLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLinesProrataDegressiveFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2025, 12, 31),
            new BigDecimal("102638.35"),
            new BigDecimal("8971.91"),
            new BigDecimal("84694.53"),
            new BigDecimal("17943.82")),
        fixedAsset.getFixedAssetLineList().get(4));
  }

  @Test
  public void testGenerateAndComputeLinesProrataDegressiveFixedAssetSixthLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLinesProrataDegressiveFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2026, 12, 31),
            new BigDecimal("102638.35"),
            new BigDecimal("8971.91"),
            new BigDecimal("93666.44"),
            new BigDecimal("8971.91")),
        fixedAsset.getFixedAssetLineList().get(5));
  }

  @Test
  public void testGenerateAndComputeLinesProrataDegressiveFixedAssetLastLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLinesProrataDegressiveFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2027, 12, 31),
            new BigDecimal("102638.35"),
            new BigDecimal("8971.91"),
            new BigDecimal("102638.35"),
            new BigDecimal("0.00")),
        fixedAsset.getFixedAssetLineList().get(6));
  }

  @Test
  public void testGenerateAndComputeLinesNoProrataDegressiveFixedAssetLastLine()
      throws AxelorException {
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
    when(fixedAssetDateService.computeLastDayOfPeriodicity(
            fixedAsset.getPeriodicityTypeSelect(), fixedAsset.getFirstServiceDate()))
        .thenReturn(LocalDate.of(2021, 12, 31));
    fixedAssetGenerationService.generateAndComputeLines(fixedAsset);
    Assert.assertEquals(
        LocalDate.of(2027, 12, 31),
        fixedAsset.getFixedAssetLineList().get(6).getDepreciationDate());
  }
}
