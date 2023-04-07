package com.axelor.apps.account.service.move.control;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface MoveLineCheckService {

  void checkDates(Move move) throws AxelorException;

  void checkAnalyticAccount(List<MoveLine> moveLineList) throws AxelorException;
}
