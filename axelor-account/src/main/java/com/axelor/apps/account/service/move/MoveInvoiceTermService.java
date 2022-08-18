package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;
import com.axelor.exception.AxelorException;

public interface MoveInvoiceTermService {
  public void generateInvoiceTerms(Move move) throws AxelorException;

  void roundInvoiceTermPercentages(Move move);

  boolean updateInvoiceTerms(Move move);

  void recreateInvoiceTerms(Move move) throws AxelorException;

  void updateMoveLineDueDates(Move move);
}
