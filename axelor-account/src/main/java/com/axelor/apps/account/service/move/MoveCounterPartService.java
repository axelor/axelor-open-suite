package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;

public interface MoveCounterPartService {

  void generateCounterpartMoveLine(Move move);

  MoveLine createCounterpartMoveLine(Move move);
}
