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
package com.axelor.apps.account.service.move.control;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.service.move.record.model.MoveContext;
import com.axelor.apps.base.AxelorException;

public interface MoveCheckService {

  /**
   * Method that checks if there are any related cutoff moves to move.
   *
   * @param move
   * @return true if there is any, else false
   */
  boolean isRelatedCutoffMoves(Move move);

  void checkPeriodPermission(Move move) throws AxelorException;

  void checkDates(Move move) throws AxelorException;

  void checkRemovedLines(Move move) throws AxelorException;

  void checkAnalyticAccount(Move move) throws AxelorException;

  void checkPartnerCompatible(Move move) throws AxelorException;

  void checkDuplicatedMoveOrigin(Move move) throws AxelorException;

  void checkOrigin(Move move) throws AxelorException;

  MoveContext checkTermsInPayment(Move move) throws AxelorException;
}
