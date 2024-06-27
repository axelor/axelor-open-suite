package com.axelor.apps.account.service.analytic;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.AxelorException;
import java.util.Map;

public interface ImportAnalyticInMoveService {
  MoveLine fillAnalyticOnMoveLine(
      MoveLine moveLine, Move move, Map<String, Object> values, String csvReference)
      throws AxelorException;
}
