package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.base.AxelorException;

public interface AnalyticMoveLineParentService {
  void refreshAxisOnParent(AnalyticMoveLine analyticMoveLine) throws AxelorException;
}
