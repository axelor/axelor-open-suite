package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import java.time.LocalDate;

public interface FixedAssetDateService {

  void computeFirstDepreciationDate(FixedAsset fixedAsset);

  /**
   * Compute and return the last day of the month/year depending on the periodicity of fixedAsset.
   *
   * @param fixedAsset
   * @param date
   */
  LocalDate computeLastDayOfPeriodicity(FixedAsset fixedAsset, LocalDate date);
}
