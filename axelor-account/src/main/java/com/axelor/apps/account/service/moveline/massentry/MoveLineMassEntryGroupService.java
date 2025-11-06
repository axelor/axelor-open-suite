/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.base.AxelorException;
import java.time.LocalDate;
import java.util.Map;

public interface MoveLineMassEntryGroupService {

  MoveLineMassEntry initializeValues(MoveLineMassEntry moveLine, Move move) throws AxelorException;

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

  Map<String, Map<String, Object>> getPfpValidatorOnSelectAttrsMap(
      MoveLineMassEntry moveLine, Move move);

  Map<String, Object> getOriginDateOnChangeValuesMap(MoveLineMassEntry moveLine, Move move)
      throws AxelorException;

  Map<String, Map<String, Object>> getOriginDateOnChangeAttrsMap(
      MoveLineMassEntry moveLine, Move move);

  Map<String, Object> getPartnerOnChangeValuesMap(MoveLineMassEntry moveLine, Move move)
      throws AxelorException;

  Map<String, Map<String, Object>> getPartnerOnChangeAttrsMap(MoveLineMassEntry moveLine);

  Map<String, Object> getInputActionOnChangeValuesMap(MoveLineMassEntry moveLine, Move move);

  Map<String, Map<String, Object>> getInputActionOnChangeAttrsMap(
      boolean isCounterpartLine, MoveLineMassEntry moveLine);

  Map<String, Object> getTemporaryMoveNumberOnChangeValuesMap(
      MoveLineMassEntry moveLine, Move move);

  Map<String, Map<String, Object>> getTemporaryMoveNumberOnChangeAttrsMap(Move move);

  Map<String, Object> getAnalyticDistributionTemplateOnChangeValuesMap(
      MoveLineMassEntry moveLine, Move move) throws AxelorException;

  Map<String, Object> getAnalyticDistributionTemplateOnChangeLightValuesMap(
      MoveLineMassEntry moveLine) throws AxelorException;

  Map<String, Object> getAnalyticAxisOnChangeValuesMap(MoveLineMassEntry moveLine, Move move)
      throws AxelorException;
}
