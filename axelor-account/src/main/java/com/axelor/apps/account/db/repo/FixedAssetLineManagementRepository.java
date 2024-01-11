package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.FixedAssetLine;
import com.axelor.apps.account.service.CurrencyScaleServiceAccount;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import java.util.Map;

public class FixedAssetLineManagementRepository extends FixedAssetLineRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    if (json != null && json.get("id") != null) {
      FixedAssetLine fixedAssetLine =
          Beans.get(FixedAssetLineRepository.class).find((Long) json.get("id"));
      try {
        json.put(
            "$currencyNumberOfDecimals",
            Beans.get(CurrencyScaleServiceAccount.class).getCompanyScale(fixedAssetLine));
      } catch (AxelorException e) {
        TraceBackService.trace(e);
      }
    }

    return super.populate(json, context);
  }
}
