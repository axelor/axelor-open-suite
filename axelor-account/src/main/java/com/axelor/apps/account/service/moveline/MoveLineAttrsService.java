package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;
import java.util.Map;

public interface MoveLineAttrsService {
  void addAnalyticAxisAttrs(Move move, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException;

  void addDescriptionRequired(Move move, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException;

  void addAnalyticAccountRequired(
      MoveLine moveLine, Move move, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException;
}
