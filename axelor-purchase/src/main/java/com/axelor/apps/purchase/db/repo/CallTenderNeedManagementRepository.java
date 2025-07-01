package com.axelor.apps.purchase.db.repo;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.inject.Beans;
import java.util.Map;

public class CallTenderNeedManagementRepository extends CallTenderNeedRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    json.put("$nbDecimalDigitForQty", Beans.get(AppBaseService.class).getNbDecimalDigitForQty());

    return super.populate(json, context);
  }
}
