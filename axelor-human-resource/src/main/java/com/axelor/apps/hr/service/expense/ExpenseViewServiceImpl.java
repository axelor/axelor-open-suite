/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;

public class ExpenseViewServiceImpl implements ExpenseViewService {

  protected ExpenseRepository expenseRepository;
  protected ExpenseFetchMoveService expenseFetchMoveService;

  @Inject
  public ExpenseViewServiceImpl(
      ExpenseRepository expenseRepository, ExpenseFetchMoveService expenseFetchMoveService) {
    this.expenseRepository = expenseRepository;
    this.expenseFetchMoveService = expenseFetchMoveService;
  }

  @Override
  public Map<String, Object> showMoves(Long expenseId) throws AxelorException {
    Expense expense = expenseRepository.find(expenseId);
    List<Move> moves = expenseFetchMoveService.findAllMovesByExpense(expense);

    if (moves.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_NO_MOVE_LINKED));
    }

    ActionViewBuilder actionView =
        ActionView.define(I18n.get(moves.size() == 1 ? "Move" : "Moves"))
            .model(Move.class.getName())
            .add("grid", "move-grid")
            .add("form", "move-form")
            .param("search-filters", "move-filters")
            .domain("self.expense.id = :expenseId OR self.expensePayment.id = :expenseId")
            .context("expenseId", expenseId);

    if (moves.size() == 1) {
      actionView.context("_showRecord", String.valueOf(moves.get(0).getId()));
    }

    return actionView.map();
  }
}
