package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.service.fixedasset.FixedAssetLineToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.inject.Beans;
import java.util.Map;

public class FixedAssetLineManagementRepository extends FixedAssetLineRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    if (json != null && json.get("id") != null) {
      FixedAssetLine fixedAssetLine = this.find((Long) json.get("id"));
      try {
        json.put(
            "$currencyNumberOfDecimals",
            Beans.get(FixedAssetLineToolService.class).getCompanyScale(fixedAssetLine));
      } catch (AxelorException e) {
        throw new RuntimeException(e);
      }
    }

    return super.populate(json, context);
  }
}
