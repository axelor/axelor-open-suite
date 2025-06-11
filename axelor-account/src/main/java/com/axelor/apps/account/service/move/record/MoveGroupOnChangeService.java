package com.axelor.apps.account.service.move.record;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.base.AxelorException;
import java.util.Map;

public interface MoveGroupOnChangeService {
  Map<String, Object> getPaymentModeOnChangeValuesMap(Move move) throws AxelorException;

  Map<String, Map<String, Object>> getHeaderChangeAttrsMap();
}
