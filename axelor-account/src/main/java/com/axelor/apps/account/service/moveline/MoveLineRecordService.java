package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;

public interface MoveLineRecordService {
  void setCurrencyFields(MoveLine moveLine, Move move) throws AxelorException;
}
