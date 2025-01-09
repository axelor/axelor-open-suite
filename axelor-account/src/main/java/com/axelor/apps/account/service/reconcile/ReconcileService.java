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
package com.axelor.apps.account.service.reconcile;

import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.base.AxelorException;
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

  /**
   * Procédure permettant de gérer les écarts de règlement, check sur la case à cocher 'Peut être
   * soldé' Alors nous utilisons la règle de gestion consitant à imputer l'écart sur un compte
   * transitoire si le seuil est respecté
   *
   * @param reconcile Une reconciliation
   * @throws AxelorException
   */
  public void canBeZeroBalance(MoveLine debitMoveLine, MoveLine creditMoveLine)
      throws AxelorException;

  public void balanceCredit(MoveLine creditMoveLine) throws AxelorException;

  public List<Reconcile> getReconciles(MoveLine moveLine);

  public static boolean isReconcilable(MoveLine acc1, MoveLine acc2) {
    return acc1.getAccount().getReconcileOk()
        && acc2.getAccount().getReconcileOk()
        && (acc1.getAccount().equals(acc2.getAccount())
            || acc1.getAccount().getCompatibleAccountSet().contains(acc2.getAccount()));
  }

  String getStringAllowedCreditMoveLines(Reconcile reconcile);

  String getStringAllowedDebitMoveLines(Reconcile reconcile);

  List<Long> getAllowedCreditMoveLines(Reconcile reconcile);

  List<Long> getAllowedDebitMoveLines(Reconcile reconcile);
}
