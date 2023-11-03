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

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.hr.db.Expense;
import com.google.inject.Inject;

public class ExpenseFetchMoveServiceImpl implements ExpenseFetchMoveService {
  protected MoveRepository moveRepository;

  @Inject
  public ExpenseFetchMoveServiceImpl(MoveRepository moveRepository) {
    this.moveRepository = moveRepository;
  }

  @Override
  public Move getExpenseMove(Expense expense) {
    return moveRepository
        .all()
        .filter("self.expense.id = :expenseId")
        .bind("expenseId", expense.getId())
        .fetchOne();
  }

  @Override
  public Move getExpensePaymentMove(Expense expense) {
    return moveRepository
        .all()
        .filter("self.expensePayment.id = :expenseId")
        .bind("expenseId", expense.getId())
        .fetchOne();
  }
}
