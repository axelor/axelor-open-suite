package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Move;
import com.axelor.exception.AxelorException;
import java.time.LocalDate;

public interface MoveLineCurrencyService {

  void computeNewCurrencyRateOnMoveLineList(Move move, LocalDate dueDate) throws AxelorException;
}
