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
package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.AxelorException;

public interface MoveLineControlService {

  /**
   * Method that control if accountingAccount of line is a valid or not.
   *
   * @param line
   * @throws AxelorException if line is not valid.
   */
  void controlAccountingAccount(MoveLine line) throws AxelorException;

  void validateMoveLine(MoveLine moveLine) throws AxelorException;

  boolean displayInvoiceTermWarningMessage(MoveLine moveLine);

  /**
   * Method that controls if moveLine.account.company is the same moveLine.move.company
   *
   * @param moveLine
   * @throws AxelorException
   */
  void checkAccountCompany(MoveLine moveLine) throws AxelorException;

  /**
   * Method that controls if moveLine.account.company is the same as moveLine.move.company
   *
   * @param moveLine
   * @throws AxelorException
   */
  void checkJournalCompany(MoveLine moveLine) throws AxelorException;

  boolean canReconcile(MoveLine moveLine);

  void checkPartner(MoveLine moveLine) throws AxelorException;

  void checkAccountAnalytic(Move move, MoveLine moveLine, Account account) throws AxelorException;
}
