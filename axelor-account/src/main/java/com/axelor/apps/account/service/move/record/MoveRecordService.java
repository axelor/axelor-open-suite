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

public interface MoveRecordService {

  /**
   * Method called on action onNew. The move will be modified but not persisted, a Map of 'field,
   * value' will be returned that contains every modified value.
   *
   * @param move
   * @return a Object {@link MoveContext} that containts attrs and values context
   * @throws AxelorException
   */
  MoveContext onNew(Move move) throws AxelorException;

  /**
   * Method called on action onLoad. The move will be modified but not persisted, a Map of 'field,
   * value' will be returned that contains every modified value.
   *
   * @param move: can not be null
   * @return a Object {@link MoveContext} that containts attrs and values context
   * @throws AxelorException
   */
  MoveContext onLoad(Move move) throws AxelorException;

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

  MoveContext onChangeDate(Move move, boolean paymentConditionChange, boolean dateChange)
      throws AxelorException;

  MoveContext onChangeJournal(Move move) throws AxelorException;

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
