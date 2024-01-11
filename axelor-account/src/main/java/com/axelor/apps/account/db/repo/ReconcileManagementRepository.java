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
package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.ReconcileSequenceService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import java.math.BigDecimal;
import javax.persistence.PersistenceException;

public class ReconcileManagementRepository extends ReconcileRepository {

  @Override
  public Reconcile save(Reconcile reconcile) {
    try {

      Beans.get(ReconcileSequenceService.class).setDraftSequence(reconcile);

      return super.save(reconcile);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }

  @Override
  public Reconcile copy(Reconcile reconcile, boolean deep) {

    Reconcile copy = super.copy(reconcile, deep);

    copy.setCanBeZeroBalanceOk(false);
    copy.setMustBeZeroBalanceOk(false);
    copy.setReconcileSeq(null);
    copy.setStatusSelect(ReconcileRepository.STATUS_DRAFT);
    copy.setPaymentMoveLineDistributionList(null);

    MoveLine debitMoveLine = reconcile.getDebitMoveLine();
    MoveLine creditMoveLine = reconcile.getCreditMoveLine();

    BigDecimal debitAmountRemaining =
        debitMoveLine == null ? BigDecimal.ZERO : debitMoveLine.getAmountRemaining();
    BigDecimal creditAmountRemaining =
        creditMoveLine == null ? BigDecimal.ZERO : creditMoveLine.getAmountRemaining();

    if (debitAmountRemaining.compareTo(BigDecimal.ZERO) == 0) {
      copy.setDebitMoveLine(null);
    }
    if (creditAmountRemaining.compareTo(BigDecimal.ZERO) == 0) {
      copy.setCreditMoveLine(null);
    }

    copy.setAmount(debitAmountRemaining.min(creditAmountRemaining));
    copy.setReconciliationDateTime(null);
    copy.setReconciliationCancelDateTime(null);
    copy.setReconcileGroup(null);

    return copy;
  }

  @Override
  public void remove(Reconcile entity) {

    if (!entity.getStatusSelect().equals(ReconcileRepository.STATUS_DRAFT)) {
      try {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.RECONCILE_CAN_NOT_BE_REMOVE),
            entity.getReconcileSeq());
      } catch (AxelorException e) {
        throw new PersistenceException(e.getMessage(), e);
      }
    } else {
      super.remove(entity);
    }
  }
}
