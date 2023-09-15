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
package com.axelor.apps.hr.db.repo;

import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.i18n.I18n;
import java.math.BigDecimal;
import javax.persistence.PersistenceException;

public class ExpenseLineHRRepository extends ExpenseLineRepository {

  @Override
  public ExpenseLine save(ExpenseLine expenseLine) {

    BigDecimal amountLimit =
        expenseLine.getExpenseProduct() != null
            ? expenseLine.getExpenseProduct().getAmountLimit()
            : BigDecimal.ZERO;

    if (amountLimit.compareTo(BigDecimal.ZERO) != 0
        && amountLimit.compareTo(expenseLine.getTotalAmount()) < 0) {
      throw new PersistenceException(
          String.format(
              I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_VALIDATE_TOTAL_AMOUNT),
              expenseLine.getExpenseProduct().getAmountLimit()));
    }
    return super.save(expenseLine);
  }
}
