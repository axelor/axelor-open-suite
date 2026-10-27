/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.reconcile;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.base.AxelorException;
import java.math.BigDecimal;
import java.util.List;

public interface ReconcileInvoiceTermComputationService {

  void updatePayments(Reconcile reconcile, boolean updateInvoiceTerms) throws AxelorException;

  void updatePayment(
      Reconcile reconcile,
      MoveLine moveLine,
      MoveLine otherMoveLine,
      Invoice invoice,
      Move move,
      Move otherMove,
      BigDecimal amount,
      boolean updateInvoiceTerms)
      throws AxelorException;

  /**
   * Currency amount remaining on the move line, computed from its confirmed reconciles with move
   * lines in its currency. Returns null when the move line is also settled in another currency than
   * through a foreign exchange gap move, as that part has no currency amount.
   */
  BigDecimal getCurrencyAmountRemaining(MoveLine moveLine);

  List<InvoiceTerm> getInvoiceTermsToPay(Invoice invoice, Move move, MoveLine moveLine)
      throws AxelorException;
}
