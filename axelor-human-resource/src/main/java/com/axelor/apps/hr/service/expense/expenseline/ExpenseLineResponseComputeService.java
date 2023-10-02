package com.axelor.apps.hr.service.expense.expenseline;

import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.rest.dto.ExpenseLinePostRequest;
import com.axelor.apps.hr.rest.dto.ExpenseLineResponse;
import javax.ws.rs.core.Response;

public interface ExpenseLineResponseComputeService {
  Response computeCreateResponse(
      ExpenseLine expenseLine,
      ExpenseLinePostRequest requestBody,
      ExpenseLineResponse expenseLineResponse);
}
