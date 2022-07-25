/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;

public interface MoveLineControlService {

  /**
   * Method that control if accountingAccount of line is a valid or not.
   *
   * @param line
   * @throws AxelorException if line is not valid.
   */
  void controlAccountingAccount(MoveLine line) throws AxelorException;

  void validateMoveLine(MoveLine moveLine) throws AxelorException;

  boolean isInvoiceTermReadonly(MoveLine moveLine, User user);

  boolean displayInvoiceTermWarningMessage(MoveLine moveLine);

  Move setMoveLineDates(Move move) throws AxelorException;

  Move setMoveLineOriginDates(Move move) throws AxelorException;

  boolean canReconcile(MoveLine moveLine);
}
