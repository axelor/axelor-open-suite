package com.axelor.apps.account.service;

import com.axelor.apps.account.db.FixedAsset;
import java.time.LocalDate;

public class AnalyticFixedAssetServiceImpl implements AnalyticFixedAssetService {

  public LocalDate computeFirstDepreciationDate(FixedAsset fixedAsset, LocalDate date) {
    if (fixedAsset.getFiscalPeriodicityTypeSelect() == null) {
      return date;
    }
    if (fixedAsset.getFiscalPeriodicityTypeSelect() == 2) {
      return LocalDate.of(date.getYear(), 12, 31);
    }
    if (fixedAsset.getFiscalPeriodicityTypeSelect() == 1) {
      int month = fixedAsset.getFiscalPeriodicityInMonth();
      if ((date.getMonthValue() + month) > 12) {
        return LocalDate.of(date.getYear() + 1, (date.getMonthValue() + month) % 12, 30);
      }
      return LocalDate.of(date.getYear(), (date.getMonthValue() + month), 30);
    }
    return date;
  }
}
