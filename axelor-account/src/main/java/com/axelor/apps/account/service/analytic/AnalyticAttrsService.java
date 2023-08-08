package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.AxelorException;
import java.util.Map;

public interface AnalyticAttrsService {

  void addAnalyticAxisAttrs(Move move, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException;
}
