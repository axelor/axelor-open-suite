package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.exception.AxelorException;

public interface MoveLineTaxService {

  void autoTaxLineGenerate(Move move) throws AxelorException;

  MoveLine computeTaxAmount(MoveLine moveLine) throws AxelorException;

  MoveLine reverseTaxPaymentMoveLines(MoveLine customerMoveLine, Reconcile reconcile)
      throws AxelorException;

  MoveLine generateTaxPaymentMoveLineList(
      MoveLine customerMoveLine, Invoice invoice, Reconcile reconcile) throws AxelorException;
}
