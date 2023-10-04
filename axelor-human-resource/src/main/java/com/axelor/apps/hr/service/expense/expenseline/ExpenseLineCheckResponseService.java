package com.axelor.apps.hr.service.expense.expenseline;

import com.axelor.apps.base.rest.dto.CheckResponse;
import com.axelor.apps.hr.db.ExpenseLine;

public interface ExpenseLineCheckResponseService {
  CheckResponse createResponse(ExpenseLine expenseLine);
}
