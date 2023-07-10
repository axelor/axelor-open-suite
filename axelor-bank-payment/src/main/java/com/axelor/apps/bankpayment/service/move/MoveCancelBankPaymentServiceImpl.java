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
