package com.axelor.apps.account.service;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.base.AxelorException;

public interface FindFixedAssetService {
  /**
   * Get Fixed asset of fixedAssetLine.
   *
   * @param fixedAssetLine
   * @return fixedAsset : {@link FixedAsset}
   */
  FixedAsset getFixedAsset(FixedAssetLine fixedAssetLine) throws AxelorException;
}
