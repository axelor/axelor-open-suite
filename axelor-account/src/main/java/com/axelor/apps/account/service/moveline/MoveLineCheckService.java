package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface MoveLineCheckService {
  void checkAnalyticByTemplate(MoveLine moveLine) throws AxelorException;

  void checkAnalyticAxes(MoveLine moveLine) throws AxelorException;

  void checkDebitCredit(MoveLine moveLine) throws AxelorException;

  void checkDates(Move move) throws AxelorException;

  void checkAnalyticAccount(List<MoveLine> moveLineList) throws AxelorException;
}
