package com.axelor.apps.hr.db.repo;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.service.PayrollPreparationService;
import com.google.inject.Inject;
import java.util.Map;

public class PayrollPreparationHRRepository extends PayrollPreparationRepository {

  protected PayrollPreparationService payrollPreparationService;

  @Inject
  public PayrollPreparationHRRepository(PayrollPreparationService payrollPreparationService) {
    this.payrollPreparationService = payrollPreparationService;
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {

    try {
      Long id = (Long) json.get("id");
      if (id != null) {
        json.put("$payrollLeavesList", payrollPreparationService.fillInLeaves(find(id)));
      }
    } catch (AxelorException e) {
      TraceBackService.trace(e);
    }

    return super.populate(json, context);
  }
}
