package com.axelor.apps.account.service;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.meta.CallMethod;
import java.time.LocalDate;

public interface AnalyticFixedAssetService {

  @CallMethod
  public LocalDate computeFirstDepreciationDate(FixedAsset fixedAsset, LocalDate date);
}
