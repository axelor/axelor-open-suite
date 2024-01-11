package com.axelor.apps.account.service.fixedasset.attributes;

import com.axelor.apps.account.db.FixedAsset;
import java.util.Map;

public interface FixedAssetAttrsService {

  void addDisposalAmountTitle(int disposalTypeSelect, Map<String, Map<String, Object>> attrsMap);

  void addDisposalAmountReadonly(int disposalTypeSelect, Map<String, Map<String, Object>> attrsMap);

  void addDisposalAmountScale(FixedAsset fixedAsset, Map<String, Map<String, Object>> attrsMap);
}
