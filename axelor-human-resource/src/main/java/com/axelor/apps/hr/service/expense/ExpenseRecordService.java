package com.axelor.apps.hr.service.expense;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Expense;
import java.util.Map;

public interface ExpenseRecordService {
  Map<String, Object> computeDummyAmounts(Expense expense) throws AxelorException;
}
