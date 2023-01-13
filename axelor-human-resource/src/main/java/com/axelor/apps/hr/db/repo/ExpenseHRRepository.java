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

import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.service.expense.ExpenseFetchPeriodService;
import com.axelor.apps.hr.service.expense.ExpenseService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import javax.persistence.PersistenceException;
import org.apache.commons.collections.CollectionUtils;

public class ExpenseHRRepository extends ExpenseRepository {

  protected ExpenseFetchPeriodService expenseFetchPeriodService;

  @Inject
  public ExpenseHRRepository(ExpenseFetchPeriodService expenseFetchPeriodService) {
    this.expenseFetchPeriodService = expenseFetchPeriodService;
  }

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
  public Expense copy(Expense entity, boolean deep) {
    Expense expense = super.copy(entity, deep);
    expense.setStatusSelect(STATUS_DRAFT);
    if (CollectionUtils.isNotEmpty(expense.getGeneralExpenseLineList())) {
      for (ExpenseLine expenseLine : expense.getGeneralExpenseLineList()) {
        expenseLine.setExpenseDate(null);
        expenseLine.setJustificationMetaFile(null);
      }
    }
    if (CollectionUtils.isNotEmpty(expense.getKilometricExpenseLineList())) {
      expense.getKilometricExpenseLineList().forEach(line -> line.setExpenseDate(null));
    }
    expense.setSentDate(null);
    expense.setValidatedBy(null);
    expense.setValidationDate(null);
    expense.setPeriod(expenseFetchPeriodService.getPeriod(expense));
    expense.setExpenseSeq(null);
    expense.setMove(null);
    expense.setMoveDate(null);
    expense.setVentilated(false);
    expense.setPaymentStatusSelect(InvoicePaymentRepository.STATUS_DRAFT);
    return expense;
  }
}
