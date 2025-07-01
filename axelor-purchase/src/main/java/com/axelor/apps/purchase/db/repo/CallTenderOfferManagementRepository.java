package com.axelor.apps.purchase.db.repo;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.inject.Beans;
import java.util.Map;

public class CallTenderOfferManagementRepository extends CallTenderOfferRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    json.put("$nbDecimalDigitForQty", Beans.get(AppBaseService.class).getNbDecimalDigitForQty());
    json.put(
        "$nbDecimalDigitForUnitPrice",
        Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice());
    return super.populate(json, context);
  }
}
