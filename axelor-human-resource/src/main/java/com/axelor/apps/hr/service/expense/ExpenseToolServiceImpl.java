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
package com.axelor.apps.hr.service.expense;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.PeriodRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.db.repo.YearBaseRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.KilometricAllowParam;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.project.db.Project;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

@Singleton
public class ExpenseToolServiceImpl implements ExpenseToolService {
  protected AppBaseService appBaseService;
  protected ExpenseLineService expenseLineService;
  protected SequenceService sequenceService;
  protected ExpenseRepository expenseRepository;
  protected PeriodRepository periodRepository;
  protected ExpenseComputationService expenseComputationService;
  protected ExpenseLineToolService expenseLineToolService;
  protected ExpenseProofFileService expenseProofFileService;

  @Inject
  public ExpenseToolServiceImpl(
      AppBaseService appBaseService,
      ExpenseLineService expenseLineService,
      SequenceService sequenceService,
      ExpenseRepository expenseRepository,
      PeriodRepository periodRepository,
      ExpenseComputationService expenseComputationService,
      ExpenseLineToolService expenseLineToolService) {
    this.appBaseService = appBaseService;
    this.expenseLineService = expenseLineService;
    this.sequenceService = sequenceService;
    this.expenseRepository = expenseRepository;
    this.periodRepository = periodRepository;
    this.expenseComputationService = expenseComputationService;
    this.expenseLineToolService = expenseLineToolService;
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
  public Expense getOrCreateExpense(Employee employee, Project project) {
    if (employee == null) {
      return null;
    }

    Expense expense =
        expenseRepository
            .all()
            .filter(
                "self.statusSelect = :status AND self.employee.id = :employeeId AND self.project.id = :projectId")
            .bind("status", ExpenseRepository.STATUS_DRAFT)
            .bind("employeeId", employee.getId())
            .bind("projectId", project.getId())
            .order("-id")
            .fetchOne();

    if (expense == null) {
      expense = new Expense();
      expense.setEmployee(employee);
      expense.setProject(project);
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
      assert company != null;
      expense.setCurrency(company.getCurrency());
      expense.setTypeSelect(1);
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

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void addExpenseLinesToExpense(Expense expense, List<ExpenseLine> expenseLineList)
      throws AxelorException {
    if (expense.getStatusSelect() != ExpenseRepository.STATUS_DRAFT) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_ADD_LINE_WRONG_STATUS));
    }

    if (CollectionUtils.isEmpty(expenseLineList)) {
      return;
    }

    checkCurrency(expense, expenseLineList);

    for (ExpenseLine expenseLine : expenseLineList) {
      Product expenseProduct = expenseLine.getExpenseProduct();
      KilometricAllowParam kilometricAllowParam = expenseLine.getKilometricAllowParam();
      if (expenseProduct != null && kilometricAllowParam == null) {
        expense.addGeneralExpenseLineListItem(expenseLine);
      }
      if (expenseLineToolService.isKilometricExpenseLine(expenseLine)) {
        expense.addKilometricExpenseLineListItem(expenseLine);
      }
    }

    expenseRepository.save(expense);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void addOrUpdateProjectExpenseLines(Expense expense, List<ExpenseLine> expenseLineList)
      throws AxelorException {
    if (expense.getStatusSelect() != ExpenseRepository.STATUS_DRAFT) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_ADD_LINE_WRONG_STATUS));
    }

    if (CollectionUtils.isEmpty(expenseLineList)) {
      return;
    }

    checkCurrency(expense, expenseLineList);

    for (ExpenseLine incomingLine : expenseLineList) {
      if (incomingLine.getId() != null) {
        ExpenseLine managedLine = Beans.get(ExpenseLineRepository.class).find(incomingLine.getId());

        if (managedLine != null) {
          managedLine.setProjectTask(incomingLine.getProjectTask());
          managedLine.setExpenseProduct(incomingLine.getExpenseProduct());
          managedLine.setExpenseDate(incomingLine.getExpenseDate());
          managedLine.setUntaxedAmount(incomingLine.getUntaxedAmount());
          managedLine.setTotalTax(incomingLine.getTotalTax());
          managedLine.setComments(incomingLine.getComments());
          managedLine.setJustificationMetaFile(incomingLine.getJustificationMetaFile());

          expenseComputationService.computeLine(managedLine);

          // Remove the incoming detached version from the expense collection
          if (expense.getGeneralExpenseLineList() != null) {
            expense
                .getGeneralExpenseLineList()
                .removeIf(l -> l.getId() != null && l.getId().equals(managedLine.getId()));
            expense.getGeneralExpenseLineList().add(managedLine);
          }
        }
      } else {
        incomingLine.setExpense(expense);
        expenseComputationService.computeLine(incomingLine);
        if (expenseLineToolService.isKilometricExpenseLine(incomingLine)) {
          expense.addKilometricExpenseLineListItem(incomingLine);
        } else {
          expense.addGeneralExpenseLineListItem(incomingLine);
        }
      }
    }

    expenseRepository.save(expense);
  }

  @Override
  public void addExpenseLineToExpense(Expense expense, ExpenseLine expenseLine)
      throws AxelorException {
    checkCurrency(expense, expenseLine);
    if (expenseLineToolService.isKilometricExpenseLine(expenseLine)) {
      expense.addKilometricExpenseLineListItem(expenseLine);
    } else {
      expense.addGeneralExpenseLineListItem(expenseLine);
    }
  }

  protected void checkCurrency(Expense expense, List<ExpenseLine> expenseLineList)
      throws AxelorException {
    Set<Currency> currencySet =
        expenseLineList.stream()
            .map(ExpenseLine::getCurrency)
            .filter(Objects::nonNull) // CRITICAL: ignore nulls during validation
            .collect(Collectors.toSet());

    if (currencySet.isEmpty()) {
      return;
    }

    Currency firstLineCurrency = currencySet.iterator().next();

    if (currencySet.size() > 1
        || (expense.getCurrency() != null && !expense.getCurrency().equals(firstLineCurrency))) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_CURRENCY_NOT_EQUAL));
    }
  }

  private ExpenseLine findExistingLine(Expense expense, ExpenseLine incoming) {
    List<ExpenseLine> allLines = expense.getGeneralExpenseLineList();
    if (allLines == null) return null;

    return allLines.stream()
        .filter(l -> l.getProjectTask() != null && incoming.getProjectTask() != null)
        .filter(l -> l.getProjectTask().getId().equals(incoming.getProjectTask().getId()))
        .filter(l -> l.getExpenseProduct() != null && incoming.getExpenseProduct() != null)
        .filter(l -> l.getExpenseProduct().getId().equals(incoming.getExpenseProduct().getId()))
        .findFirst()
        .orElse(null);
  }

  protected void checkCurrency(Expense expense, ExpenseLine expenseLine) throws AxelorException {
    if (!expense.getCurrency().equals(expenseLine.getCurrency())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_CURRENCY_NOT_EQUAL));
    }
  }

  @Override
  public boolean hasSeveralCurrencies(List<ExpenseLine> expenseLineList) {
    Set<Currency> currencySet =
        expenseLineList.stream().map(ExpenseLine::getCurrency).collect(Collectors.toSet());
    return currencySet.size() > 1;
  }

  @Override
  public boolean hasSeveralEmployees(List<ExpenseLine> expenseLineList) {
    Set<Employee> employeeSet =
        expenseLineList.stream().map(ExpenseLine::getEmployee).collect(Collectors.toSet());
    return employeeSet.size() > 1;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void addExpenseLinesToExpenseAndCompute(Expense expense, List<ExpenseLine> expenseLineList)
      throws AxelorException {
    addExpenseLinesToExpense(expense, expenseLineList);
    expenseComputationService.compute(expense);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void addExpenseLineToExpenseAndCompute(Expense expense, ExpenseLine expenseLine)
      throws AxelorException {
    addExpenseLineToExpense(expense, expenseLine);
    expenseComputationService.compute(expense);
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
