package com.axelor.apps.hr.rest.dto;

import com.axelor.apps.hr.db.Expense;
import com.axelor.utils.api.ResponseStructure;

public class ExpenseResponse extends ResponseStructure {

  protected Long expenseId;
  protected Integer status;

  public ExpenseResponse(Expense expense) {
    super(expense.getVersion());
    this.expenseId = expense.getId();
    this.status = expense.getStatusSelect();
  }

  public Long getExpenseId() {
    return expenseId;
  }
}
