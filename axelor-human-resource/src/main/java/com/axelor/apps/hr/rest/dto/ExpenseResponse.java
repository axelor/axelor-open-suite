/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.rest.dto;

import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.utils.api.ResponseStructure;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class ExpenseResponse extends ResponseStructure {

  protected Long expenseId;
  protected Integer status;
  protected List<ExpenseLineIdDto> expenseLines;

  public ExpenseResponse(Expense expense) {
    super(expense.getVersion());
    this.expenseId = expense.getId();
    this.status = expense.getStatusSelect();

    // We map the database lines to our simple DTO here
    if (expense.getGeneralExpenseLineList() != null) {
      this.expenseLines =
          expense.getGeneralExpenseLineList().stream()
              .map(line -> new ExpenseLineIdDto(line))
              .collect(Collectors.toList());
    }
  }

  public Long getExpenseId() {
    return expenseId;
  }

  public Integer getStatus() {
    return status;
  }

  public List<ExpenseLineIdDto> getExpenseLines() {
    return expenseLines;
  }

  /** This is the "ExpenseLineIdDto" class. It's defined right here as a helper class. */
  public static class ExpenseLineIdDto {
    public Long id;
    public Long expenseProductId;
    public BigDecimal untaxedAmount;
    public BigDecimal totalTax;
    public BigDecimal totalAmount;
    public String comments;
    public Long justificationFileId;

    public ExpenseLineIdDto(ExpenseLine line) {
      this.id = line.getId();
      this.expenseProductId =
          line.getExpenseProduct() != null ? line.getExpenseProduct().getId() : null;
      this.untaxedAmount = line.getUntaxedAmount();
      this.totalTax = line.getTotalTax();
      this.totalAmount = line.getTotalAmount();
      this.justificationFileId =
          line.getJustificationMetaFile() != null ? line.getJustificationMetaFile().getId() : null;
      this.comments = line.getComments();
    }
  }
}
