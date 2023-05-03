package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Move;
import java.util.Map;

public interface MoveLineMassEntryAttrsService {

  void addCutOffReadOnly(Account account, Map<String, Map<String, Object>> attrsMap);

  void addMovePaymentModeReadOnly(Map<String, Map<String, Object>> attrsMap);

  void addInputActionSelectionIn(Move move, Map<String, Map<String, Object>> attrsMap);

  void addDebitCreditFocus(
      Account account, boolean isOtherCurrency, Map<String, Map<String, Object>> attrsMap);
}
