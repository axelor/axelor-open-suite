package com.axelor.apps.account.service.reconcile.foreignexchange;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;

public class ForeignMoveToReconcile {

  private Move move;
  private MoveLine debitMoveLine;
  private MoveLine creditMoveLine;
  private boolean updateInvoiceTerms;

  public ForeignMoveToReconcile(
      Move move, MoveLine debitMoveLine, MoveLine creditMoveLine, boolean updateInvoiceTerms) {
    this.move = move;
    this.debitMoveLine = debitMoveLine;
    this.creditMoveLine = creditMoveLine;
    this.updateInvoiceTerms = updateInvoiceTerms;
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

  public boolean getUpdateInvoiceTerms() {
    return updateInvoiceTerms;
  }
}
