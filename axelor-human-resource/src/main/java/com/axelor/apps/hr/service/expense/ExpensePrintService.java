package com.axelor.apps.hr.service.expense;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Expense;
import com.axelor.dms.db.DMSFile;
import java.io.IOException;

public interface ExpensePrintService {

  String getExpenseReportTitle();

  DMSFile uploadExpenseReport(Expense expense) throws IOException, AxelorException;
}
