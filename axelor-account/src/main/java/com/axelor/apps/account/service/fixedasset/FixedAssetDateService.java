package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import java.time.LocalDate;

public interface FixedAssetDateService {

  /** Compute first depreciation date of economic, fiscal and ifrs plan */
  void computeFirstDepreciationDate(FixedAsset fixedAsset);

  /** Compute first depreciation date of fiscal */
  void computeFiscalFirstDepreciationDate(FixedAsset fixedAsset);

  /** Compute first depreciation date of economic */
  void computeEconomicFirstDepreciationDate(FixedAsset fixedAsset);

  /** Compute first depreciation date of ifrs plan */
  void computeIfrsFirstDepreciationDate(FixedAsset fixedAsset);

  /**
   * Compute and return the last day of the month/year depending on the periodicity type.
   *
   * @param fixedAsset
   * @param date
   */
  LocalDate computeLastDayOfPeriodicity(Integer periodicityType, LocalDate date);
}
