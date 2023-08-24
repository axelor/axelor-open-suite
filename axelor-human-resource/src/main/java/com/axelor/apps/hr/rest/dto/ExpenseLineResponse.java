package com.axelor.apps.hr.rest.dto;

import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.utils.api.ResponseStructure;

public class ExpenseLineResponse extends ResponseStructure {

  private final Long expenseLineId;

  public ExpenseLineResponse(ExpenseLine expenseLine) {
    super(expenseLine.getVersion());
    this.expenseLineId = expenseLine.getId();
  }

  public Long getExpenseLineId() {
    return expenseLineId;
  }
}
