package com.axelor.apps.account.service.period;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Period;
import com.axelor.auth.db.User;
import com.axelor.meta.CallMethod;

public interface PeriodCheckService {

  @CallMethod
  boolean isAuthorizedToAccountOnPeriod(Period period, User user) throws AxelorException;

  @CallMethod
  boolean isAuthorizedToAccountOnPeriod(Move move, User user) throws AxelorException;
}
