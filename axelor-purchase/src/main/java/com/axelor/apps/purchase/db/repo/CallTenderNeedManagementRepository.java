package com.axelor.apps.purchase.db.repo;

import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import java.util.Map;

public class CallTenderNeedManagementRepository extends CallTenderNeedRepository {

  protected final AppBaseService appBaseService;

  @Inject
  public CallTenderNeedManagementRepository(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    json.put("$nbDecimalDigitForQty", appBaseService.getNbDecimalDigitForQty());

    return super.populate(json, context);
  }
}
