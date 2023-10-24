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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.YearBaseRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Singleton
public class ExpenseToolServiceImpl implements ExpenseToolService {
  protected AppBaseService appBaseService;
  protected ExpenseLineService expenseLineService;
  protected SequenceService sequenceService;
  protected ExpenseRepository expenseRepository;
  protected PeriodRepository periodRepository;

  @Inject
  public ExpenseToolServiceImpl(
      AppBaseService appBaseService,
      ExpenseLineService expenseLineService,
      SequenceService sequenceService,
      ExpenseRepository expenseRepository,
      PeriodRepository periodRepository) {
    this.appBaseService = appBaseService;
    this.expenseLineService = expenseLineService;
    this.sequenceService = sequenceService;
    this.expenseRepository = expenseRepository;
    this.periodRepository = periodRepository;
  }

  @Override
  public Expense getOrCreateExpense(Employee employee) {
    if (employee == null) {
      return null;
    }

    Expense expense =
        expenseRepository
            .all()
            .filter(
                "self.statusSelect = ?1 AND self.employee.id = ?2",
                ExpenseRepository.STATUS_DRAFT,
                employee.getId())
            .order("-id")
            .fetchOne();

    if (expense == null) {
      expense = new Expense();
      expense.setEmployee(employee);
      Company company = null;
      if (employee.getMainEmploymentContract() != null) {
        company = employee.getMainEmploymentContract().getPayCompany();
      } else if (employee.getUser() != null) {
        company = employee.getUser().getActiveCompany();
      }

      Period period =
          periodRepository
              .all()
              .filter(
                  "self.fromDate <= ?1 AND self.toDate >= ?1 AND self.allowExpenseCreation = true AND self.year.company = ?2 AND self.year.typeSelect = ?3",
                  appBaseService.getTodayDate(company),
                  company,
                  YearBaseRepository.STATUS_OPENED)
              .fetchOne();

      expense.setCompany(company);
      expense.setPeriod(period);
      expense.setStatusSelect(ExpenseRepository.STATUS_DRAFT);
    }
    return expense;
  }

  @Override
  public void setDraftSequence(Expense expense) throws AxelorException {
    if (expense.getId() != null && Strings.isNullOrEmpty(expense.getExpenseSeq())) {
      expense.setExpenseSeq(sequenceService.getDraftSequenceNumber(expense));
    }
  }

  @Override
  public Expense updateMoveDateAndPeriod(Expense expense) {
    updateMoveDate(expense);
    updatePeriod(expense);
    return expense;
  }

  protected void updateMoveDate(Expense expense) {
    List<ExpenseLine> expenseLines = new ArrayList<>();

    if (expense.getGeneralExpenseLineList() != null) {
      expenseLines.addAll(expense.getGeneralExpenseLineList());
    }
    if (expense.getKilometricExpenseLineList() != null) {
      expenseLines.addAll(expense.getKilometricExpenseLineList());
    }
    expense.setMoveDate(
        expenseLines.stream()
            .map(ExpenseLine::getExpenseDate)
            .filter(Objects::nonNull)
            .max(LocalDate::compareTo)
            .orElse(null));
  }

  protected void updatePeriod(Expense expense) {
    if (expense.getMoveDate() != null) {
      LocalDate moveDate = expense.getMoveDate();
      if (expense.getPeriod() == null
          || !(!moveDate.isBefore(expense.getPeriod().getFromDate()))
          || !(!moveDate.isAfter(expense.getPeriod().getToDate()))) {
        expense.setPeriod(
            periodRepository
                .all()
                .filter(
                    "self.fromDate <= :_moveDate AND self.toDate >= :_moveDate AND"
                        + " self.statusSelect = 1 AND self.allowExpenseCreation = true AND"
                        + " self.year.company = :_company AND self.year.typeSelect = 1")
                .bind("_moveDate", expense.getMoveDate())
                .bind("_company", expense.getCompany())
                .fetchOne());
      }
    }
  }
}
