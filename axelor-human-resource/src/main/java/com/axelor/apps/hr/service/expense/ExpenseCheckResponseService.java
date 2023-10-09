package com.axelor.apps.hr.service.expense;

import com.axelor.apps.base.rest.dto.CheckResponse;
import com.axelor.apps.hr.db.Expense;

public interface ExpenseCheckResponseService {
  CheckResponse createResponse(Expense expense);
}
