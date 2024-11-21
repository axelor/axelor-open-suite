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
import com.axelor.apps.account.db.FixedAssetCategory;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.db.FixedAssetType;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Assertions;

class FixedAssetTestTool {

  static FixedAssetCategory createFixedAssetCategoryFromIsProrataTemporis(
      boolean isProrataTemporis) {
    return createFixedAssetCategoryFromIsProrataTemporis(isProrataTemporis, false);
  }

  static FixedAssetCategory createFixedAssetCategoryFromIsProrataTemporis(
      boolean isProrataTemporis, boolean usProrataTemporis) {
    FixedAssetType fixedAssetType = new FixedAssetType();
    FixedAssetCategory fixedAssetCategory = new FixedAssetCategory();
    fixedAssetCategory.setFixedAssetType(fixedAssetType);
    fixedAssetCategory.setIsProrataTemporis(isProrataTemporis);
    fixedAssetCategory.setIsUSProrataTemporis(usProrataTemporis);
    return fixedAssetCategory;
  }

  static FixedAssetLine createFixedAssetLine(
      LocalDate depreciationDate,
      BigDecimal depreciationBase,
      BigDecimal depreciation,
      BigDecimal cumulativeDepreciation,
      BigDecimal accountingValue) {
    FixedAssetLine fixedAssetLine = new FixedAssetLine();
    fixedAssetLine.setDepreciationBase(depreciationBase);
    fixedAssetLine.setDepreciationDate(depreciationDate);
    fixedAssetLine.setDepreciation(depreciation);
    fixedAssetLine.setCumulativeDepreciation(cumulativeDepreciation);
    fixedAssetLine.setAccountingValue(accountingValue);
    return fixedAssetLine;
  }

  static FixedAsset createFixedAsset(
      String computationMethodSelect,
      LocalDate acquisitionDate,
      LocalDate firstDepreciationDate,
      int numberOfDepreciation,
      int periodicityInMonth,
      FixedAssetCategory fixedAssetCategory,
      BigDecimal grossValue) {

    return createFixedAsset(
        computationMethodSelect,
        BigDecimal.ZERO,
        acquisitionDate,
        firstDepreciationDate,
        numberOfDepreciation,
        periodicityInMonth,
        fixedAssetCategory,
        grossValue);
  }

  static FixedAsset createFixedAsset(
      String computationMethodSelect,
      BigDecimal degressiveCoef,
      LocalDate acquisitionDate,
      LocalDate firstDepreciationDate,
      int numberOfDepreciation,
      int periodicityInMonth,
      FixedAssetCategory fixedAssetCategory,
      BigDecimal grossValue) {
    FixedAsset fixedAsset = new FixedAsset();
    fixedAsset.setComputationMethodSelect(computationMethodSelect);
    fixedAsset.setDegressiveCoef(degressiveCoef);
    fixedAsset.setFirstDepreciationDate(firstDepreciationDate);
    fixedAsset.setAcquisitionDate(acquisitionDate);
    fixedAsset.setFirstServiceDate(acquisitionDate);
    fixedAsset.setNumberOfDepreciation(numberOfDepreciation);
    fixedAsset.setPeriodicityInMonth(periodicityInMonth);
    fixedAsset.setDurationInMonth(numberOfDepreciation * periodicityInMonth);
    fixedAsset.setFixedAssetCategory(fixedAssetCategory);
    fixedAsset.setGrossValue(grossValue);
    fixedAsset.setResidualValue(BigDecimal.ZERO);
    fixedAsset.setDepreciationPlanSelect(FixedAssetRepository.DEPRECIATION_PLAN_ECONOMIC);

    return fixedAsset;
  }

  // Compare fields only if fields in expected fixed asset line are not null
  static void assertFixedAssetLineEquals(FixedAssetLine expectedLine, FixedAssetLine actualLine) {
    if (expectedLine.getDepreciationDate() != null) {
      Assertions.assertEquals(expectedLine.getDepreciationDate(), actualLine.getDepreciationDate());
    }
    if (expectedLine.getDepreciation() != null) {
      Assertions.assertEquals(expectedLine.getDepreciation(), actualLine.getDepreciation());
    }
    if (expectedLine.getCumulativeDepreciation() != null) {
      Assertions.assertEquals(
          expectedLine.getCumulativeDepreciation(), actualLine.getCumulativeDepreciation());
    }
    if (expectedLine.getAccountingValue() != null) {
      Assertions.assertEquals(expectedLine.getAccountingValue(), actualLine.getAccountingValue());
    }
  }
}
