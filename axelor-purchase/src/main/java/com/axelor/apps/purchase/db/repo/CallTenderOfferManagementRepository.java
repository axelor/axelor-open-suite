package com.axelor.apps.purchase.db.repo;

import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import java.util.Map;

public class CallTenderOfferManagementRepository extends CallTenderOfferRepository {

  protected final AppBaseService appBaseService;

  @Inject
  public CallTenderOfferManagementRepository(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    json.put("$nbDecimalDigitForQty", appBaseService.getNbDecimalDigitForQty());
    json.put("$nbDecimalDigitForUnitPrice", appBaseService.getNbDecimalDigitForUnitPrice());
    return super.populate(json, context);
  }
}
