package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.exception.AxelorException;
import java.time.LocalDate;
import java.util.Map;

public interface MoveReverseService {

  Move generateReverse(
      Move move,
      boolean isAutomaticReconcile,
      boolean isAutomaticAccounting,
      boolean isUnreconcileOriginalMove,
      LocalDate dateOfReversion)
      throws AxelorException;

  Move generateReverse(Move move, Map<String, Object> assistantMap) throws AxelorException;
}
