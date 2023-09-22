package com.axelor.apps.hr.service.expense.expenseline;

import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.rest.dto.ExpenseLineResponse;
import com.axelor.apps.project.db.Project;
import com.axelor.utils.api.ResponseConstructor;
import javax.ws.rs.core.Response;

public class ExpenseLineResponseComputeServiceImpl implements ExpenseLineResponseComputeService {
  @Override
  public Response computeCreateResponse(
      ExpenseLine expenseLine,
      Project project,
      Boolean toInvoice,
      ExpenseLineResponse expenseLineResponse) {
    return ResponseConstructor.buildCreateResponse(expenseLine, expenseLineResponse);
  }
}
