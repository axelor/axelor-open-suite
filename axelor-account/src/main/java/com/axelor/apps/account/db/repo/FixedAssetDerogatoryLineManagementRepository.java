package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.FixedAssetDerogatoryLine;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.inject.Beans;
import java.util.Map;

public class FixedAssetDerogatoryLineManagementRepository
    extends FixedAssetDerogatoryLineRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    if (json != null && json.get("id") != null) {
      FixedAssetDerogatoryLine fixedAssetDerogatoryLine = this.find((Long) json.get("id"));
      json.put(
          "$currencyNumberOfDecimals",
          Beans.get(CurrencyScaleService.class)
              .getCompanyScale(fixedAssetDerogatoryLine.getFixedAsset()));
    }

    return super.populate(json, context);
  }
}
