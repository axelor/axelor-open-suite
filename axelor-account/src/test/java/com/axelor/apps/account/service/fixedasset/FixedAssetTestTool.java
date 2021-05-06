package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetCategory;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.FixedAssetType;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.Assert;

public class FixedAssetTestTool {

  public static FixedAssetCategory createFixedAssetCategoryFromIsProrataTemporis(
      boolean isProrataTemporis) {
    return createFixedAssetCategoryFromIsProrataTemporis(isProrataTemporis, false);
  }

  public static FixedAssetCategory createFixedAssetCategoryFromIsProrataTemporis(
      boolean isProrataTemporis, boolean usProrataTemporis) {
    FixedAssetType fixedAssetType = new FixedAssetType();
    FixedAssetCategory fixedAssetCategory = new FixedAssetCategory();
    fixedAssetCategory.setFixedAssetType(fixedAssetType);
    fixedAssetCategory.setIsProrataTemporis(isProrataTemporis);
    fixedAssetCategory.setIsUSProrataTemporis(usProrataTemporis);
    return fixedAssetCategory;
  }

  public static FixedAssetLine createFixedAssetLine(
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

  public static FixedAsset createFixedAsset(
      String computationMethodSelect,
      LocalDate acquisitionDate,
      LocalDate firstDepreciationDate,
      int numberOfDepreciation,
      int periodicityInMonth,
      FixedAssetCategory fixedAssetCategory,
      BigDecimal grossValue) {
    FixedAsset fixedAsset = new FixedAsset();
    fixedAsset.setComputationMethodSelect(computationMethodSelect);
    fixedAsset.setFirstDepreciationDate(firstDepreciationDate);
    fixedAsset.setAcquisitionDate(acquisitionDate);
    fixedAsset.setNumberOfDepreciation(numberOfDepreciation);
    fixedAsset.setPeriodicityInMonth(periodicityInMonth);
    fixedAsset.setDurationInMonth(numberOfDepreciation * periodicityInMonth);
    fixedAsset.setFixedAssetCategory(fixedAssetCategory);
    fixedAsset.setGrossValue(grossValue);

    return fixedAsset;
  }

  // Compare fields only if fields in expected fixed asset line are not null
  public static void assertFixedAssetLineEquals(
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
