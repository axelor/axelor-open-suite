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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import java.math.BigDecimal;
import java.util.List;

public interface ReconcileService {

  public Reconcile createReconcile(
      MoveLine debitMoveLine,
      MoveLine creditMoveLine,
      BigDecimal amount,
      boolean canBeZeroBalanceOk)
      throws AxelorException;

  public Reconcile confirmReconcile(
      Reconcile reconcile, boolean updateInvoicePayments, boolean updateInvoiceTerms)
      throws AxelorException;

  public void reconcilePreconditions(
      Reconcile reconcile, boolean updateInvoicePayments, boolean updateInvoiceTerm)
      throws AxelorException;

  public void updatePartnerAccountingSituation(Reconcile reconcile) throws AxelorException;

  public List<Partner> getPartners(Reconcile reconcile);

  Reconcile reconcile(
      MoveLine debitMoveLine,
      MoveLine creditMoveLine,
      boolean canBeZeroBalanceOk,
      boolean updateInvoicePayments,
      InvoicePayment invoicePayment)
      throws AxelorException;

  public Reconcile reconcile(
      MoveLine debitMoveLine,
      MoveLine creditMoveLine,
      boolean canBeZeroBalanceOk,
      boolean updateInvoicePayments)
      throws AxelorException;

  Reconcile reconcile(
      MoveLine debitMoveLine,
      MoveLine creditMoveLine,
      InvoicePayment invoicePayment,
      boolean canBeZeroBalanceOk,
      boolean updateInvoicePayments)
      throws AxelorException;

  public void unreconcile(Reconcile reconcile) throws AxelorException;

  public void canBeZeroBalance(Reconcile reconcile) throws AxelorException;

  public void balanceCredit(MoveLine creditMoveLine) throws AxelorException;

  public List<Reconcile> getReconciles(MoveLine moveLine);

  /**
   * Add a reconcile to an existing or created reconcile group.
   *
   * @param reconcile a confirmed reconcile.
   */
  void addToReconcileGroup(Reconcile reconcile) throws AxelorException;

  public static boolean isReconcilable(MoveLine acc1, MoveLine acc2) {
    return acc1.getAccount().getReconcileOk()
        && acc2.getAccount().getReconcileOk()
        && (acc1.getAccount().equals(acc2.getAccount())
            || acc1.getAccount().getCompatibleAccountSet().contains(acc2.getAccount()));
  }

  public List<InvoiceTermPayment> updateInvoiceTerms(
      List<InvoiceTerm> invoiceTermList,
      InvoicePayment invoicePayment,
      BigDecimal amount,
      Reconcile reconcile)
      throws AxelorException;

  void checkReconcile(Reconcile reconcile) throws AxelorException;

  String getStringAllowedCreditMoveLines(Reconcile reconcile);

  String getStringAllowedDebitMoveLines(Reconcile reconcile);

  List<Long> getAllowedCreditMoveLines(Reconcile reconcile);

  List<Long> getAllowedDebitMoveLines(Reconcile reconcile);
}
