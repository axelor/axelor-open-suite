package com.axelor.apps.account.service.reconcile;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;

public class ForeignMoveToReconcile {
  private Move move;
  private MoveLine debitMoveLine;
  private MoveLine creditMoveLine;
  private boolean isGain;

  public ForeignMoveToReconcile(
      Move move, MoveLine debitMoveLine, MoveLine creditMoveLine, boolean isGain) {
    this.move = move;
    this.debitMoveLine = debitMoveLine;
    this.creditMoveLine = creditMoveLine;
    this.isGain = isGain;
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

  public boolean getIsGain() {
    return isGain;
  }
}
