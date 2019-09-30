/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;

public interface ReconcileService {

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Reconcile createReconcile(
      MoveLine debitMoveLine,
      MoveLine creditMoveLine,
      BigDecimal amount,
      boolean canBeZeroBalanceOk);

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Reconcile confirmReconcile(Reconcile reconcile, boolean updateInvoicePayments)
      throws AxelorException;

  public void reconcilePreconditions(Reconcile reconcile) throws AxelorException;

  public void updatePartnerAccountingSituation(Reconcile reconcile) throws AxelorException;

  public List<Partner> getPartners(Reconcile reconcile);

  public Reconcile reconcile(
      MoveLine debitMoveLine,
      MoveLine creditMoveLine,
      boolean canBeZeroBalanceOk,
      boolean updateInvoicePayments)
      throws AxelorException;

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void unreconcile(Reconcile reconcile) throws AxelorException;

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void canBeZeroBalance(Reconcile reconcile) throws AxelorException;

  public void balanceCredit(MoveLine creditMoveLine) throws AxelorException;

  public List<Reconcile> getReconciles(MoveLine moveLine);

  public static boolean isReconcilable(MoveLine acc1, MoveLine acc2) {
    return acc1.getAccount().getReconcileOk()
        && acc2.getAccount().getReconcileOk()
        && (acc1.getAccount() == acc2.getAccount()
            || acc1.getAccount().getCompatibleAccountSet().contains(acc2.getAccount()));
  }
}
