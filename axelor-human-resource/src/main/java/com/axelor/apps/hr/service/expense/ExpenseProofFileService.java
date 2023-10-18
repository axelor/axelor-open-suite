package com.axelor.apps.hr.service.expense;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PfxCertificate;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;

public interface ExpenseProofFileService {
  void convertProofFilesInPdf(Expense expense) throws AxelorException;

  void convertProofFileToPdf(PfxCertificate pfxCertificate, ExpenseLine expenseLine)
      throws AxelorException;
}
