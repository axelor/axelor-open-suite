/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.JournalType;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.base.AxelorException;
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
      Account account, Journal journal, Company company, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException;

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
