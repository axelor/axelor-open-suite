/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.expense;

import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import java.util.List;

public interface ExpenseLineService {

  public List<ExpenseLine> getExpenseLineList(Expense expense);

  /**
   * fill {@link ExpenseLine#expense} in {@link Expense#generalExpenseLineList} and {@link
   * Expense#kilometricExpenseLineList}
   *
   * @param expense
   */
  void completeExpenseLines(Expense expense);

  public ExpenseLine getTotalTaxFromProductAndTotalAmount(ExpenseLine expenseLine);
}
