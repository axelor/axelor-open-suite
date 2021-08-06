package com.axelor.apps.account.service;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.repo.FixedAssetRepository;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class AnalyticFixedAssetServiceImpl implements AnalyticFixedAssetService {

  public LocalDate computeFirstDepreciationDate(FixedAsset fixedAsset, LocalDate date) {
    if (fixedAsset.getPeriodicityTypeSelect() == null) {
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
