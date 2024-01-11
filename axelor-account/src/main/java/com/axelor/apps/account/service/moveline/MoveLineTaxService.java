/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.base.AxelorException;
import java.util.List;

public interface MoveLineTaxService {

  void autoTaxLineGenerate(Move move, Account account, boolean percentMoveTemplate)
      throws AxelorException;

  void autoTaxLineGenerateNoSave(Move move) throws AxelorException;

  /**
   * Same as method 'autoTaxLineGenerate' but this method will not save the move.
   *
   * @param move
   * @throws AxelorException
   */
  void autoTaxLineGenerateNoSave(Move move, Account account, boolean percentMoveTemplate)
      throws AxelorException;

  MoveLine computeTaxAmount(MoveLine moveLine) throws AxelorException;

  MoveLine reverseTaxPaymentMoveLines(MoveLine customerMoveLine, Reconcile reconcile)
      throws AxelorException;

  MoveLine generateTaxPaymentMoveLineList(
      MoveLine customerPaymentMoveLine, MoveLine invoiceMoveLine, Reconcile reconcile)
      throws AxelorException;

  int getVatSystem(Move move, MoveLine moveline) throws AxelorException;

  void checkDuplicateTaxMoveLines(Move move) throws AxelorException;

  void checkEmptyTaxLines(List<MoveLine> moveLineList) throws AxelorException;

  boolean isMoveLineTaxAccountRequired(MoveLine moveLine, int functionalOriginSelect);
}
