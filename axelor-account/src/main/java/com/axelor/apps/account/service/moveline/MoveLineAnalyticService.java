package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.db.Company;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;

public interface MoveLineAnalyticService {

  public BigDecimal getAnalyticAmount(MoveLine moveLine, AnalyticMoveLine analyticMoveLine);

  public boolean checkManageAnalytic(Move move) throws AxelorException;

  MoveLine clearAnalyticAccounting(MoveLine moveLine);

  MoveLine printAnalyticAccount(MoveLine moveLine, Company company) throws AxelorException;

  MoveLine checkAnalyticMoveLineForAxis(MoveLine moveLine);

  boolean isAxisRequired(MoveLine moveLine, Move move, int position) throws AxelorException;
}
