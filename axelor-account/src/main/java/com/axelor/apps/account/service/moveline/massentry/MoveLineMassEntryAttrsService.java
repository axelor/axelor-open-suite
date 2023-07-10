package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.JournalType;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import java.util.Map;

public interface MoveLineMassEntryAttrsService {

  void addCutOffReadonly(Account account, Map<String, Map<String, Object>> attrsMap);

  void addMovePaymentModeReadOnly(Map<String, Map<String, Object>> attrsMap);

  void addInputActionSelectionIn(Move move, Map<String, Map<String, Object>> attrsMap);

  void addDebitCreditFocus(
      Account account, boolean isOtherCurrency, Map<String, Map<String, Object>> attrsMap);

  void addPartnerBankDetailsReadOnly(
      MoveLineMassEntry moveLine, Map<String, Map<String, Object>> attrsMap);

  void addMovePfpValidatorUserReadOnly(
      MoveLineMassEntry moveLine, Map<String, Map<String, Object>> attrsMap);

  void addMovePfpValidatorUserRequired(
      Account account, Journal journal, Map<String, Map<String, Object>> attrsMap);

  void addTemporaryMoveNumberFocus(Move move, Map<String, Map<String, Object>> attrsMap);

  void addMovePaymentConditionRequired(
      JournalType journalType, Map<String, Map<String, Object>> attrsMap);

  void addOriginRequired(
      MoveLineMassEntry moveLine, Journal journal, Map<String, Map<String, Object>> attrsMap);

  void addPfpValidatorUserDomain(
      Partner partner, Company company, Map<String, Map<String, Object>> attrsMap);

  void addReadonly(
      boolean isCounterPartLine, Account account, Map<String, Map<String, Object>> attrsMap);

  void addRequired(boolean isCounterPartLine, Map<String, Map<String, Object>> attrsMap);

  void addInputActionReadonly(boolean readonly, Map<String, Map<String, Object>> attrsMap);

  void addTemporaryMoveNumberFocus(Map<String, Map<String, Object>> attrsMap);
}
