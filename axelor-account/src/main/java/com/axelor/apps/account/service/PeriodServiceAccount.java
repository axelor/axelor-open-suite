package com.axelor.apps.account.service;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.db.Period;
import com.axelor.db.Query;

public interface PeriodServiceAccount {

  public Query<Move> getMoveListToValidateQuery(Period period);
}
