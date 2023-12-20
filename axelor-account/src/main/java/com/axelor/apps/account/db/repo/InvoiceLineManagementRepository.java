package com.axelor.apps.account.db.repo;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.inject.Beans;
import java.util.Map;

public class InvoiceLineManagementRepository extends InvoiceLineRepository {
  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    json.put(
        "$nbDecimalDigitForUnitPrice",
        Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice());

    return super.populate(json, context);
  }
}
