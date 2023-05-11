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
package com.axelor.apps.account.service.move.record;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.service.move.record.model.MoveContext;
import com.axelor.apps.base.AxelorException;
import java.util.Map;

public interface MoveGroupService {

  Map<String, Object> getOnNewValuesMap(Move move) throws AxelorException;

  Map<String, Map<String, Object>> getOnNewAttrsMap(Move move) throws AxelorException;

  Map<String, Object> getOnLoadValuesMap(Move move) throws AxelorException;

  Map<String, Map<String, Object>> getOnLoadAttrsMap(Move move) throws AxelorException;

  /**
   * Method called on action onSave
   *
   * @param move: can not be null
   * @param paymentConditionChange
   * @param headerChange
   * @throws AxelorException
   */
  MoveContext onSaveBefore(Move move, boolean paymentConditionChange, boolean headerChange)
      throws AxelorException;

  /**
   * Method called on action onSave
   *
   * @param move: can not be null
   * @throws AxelorException
   */
  MoveContext onSaveAfter(Move move) throws AxelorException;

  MoveContext onSaveCheck(Move move) throws AxelorException;

  Map<String, Object> getDateOnChangeValuesMap(Move move, boolean paymentConditionChange)
      throws AxelorException;

  Map<String, Map<String, Object>> getDateOnChangeAttrsMap(
      Move move, boolean paymentConditionChange);

  Map<String, Object> getJournalOnChangeValuesMap(Move move);

  Map<String, Map<String, Object>> getJournalOnChangeAttrsMap(Move move);

  MoveContext onChangePartner(Move move, boolean paymentConditionChange, boolean dateChange)
      throws AxelorException;

  MoveContext onChangeMoveLineList(Move move, boolean paymentConditinoChange, boolean dateChange)
      throws AxelorException;

  MoveContext onChangeOriginDate(Move move, boolean paymentConditionChange, boolean dateChange)
      throws AxelorException;

  MoveContext onChangeOrigin(Move move) throws AxelorException;

  MoveContext onChangePaymentCondition(
      Move move, boolean paymentConditionChange, boolean dateChange, boolean headerChange)
      throws AxelorException;
}
