package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;

public interface MoveCounterPartService {

  void generateCounterpartMoveLine(Move move) throws AxelorException;

  MoveLine createCounterpartMoveLine(Move move);
  
  void generateCounterpartAnalyticMoveLine(Move move, MoveLine counterpartMoveLine) throws AxelorException;
}
