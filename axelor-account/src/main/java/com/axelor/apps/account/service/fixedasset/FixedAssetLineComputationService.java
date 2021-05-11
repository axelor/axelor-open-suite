package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import com.axelor.apps.account.db.FixedAssetLine;

public interface FixedAssetLineComputationService {

  FixedAssetLine computeInitialPlannedFixedAssetLine(FixedAsset fixedAsset);

  FixedAssetLine computePlannedFixedAssetLine(
      FixedAsset fixedAsset, FixedAssetLine previousFixedAssetLine);
}
