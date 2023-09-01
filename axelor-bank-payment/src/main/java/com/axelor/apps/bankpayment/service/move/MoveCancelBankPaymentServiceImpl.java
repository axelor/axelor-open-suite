/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.move.MoveReverseService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class MoveCancelBankPaymentServiceImpl implements MoveCancelBankPaymentService {
  protected AppBaseService appBaseService;
  protected ReconcileService reconcileService;
  protected MoveReverseService moveReverseService;
  protected MoveValidateService moveValidateService;

  @Inject
  public MoveCancelBankPaymentServiceImpl(
      AppBaseService appBaseService,
      ReconcileService reconcileService,
      MoveReverseService moveReverseService,
      MoveValidateService moveValidateService) {
    this.appBaseService = appBaseService;
    this.reconcileService = reconcileService;
    this.moveReverseService = moveReverseService;
    this.moveValidateService = moveValidateService;
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

    moveValidateService.accounting(reverseMove);

    for (MoveLine moveLine : move.getMoveLineList()) {
      this.cancelMoveLine(moveLine, reverseMove);
    }
  }

  protected void cancelMoveLine(MoveLine moveLine, Move reverseMove) throws AxelorException {
    for (Reconcile reconcile : moveLine.getDebitReconcileList()) {
      reconcileService.unreconcile(reconcile);
    }

    for (Reconcile reconcile : moveLine.getCreditReconcileList()) {
      reconcileService.unreconcile(reconcile);
    }

    MoveLine reverseMoveLine =
        reverseMove.getMoveLineList().stream()
            .filter(it -> it.getAccount().equals(moveLine.getAccount()))
            .findFirst()
            .orElse(null);

    if (reverseMoveLine != null) {
      if (moveLine.getDebit().signum() > 0) {
        reconcileService.reconcile(moveLine, reverseMoveLine, false, false);
      }

      if (moveLine.getCredit().signum() > 0) {
        reconcileService.reconcile(reverseMoveLine, moveLine, false, false);
      }
    }
  }
}
