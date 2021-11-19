package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.exception.AxelorException;

public interface MoveViewHelperService {

  String filterPartner(Move move);

  Move updateMoveLinesDateExcludeFromPeriodOnlyWithoutSave(Move move) throws AxelorException;
}
