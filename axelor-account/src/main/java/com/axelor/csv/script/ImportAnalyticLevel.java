package com.axelor.csv.script;

import com.axelor.apps.account.db.AnalyticLevel;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import java.util.Map;

public class ImportAnalyticLevel {

  public Object importAnalyticLevel(Object bean, Map<String, Object> values)
      throws AxelorException {
    assert bean instanceof AnalyticLevel;
    AnalyticLevel analyticLevel = (AnalyticLevel) bean;
    if (ObjectUtils.isEmpty(analyticLevel.getName())) {
      analyticLevel.setName(analyticLevel.getNbr().toString());
    }
    return analyticLevel;
  }
}
