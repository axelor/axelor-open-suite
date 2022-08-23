package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.exception.AxelorException;
import java.time.LocalDate;

public interface MoveLineInvoiceTermService {
  void generateDefaultInvoiceTerm(
      MoveLine moveLine, LocalDate singleTermDueDate, boolean canCreateHolbackMoveLine)
      throws AxelorException;

  void generateDefaultInvoiceTerm(MoveLine moveLine, boolean canCreateHolbackMoveLine)
      throws AxelorException;

  void updateInvoiceTermsParentFields(MoveLine moveLine);

  void recreateInvoiceTerms(MoveLine moveLine) throws AxelorException;

  void setDueDateFromInvoiceTerms(MoveLine moveLine);
}
