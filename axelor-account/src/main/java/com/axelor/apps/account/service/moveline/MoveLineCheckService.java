package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;

public interface MoveLineCheckService {
  void checkAnalyticByTemplate(MoveLine moveLine) throws AxelorException;

  void checkAnalyticAxes(MoveLine moveLine) throws AxelorException;
}
