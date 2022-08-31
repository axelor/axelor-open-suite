package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetCategory;
import com.axelor.apps.account.db.repo.FixedAssetCategoryRepository;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class FixedAssetDateServiceImpl implements FixedAssetDateService {

  /**
   * If firstDepreciationDateInitSeelct if acquisition Date THEN : -If PeriodicityTypeSelect = 12
   * (Year) >> FirstDepreciationDate = au 31/12 of the year of fixedAsset.acquisitionDate -if
   * PeriodicityTypeSelect = 1 (Month) >> FirstDepreciationDate = last day of the month of
   * fixedAsset.acquisitionDate Else (== first service date) -If PeriodicityTypeSelect = 12 (Year)
   * >> FirstDepreciationDate = au 31/12 of the year of fixedAsset.firstServiceDate -if
   * PeriodicityTypeSelect = 1 (Month) >> FirstDepreciationDate = last day of the month of
   * fixedAsset.firstServiceDate
   */
  @Override
  public void computeFirstDepreciationDate(FixedAsset fixedAsset) {

    FixedAssetCategory fixedAssetCategory = fixedAsset.getFixedAssetCategory();
    if (fixedAssetCategory == null) {
      return;
    }
    Integer periodicityTypeSelect = fixedAsset.getPeriodicityTypeSelect();
    Integer firstDepreciationDateInitSelect =
        fixedAssetCategory.getFirstDepreciationDateInitSelect();
    if (fixedAssetCategory != null
        && periodicityTypeSelect != null
        && firstDepreciationDateInitSelect != null) {
      if (firstDepreciationDateInitSelect
              == FixedAssetCategoryRepository.REFERENCE_FIRST_DEPRECIATION_DATE_ACQUISITION
          || fixedAsset.getFirstServiceDate() == null) {
        fixedAsset.setFirstDepreciationDate(
            this.computeLastDayOfPeriodicity(fixedAsset, fixedAsset.getAcquisitionDate()));
      } else {
        fixedAsset.setFirstDepreciationDate(
            this.computeLastDayOfPeriodicity(fixedAsset, fixedAsset.getFirstServiceDate()));
      }
    }
  }

  @Override
  public LocalDate computeLastDayOfPeriodicity(FixedAsset fixedAsset, LocalDate date) {
    if (fixedAsset.getPeriodicityTypeSelect() == null || date == null) {
      return date;
    }
    if (fixedAsset.getPeriodicityTypeSelect() == FixedAssetRepository.PERIODICITY_TYPE_YEAR) {
      return LocalDate.of(date.getYear(), 12, 31);
    }
    if (fixedAsset.getPeriodicityTypeSelect() == FixedAssetRepository.PERIODICITY_TYPE_MONTH) {
      return date.with(TemporalAdjusters.lastDayOfMonth());
    }
    return date;
  }
}
