package com.axelor.apps.hr.service.expense;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Expense;

public interface ExpenseProofFileService {
  void convertProofFilesInPdf(Expense expense) throws AxelorException;
}
