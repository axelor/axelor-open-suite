package com.axelor.apps.hr.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.service.PayrollPreparationService;
import com.axelor.inject.Beans;
import java.util.Map;

public class PayrollPreparationHRRepository extends PayrollPreparationRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {

    try {
      Long id = (Long) json.get("id");
      if (id != null) {
        json.put(
            "$payrollLeavesList",
            Beans.get(PayrollPreparationService.class).fillInLeaves(find(id)));
      }
    } catch (AxelorException e) {
      TraceBackService.trace(e);
    }

    return super.populate(json, context);
  }
}
