/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.db.repo;

import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.service.expense.ExpenseService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import javax.persistence.PersistenceException;

public class ExpenseHRRepository extends ExpenseRepository {
  @Override
  public Expense save(Expense expense) {
    try {
      expense = super.save(expense);
      Beans.get(ExpenseService.class).setDraftSequence(expense);
      if (expense.getStatusSelect() == ExpenseRepository.STATUS_DRAFT) {
        Beans.get(ExpenseService.class).completeExpenseLines(expense);
      }

      return expense;
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }

  @Override
  public void remove(Expense entity) {
    if (entity.getVentilated()) {
      try {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.EXPENSE_CAN_NOT_DELETE_VENTILATED),
            entity.getExpenseSeq());
      } catch (AxelorException e) {
        throw new PersistenceException(e.getMessage(), e);
      }
    }
    super.remove(entity);
  }
}
