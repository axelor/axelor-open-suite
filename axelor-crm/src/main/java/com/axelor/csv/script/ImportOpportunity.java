package com.axelor.csv.script;

import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.service.OpportunityService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import java.util.Map;

public class ImportOpportunity {
  public Object importOpportunity(Object bean, Map<String, Object> values) {
    assert bean instanceof Opportunity;

    Opportunity opportunity = (Opportunity) bean;

    try {
      Beans.get(OpportunityService.class).setSequence(opportunity);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }

    return opportunity;
  }
}
