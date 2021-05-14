package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;

/** This service is used to compute new lines from an existing fixed asset header. */
public interface FixedAssetLineComputationService {

  /**
   * Compute the first fixed asset line from an empty fixed asset.
   *
   * @param fixedAsset a fixed asset with no lines
   * @return the created fixed asset line
   */
  FixedAssetLine computeInitialPlannedFixedAssetLine(FixedAsset fixedAsset);

  /**
   * Compute the next fixed asset line from a fixed asset and the previous line.
   *
   * @param fixedAsset a fixed asset with existing lines
   * @param previousFixedAssetLine the previous line
   * @return the created fixed asset line
   */
  FixedAssetLine computePlannedFixedAssetLine(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine);
}
