package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Move;

public interface MoveInvoiceTermService {
  public void generateInvoiceTerms(Move move);

  void roundInvoiceTermPercentages(Move move);
}
