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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.base.AxelorException;
import java.util.List;

public interface InvoiceTermReplaceService {

  /**
   * This function replace invoice terms from a move to another when changing the invoice's move in
   * a process.
   *
   * @param invoice, move (the new move), invoiceMoveLineList (the old move movelinelist),
   *     partnerAccount for the reconcile
   */
  void replaceInvoiceTerms(
      Invoice invoice, Move move, List<MoveLine> invoiceMoveLineList, Account partnerAccount)
      throws AxelorException;

  void replaceInvoiceTerms(
      Invoice invoice,
      List<InvoiceTerm> newInvoiceTermList,
      List<InvoiceTerm> invoiceTermListToRemove);
}
