package com.axelor.apps.account.service.fixedasset;

import com.axelor.apps.account.db.FixedAsset;
import java.util.Map;

public interface FixedAssetGroupService {

  Map<String, Object> getDisposalWizardValuesMap(FixedAsset fixedAsset, int disposalTypeSelect);

  Map<String, Map<String, Object>> getDisposalWizardAttrsMap(
      int disposalTypeSelect, FixedAsset fixedAsset);
}
