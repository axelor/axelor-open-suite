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
package com.axelor.apps.account.service.reconcile.foreignexchange;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;

public class ForeignMoveToReconcile {

  private Move move;
  private MoveLine debitMoveLine;
  private MoveLine creditMoveLine;
  private boolean updateInvoiceTerms;

  public ForeignMoveToReconcile(
      Move move, MoveLine debitMoveLine, MoveLine creditMoveLine, boolean updateInvoiceTerms) {
    this.move = move;
    this.debitMoveLine = debitMoveLine;
    this.creditMoveLine = creditMoveLine;
    this.updateInvoiceTerms = updateInvoiceTerms;
  }

  public Move getMove() {
    return move;
  }

  public MoveLine getDebitMoveLine() {
    return debitMoveLine;
  }

  public MoveLine getCreditMoveLine() {
    return creditMoveLine;
  }

  public boolean getUpdateInvoiceTerms() {
    return updateInvoiceTerms;
  }
}
