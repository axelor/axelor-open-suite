package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;
import java.time.LocalDate;
import java.util.Map;

public interface MoveLineGroupService {
  Map<String, Object> getOnNewValuesMap(MoveLine moveLine, Move move) throws AxelorException;

  Map<String, Map<String, Object>> getOnNewAttrsMap(MoveLine moveLine, Move move)
      throws AxelorException;

  Map<String, Map<String, Object>> getOnLoadAttrsMap(MoveLine moveLine, Move move)
      throws AxelorException;

  Map<String, Map<String, Object>> getOnLoadMoveAttrsMap(MoveLine moveLine, Move move)
      throws AxelorException;

  Map<String, Object> getAnalyticDistributionTemplateOnChangeValuesMap(MoveLine moveLine, Move move)
      throws AxelorException;

  Map<String, Map<String, Object>> getAnalyticDistributionTemplateOnChangeAttrsMap(
      MoveLine moveLine, Move move) throws AxelorException;

  Map<String, Object> getDebitCreditOnChangeValuesMap(MoveLine moveLine, Move move)
      throws AxelorException;

  Map<String, Object> getAccountOnChangeValuesMap(
      MoveLine moveLine,
      Move move,
      LocalDate cutOffStartDate,
      LocalDate cutOffEndDate,
      LocalDate dueDate)
      throws AxelorException;

  Map<String, Map<String, Object>> getAccountOnChangeAttrsMap(MoveLine moveLine, Move move)
      throws AxelorException;

  Map<String, Object> getAnalyticAxisOnChangeValuesMap(MoveLine moveLine, Move move)
      throws AxelorException;

  Map<String, Map<String, Object>> getAnalyticAxisOnChangeAttrsMap(MoveLine moveLine, Move move)
      throws AxelorException;

  Map<String, Object> getDateOnChangeValuesMap(MoveLine moveLine, Move move) throws AxelorException;

  Map<String, Object> getCurrencyAmountRateOnChangeValuesMap(MoveLine moveLine, LocalDate dueDate)
      throws AxelorException;

  Map<String, Map<String, Object>> getAccountOnSelectAttrsMap(Move move);

  Map<String, Map<String, Object>> getPartnerOnSelectAttrsMap(Move move);

  Map<String, Map<String, Object>> getAnalyticDistributionTemplateOnSelectAttrsMap(Move move);
}
