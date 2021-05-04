package com.axelor.apps.account.service.fixedasset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetCategory;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.FixedAssetType;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineService;
import com.axelor.apps.account.service.fixedasset.FixedAssetServiceImpl;
import com.axelor.apps.account.service.move.MoveLineService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;

public class TestFixedAssetService {

  protected FixedAssetServiceImpl fixedAssetService;
  protected FixedAssetRepository fixedAssetRepo;
  protected FixedAssetLineService fixedAssetLineService;
  protected MoveLineService moveLineService;
  protected AccountConfigService accountConfigService;

  /*
   * Prepare dependencies by mocking them
   */
  @Before
  public void prepare() {

    fixedAssetRepo = mock(FixedAssetRepository.class);
    fixedAssetLineService = mock(FixedAssetLineService.class);
    moveLineService = mock(MoveLineService.class);
    accountConfigService = mock(AccountConfigService.class);

    fixedAssetService =
        new FixedAssetServiceImpl(
            fixedAssetRepo, fixedAssetLineService, moveLineService, accountConfigService);

    prepareFixedAssetRepo();
  }

  protected FixedAsset generateAndComputeLineSimpleLinearFixedAsset() {
    FixedAsset fixedAsset =
        createFixedAsset(
            FixedAssetRepository.COMPUTATION_METHOD_LINEAR,
            LocalDate.of(2020, 10, 4),
            LocalDate.of(2020, 12, 31),
            5,
            12,
            60,
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

  protected FixedAsset generateAndComputeLineProrataLinearFixedAsset() {
    FixedAsset fixedAsset =
        createFixedAsset(
            FixedAssetRepository.COMPUTATION_METHOD_LINEAR,
            LocalDate.of(2020, 10, 4),
            LocalDate.of(2020, 12, 31),
            5,
            12,
            60,
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
            LocalDate.of(2025, 10, 3),
            new BigDecimal("76.11"),
            new BigDecimal("500.00"),
            new BigDecimal("0.00")),
        fixedAsset.getFixedAssetLineList().get(5));
  }

  protected void prepareFixedAssetRepo() {
    when(fixedAssetRepo.save(any(FixedAsset.class)))
        .then((Answer<FixedAsset>) invocation -> (FixedAsset) invocation.getArguments()[0]);
  }

  protected void prepareFixedAssetLineService() {}

  protected void prepareMoveLineService() {}

  protected void prepareAccountConfigService() {}

  protected FixedAssetCategory createFixedAssetCategoryFromIsProrataTemporis(
      boolean isProrataTemporis) {
    FixedAssetType fixedAssetType = new FixedAssetType();
    FixedAssetCategory fixedAssetCategory = new FixedAssetCategory();
    fixedAssetCategory.setFixedAssetType(fixedAssetType);
    fixedAssetCategory.setIsProrataTemporis(isProrataTemporis);
    return fixedAssetCategory;
  }

  protected FixedAssetLine createFixedAssetLine(
      LocalDate depreciationDate,
      BigDecimal depreciation,
      BigDecimal cumulativeDepreciation,
      BigDecimal residualValue) {
    FixedAssetLine fixedAssetLine = new FixedAssetLine();
    fixedAssetLine.setDepreciationDate(depreciationDate);
    fixedAssetLine.setDepreciation(depreciation);
    fixedAssetLine.setCumulativeDepreciation(cumulativeDepreciation);
    fixedAssetLine.setResidualValue(residualValue);
    return fixedAssetLine;
  }

  protected FixedAsset createFixedAsset(
      String computationMethodSelect,
      LocalDate acquisitionDate,
      LocalDate firstDepreciationDate,
      int numberOfDepreciation,
      int periodicityInMonth,
      int durationInMonth,
      FixedAssetCategory fixedAssetCategory,
      BigDecimal grossValue) {
    FixedAsset fixedAsset = new FixedAsset();
    fixedAsset.setComputationMethodSelect(computationMethodSelect);
    fixedAsset.setFirstDepreciationDate(firstDepreciationDate);
    fixedAsset.setAcquisitionDate(acquisitionDate);
    fixedAsset.setNumberOfDepreciation(numberOfDepreciation);
    fixedAsset.setPeriodicityInMonth(periodicityInMonth);
    fixedAsset.setDurationInMonth(durationInMonth);
    fixedAsset.setFixedAssetCategory(fixedAssetCategory);
    fixedAsset.setGrossValue(grossValue);

    return fixedAsset;
  }

  // Compare fields only if fields in expected fixed asset line are not null
  protected void assertFixedAssetLineEquals(
      FixedAssetLine expectedLine, FixedAssetLine actualLine) {
    if (expectedLine.getDepreciationDate() != null) {
      Assert.assertEquals(expectedLine.getDepreciationDate(), actualLine.getDepreciationDate());
    }
    if (expectedLine.getDepreciation() != null) {
      Assert.assertEquals(expectedLine.getDepreciation(), actualLine.getDepreciation());
    }
    if (expectedLine.getCumulativeDepreciation() != null) {
      Assert.assertEquals(
          expectedLine.getCumulativeDepreciation(), actualLine.getCumulativeDepreciation());
    }
    if (expectedLine.getResidualValue() != null) {
      Assert.assertEquals(expectedLine.getResidualValue(), actualLine.getResidualValue());
    }
  }
}
