package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;

public interface MoveLineInvoiceTermService {
  public void generateDefaultInvoiceTerm(MoveLine moveLine, boolean canCreateHolbackMoveLine)
      throws AxelorException;

  void updateInvoiceTermsParentFields(MoveLine moveLine);

  void recreateInvoiceTerms(MoveLine moveLine) throws AxelorException;
}
