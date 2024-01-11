/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.expense.ExpenseFetchPeriodService;
import com.axelor.apps.hr.service.expense.ExpenseService;
import com.axelor.i18n.I18n;
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
    expense.setSentDateTime(null);
    expense.setValidatedBy(null);
    expense.setValidationDateTime(null);
    expense.setPeriod(expenseFetchPeriodService.getPeriod(expense));
    expense.setExpenseSeq(null);
    expense.setMove(null);
    expense.setMoveDate(null);
    expense.setVentilated(false);
    expense.setPaymentStatusSelect(InvoicePaymentRepository.STATUS_DRAFT);
    return expense;
  }

  @Override
  public void remove(Expense entity) {
    if (entity.getVentilated()) {
      try {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(HumanResourceExceptionMessage.EXPENSE_CAN_NOT_DELETE_VENTILATED),
            entity.getExpenseSeq());
      } catch (AxelorException e) {
        throw new PersistenceException(e.getMessage(), e);
      }
    }
    super.remove(entity);
  }
}
