package com.axelor.csv.script;

import com.axelor.apps.account.db.AnalyticGrouping;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import java.util.Map;

public class ImportAnalyticGrouping {

  public Object importAnalyticGrouping(Object bean, Map<String, Object> values)
      throws AxelorException {
    assert bean instanceof AnalyticGrouping;
    AnalyticGrouping analyticGrouping = (AnalyticGrouping) bean;
    if (ObjectUtils.isEmpty(analyticGrouping.getName())) {
      analyticGrouping.setFullName(analyticGrouping.getCode() + "_" + analyticGrouping.getName());
    }
    return analyticGrouping;
  }
}
