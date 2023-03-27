package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;
import java.util.Map;

public interface MoveLineGroupService {
  Map<String, Object> getOnNewValuesMap(MoveLine moveLine, Move move) throws AxelorException;

  Map<String, Map<String, Object>> getOnNewAttrsMap(MoveLine moveLine, Move move)
      throws AxelorException;

  Map<String, Object> getAnalyticDistributionTemplateOnChangeValuesMap(MoveLine moveLine, Move move)
      throws AxelorException;

  Map<String, Map<String, Object>> getAnalyticDistributionTemplateOnChangeAttrsMap(
      MoveLine moveLine, Move move) throws AxelorException;

  Map<String, Object> getDebitCreditOnChangeValuesMap(MoveLine moveLine, Move move)
          throws AxelorException;
}
