/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.repo.FixedAssetLineRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.service.AnalyticFixedAssetService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveLineService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.exception.AxelorException;
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
  protected FixedAssetDerogatoryLineService fixedAssetDerogatoryLineService;
  protected AnalyticFixedAssetService analyticFixedAssetService;
  protected FixedAssetLineRepository fixedAssetLineRepo;
  protected FixedAssetDerogatoryLineMoveService fixedAssetDerogatoryLineMoveService;
  protected SequenceService sequenceService;
  protected FixedAssetLineService fixedAssetLineService;

  /*
   * Prepare dependencies by mocking them
   */
  @Before
  public void prepare() {

    fixedAssetRepo = mock(FixedAssetRepository.class);
    fixedAssetLineRepo = mock(FixedAssetLineRepository.class);
    fixedAssetLineMoveService = mock(FixedAssetLineMoveService.class);
    moveLineService = mock(MoveLineService.class);
    accountConfigService = mock(AccountConfigService.class);
    fixedAssetDerogatoryLineService = mock(FixedAssetDerogatoryLineService.class);
    analyticFixedAssetService = mock(AnalyticFixedAssetService.class);
    fixedAssetDerogatoryLineMoveService = mock(FixedAssetDerogatoryLineMoveService.class);
    sequenceService = mock(SequenceService.class);
    fixedAssetLineService = mock(FixedAssetLineService.class);
    fixedAssetLineComputationService =
        new FixedAssetLineEconomicComputationServiceImpl(
            analyticFixedAssetService,
            fixedAssetDerogatoryLineService,
            fixedAssetDerogatoryLineMoveService);

    fixedAssetService =
        new FixedAssetServiceImpl(
            fixedAssetRepo,
            fixedAssetLineMoveService,
            fixedAssetLineComputationService,
            moveLineService,
            accountConfigService,
            fixedAssetDerogatoryLineService,
            analyticFixedAssetService,
            sequenceService,
            fixedAssetLineService);

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
    when(analyticFixedAssetService.computeFirstDepreciationDate(
            fixedAsset, fixedAsset.getFirstServiceDate()))
        .thenReturn(LocalDate.of(2020, 12, 31));
    fixedAssetService.generateAndComputeLines(fixedAsset);
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
    when(analyticFixedAssetService.computeFirstDepreciationDate(
            fixedAsset, fixedAsset.getFirstServiceDate()))
        .thenReturn(LocalDate.of(2020, 12, 31));
    fixedAssetService.generateAndComputeLines(fixedAsset);
    return fixedAsset;
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAssetNoProrataNeeded()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAssetNoProrataNeeded();
    Assert.assertTrue(
        fixedAsset.getFixedAssetLineList() != null
            && fixedAsset.getFixedAssetLineList().size() == 5);
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAssetNoProrataNeededFirstLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAssetNoProrataNeeded();
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
  public void testGenerateAndComputeLinesProrataLinearFixedAssetNoProrataNeededSecondLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAssetNoProrataNeeded();
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
  public void testGenerateAndComputeLinesProrataLinearFixedAssetNoProrataNeededThirdLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAssetNoProrataNeeded();
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
  public void testGenerateAndComputeLinesProrataLinearFixedAssetNoProrataNeededFourthLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAssetNoProrataNeeded();
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
  public void testGenerateAndComputeLinesProrataLinearFixedAssetNoProrataNeededFifthLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAssetNoProrataNeeded();
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
    when(analyticFixedAssetService.computeFirstDepreciationDate(
            fixedAsset, fixedAsset.getFirstServiceDate()))
        .thenReturn(LocalDate.of(2020, 12, 31));
    fixedAssetService.generateAndComputeLines(fixedAsset);
    return fixedAsset;
  }

  @Test
  public void testGenerateAndComputeLinesProrataLinearFixedAsset() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineProrataLinearFixedAsset();
    Assert.assertTrue(
        fixedAsset.getFixedAssetLineList() != null
            && fixedAsset.getFixedAssetLineList().size() == 5);
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
            new BigDecimal("176.11"),
            new BigDecimal("500.00"),
            new BigDecimal("0.00")),
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
    when(analyticFixedAssetService.computeFirstDepreciationDate(
            fixedAsset, fixedAsset.getFirstServiceDate()))
        .thenReturn(LocalDate.of(2020, 12, 31));
    fixedAssetService.generateAndComputeLines(fixedAsset);
    return fixedAsset;
  }

  @Test
  public void testGenerateAndComputeLinesUsProrataLinearFixedAsset() throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineUsProrataLinearFixedAsset();
    Assert.assertTrue(
        fixedAsset.getFixedAssetLineList() != null
            && fixedAsset.getFixedAssetLineList().size() == 7);
  }

  @Test
  public void testGenerateAndComputeLinesUsProrataLinearFixedAssetFirstLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineUsProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2020, 12, 31),
            new BigDecimal("500.00"),
            new BigDecimal("7331.31"),
            new BigDecimal("7331.31"),
            new BigDecimal("95307.04")),
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
            new BigDecimal("21993.93"),
            new BigDecimal("80644.42")),
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
            new BigDecimal("36656.55"),
            new BigDecimal("65981.80")),
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
            new BigDecimal("51319.17"),
            new BigDecimal("51319.18")),
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
            new BigDecimal("65981.79"),
            new BigDecimal("36656.56")),
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
            new BigDecimal("80644.41"),
            new BigDecimal("21993.94")),
        fixedAsset.getFixedAssetLineList().get(5));
  }

  @Test
  public void testGenerateAndComputeLinesUsProrataLinearFixedAssetSeventhLine()
      throws AxelorException {
    FixedAsset fixedAsset = generateAndComputeLineUsProrataLinearFixedAsset();
    assertFixedAssetLineEquals(
        createFixedAssetLine(
            LocalDate.of(2026, 12, 31),
            new BigDecimal("500.00"),
            new BigDecimal("21993.94"),
            new BigDecimal("102638.35"),
            new BigDecimal("0.00")),
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
    when(analyticFixedAssetService.computeFirstDepreciationDate(
            fixedAsset, fixedAsset.getFirstServiceDate()))
        .thenReturn(LocalDate.of(2021, 12, 31));
    fixedAssetService.generateAndComputeLines(fixedAsset);
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
    when(analyticFixedAssetService.computeFirstDepreciationDate(
            fixedAsset, fixedAsset.getFirstServiceDate()))
        .thenReturn(LocalDate.of(2021, 12, 31));
    fixedAssetService.generateAndComputeLines(fixedAsset);
    Assert.assertEquals(
        LocalDate.of(2027, 12, 31),
        fixedAsset.getFixedAssetLineList().get(6).getDepreciationDate());
  }
}
