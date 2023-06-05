package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.base.AxelorException;
import java.time.LocalDate;
import java.util.Map;

public interface MoveLineMassEntryGroupService {

  Map<String, Object> getOnNewValuesMap(MoveLineMassEntry moveLine, Move move)
      throws AxelorException;

  Map<String, Map<String, Object>> getOnNewAttrsMap(MoveLineMassEntry moveLine, Move move)
      throws AxelorException;

  Map<String, Object> getDebitCreditOnChangeValuesMap(MoveLineMassEntry moveLine, Move move)
      throws AxelorException;

  Map<String, Object> getDebitOnChangeValuesMap(
      MoveLineMassEntry moveLine, Move move, LocalDate dueDate) throws AxelorException;

  Map<String, Object> getCreditOnChangeValuesMap(
      MoveLineMassEntry moveLine, Move move, LocalDate dueDate) throws AxelorException;

  Map<String, Object> getAccountOnChangeValuesMap(
      MoveLineMassEntry moveLine, Move move, LocalDate dueDate) throws AxelorException;

  Map<String, Map<String, Object>> getAccountOnChangeAttrsMap(MoveLineMassEntry moveLine, Move move)
      throws AxelorException;
}
