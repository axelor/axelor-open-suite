package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.MoveLine;

public interface MoveLineInvoiceTermService {
  public void generateDefaultInvoiceTerm(MoveLine moveLine);

  void updateInvoiceTermsParentFields(MoveLine moveLine);;
}
