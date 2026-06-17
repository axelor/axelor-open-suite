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
package com.axelor.apps.bankpayment.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.service.move.MoveReverseService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.reconcile.ReconcileService;
import com.axelor.apps.account.service.reconcile.UnreconcileService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MoveCancelBankPaymentServiceImpl implements MoveCancelBankPaymentService {
  protected AppBaseService appBaseService;
  protected ReconcileService reconcileService;
  protected MoveReverseService moveReverseService;
  protected MoveValidateService moveValidateService;
  protected UnreconcileService unReconcileService;

  @Inject
  public MoveCancelBankPaymentServiceImpl(
      AppBaseService appBaseService,
      ReconcileService reconcileService,
      MoveReverseService moveReverseService,
      MoveValidateService moveValidateService,
      UnreconcileService unReconcileService) {
    this.appBaseService = appBaseService;
    this.reconcileService = reconcileService;
    this.moveReverseService = moveReverseService;
    this.moveValidateService = moveValidateService;
    this.unReconcileService = unReconcileService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void cancelGeneratedMove(Move move) throws AxelorException {
    if (move == null) {
      return;
    }

    Move reverseMove =
        moveReverseService.generateReverse(
            move, false, false, false, appBaseService.getTodayDate(move.getCompany()));

    List<MoveLine> reverseMoveLineList = new ArrayList<>(reverseMove.getMoveLineList());

    moveValidateService.accounting(reverseMove);

    List<MoveLine> moveLineList = new ArrayList<>(move.getMoveLineList());
    for (int i = 0; i < moveLineList.size(); i++) {
      MoveLine reverseMoveLine = i < reverseMoveLineList.size() ? reverseMoveLineList.get(i) : null;
      this.cancelMoveLine(moveLineList.get(i), reverseMoveLine);
    }
  }

  protected void cancelMoveLine(MoveLine moveLine, MoveLine reverseMoveLine)
      throws AxelorException {
    for (Reconcile reconcile : moveLine.getDebitReconcileList()) {
      unReconcileService.unreconcile(reconcile);
      MoveLine creditLine = reconcile.getCreditMoveLine();
      if (creditLine != null) {
        creditLine.setAmountPaid(computeAmountPaidFromConfirmedReconciles(creditLine));
      }
    }

    for (Reconcile reconcile : moveLine.getCreditReconcileList()) {
      unReconcileService.unreconcile(reconcile);
      MoveLine debitLine = reconcile.getDebitMoveLine();
      if (debitLine != null) {
        debitLine.setAmountPaid(computeAmountPaidFromConfirmedReconciles(debitLine));
      }
    }

    moveLine.setAmountPaid(BigDecimal.ZERO);

    if (reverseMoveLine != null) {
      if (moveLine.getDebit().signum() > 0) {
        reconcileService.reconcile(moveLine, reverseMoveLine, false, false);
      }

      if (moveLine.getCredit().signum() > 0) {
        reconcileService.reconcile(reverseMoveLine, moveLine, false, false);
      }
    }
  }

  protected BigDecimal computeAmountPaidFromConfirmedReconciles(MoveLine moveLine) {
    BigDecimal amountPaid = BigDecimal.ZERO;
    if (moveLine.getDebitReconcileList() != null) {
      for (Reconcile reconcile : moveLine.getDebitReconcileList()) {
        if (reconcile.getStatusSelect() == ReconcileRepository.STATUS_CONFIRMED) {
          amountPaid = amountPaid.add(reconcile.getAmount());
        }
      }
    }
    if (moveLine.getCreditReconcileList() != null) {
      for (Reconcile reconcile : moveLine.getCreditReconcileList()) {
        if (reconcile.getStatusSelect() == ReconcileRepository.STATUS_CONFIRMED) {
          amountPaid = amountPaid.add(reconcile.getAmount());
        }
      }
    }
    return amountPaid;
  }
}
