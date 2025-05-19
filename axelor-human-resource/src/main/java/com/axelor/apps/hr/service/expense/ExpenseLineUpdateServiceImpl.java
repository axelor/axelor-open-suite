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
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.KilometricAllowParam;
import com.axelor.apps.hr.db.repo.ExpenseLineRepository;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.employee.EmployeeFetchService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class ExpenseLineUpdateServiceImpl implements ExpenseLineUpdateService {

  protected ExpenseToolService expenseToolService;
  protected ExpenseComputationService expenseComputationService;
  protected ExpenseLineToolService expenseLineToolService;
  protected ExpenseLineRepository expenseLineRepository;
  protected ExpenseRepository expenseRepository;
  protected EmployeeFetchService employeeFetchService;

  @Inject
  public ExpenseLineUpdateServiceImpl(
      ExpenseToolService expenseToolService,
      ExpenseComputationService expenseComputationService,
      ExpenseLineToolService expenseLineToolService,
      ExpenseLineRepository expenseLineRepository,
      ExpenseRepository expenseRepository,
      EmployeeFetchService employeeFetchService) {
    this.expenseToolService = expenseToolService;
    this.expenseComputationService = expenseComputationService;
    this.expenseLineToolService = expenseLineToolService;
    this.expenseLineRepository = expenseLineRepository;
    this.expenseRepository = expenseRepository;
    this.employeeFetchService = employeeFetchService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public ExpenseLine updateExpenseLine(
      ExpenseLine expenseLine,
      Project project,
      Product expenseProduct,
      LocalDate expenseDate,
      KilometricAllowParam kilometricAllowParam,
      Integer kilometricType,
      BigDecimal distance,
      String fromCity,
      String toCity,
      BigDecimal totalAmount,
      BigDecimal totalTax,
      MetaFile justificationMetaFile,
      String comments,
      Employee employee,
      Currency currency,
      Boolean toInvoice,
      Expense newExpense,
      ProjectTask projectTask,
      List<Long> invitedCollaboratorList)
      throws AxelorException {
    expenseDate = expenseDate != null ? expenseDate : expenseLine.getExpenseDate();
    List<Employee> employeeList =
        employeeFetchService.filterInvitedCollaborators(invitedCollaboratorList, expenseDate);

    if (expenseLineToolService.isKilometricExpenseLine(expenseLine)) {
      updateKilometricExpenseLine(
          expenseLine,
          project,
          expenseDate,
          kilometricAllowParam,
          kilometricType,
          distance,
          fromCity,
          toCity,
          comments,
          employee,
          currency,
          toInvoice,
          expenseProduct,
          newExpense,
          projectTask,
          employeeList);
    } else {
      updateGeneralExpenseLine(
          expenseLine,
          project,
          expenseProduct,
          expenseDate,
          totalAmount,
          totalTax,
          justificationMetaFile,
          comments,
          employee,
          currency,
          toInvoice,
          newExpense,
          projectTask,
          employeeList);
    }

    if (newExpense != null) {
      changeLineParentExpense(expenseLine, newExpense);
      return expenseLine;
    }

    Expense expense = expenseLine.getExpense();
    if (expense != null) {
      expenseComputationService.compute(expense);
    }

    return expenseLine;
  }

  protected void updateGeneralExpenseLine(
      ExpenseLine expenseLine,
      Project project,
      Product expenseProduct,
      LocalDate expenseDate,
      BigDecimal totalAmount,
      BigDecimal totalTax,
      MetaFile justificationMetaFile,
      String comments,
      Employee employee,
      Currency currency,
      Boolean toInvoice,
      Expense newExpense,
      ProjectTask projectTask,
      List<Employee> employeeList)
      throws AxelorException {

    checkParentStatus(expenseLine.getExpense());
    updateBasicExpenseLine(
        expenseLine,
        project,
        employee,
        expenseDate,
        comments,
        currency,
        expenseProduct,
        toInvoice,
        newExpense,
        projectTask,
        employeeList);
    expenseProduct = expenseProduct != null ? expenseProduct : expenseLine.getExpenseProduct();
    totalAmount = totalAmount != null ? totalAmount : expenseLine.getTotalAmount();
    totalTax = totalTax != null ? totalTax : expenseLine.getTotalTax();
    expenseLineToolService.setGeneralExpenseLineInfo(
        expenseProduct, totalAmount, totalTax, justificationMetaFile, expenseLine);
  }

  protected void updateKilometricExpenseLine(
      ExpenseLine expenseLine,
      Project project,
      LocalDate expenseDate,
      KilometricAllowParam kilometricAllowParam,
      Integer kilometricType,
      BigDecimal distance,
      String fromCity,
      String toCity,
      String comments,
      Employee employee,
      Currency currency,
      Boolean toInvoice,
      Product expenseProduct,
      Expense newExpense,
      ProjectTask projectTask,
      List<Employee> employeeList)
      throws AxelorException {
    checkParentStatus(expenseLine.getExpense());
    updateBasicExpenseLine(
        expenseLine,
        project,
        employee,
        expenseDate,
        comments,
        currency,
        expenseProduct,
        toInvoice,
        newExpense,
        projectTask,
        employeeList);
    updateKilometricExpenseLineInfo(
        expenseLine, kilometricAllowParam, kilometricType, fromCity, toCity, distance);
  }

  protected void changeLineParentExpense(ExpenseLine expenseLine, Expense newExpense)
      throws AxelorException {
    Expense oldExpense = expenseLine.getExpense();
    if (oldExpense == null) {
      expenseToolService.addExpenseLineToExpenseAndCompute(newExpense, expenseLine);
    } else {
      Long oldExpenseId = oldExpense.getId();
      changeParent(expenseLine, newExpense);
      Expense oldExpenseToCompute = expenseRepository.find(oldExpenseId);
      expenseComputationService.compute(oldExpenseToCompute);
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void changeParent(ExpenseLine expenseLine, Expense newExpense) throws AxelorException {
    Expense oldExpense = expenseLine.getExpense();
    if (oldExpense == null) {
      return;
    }
    checkParentStatus(newExpense, oldExpense);
    expenseLine.setExpense(newExpense);
    expenseToolService.addExpenseLineToExpense(newExpense, expenseLine);
    expenseComputationService.compute(newExpense);
    expenseLineRepository.save(expenseLine);
  }

  protected void checkParentStatus(Expense newExpense, Expense oldExpense) throws AxelorException {
    if (oldExpense.getStatusSelect() != ExpenseRepository.STATUS_DRAFT) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_PARENT_NOT_DRAFT));
    }

    if (newExpense.getStatusSelect() != ExpenseRepository.STATUS_DRAFT) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_NEW_EXPENSE_NOT_DRAFT));
    }
  }

  protected void updateBasicExpenseLine(
      ExpenseLine expenseLine,
      Project project,
      Employee employee,
      LocalDate expenseDate,
      String comments,
      Currency currency,
      Product expenseProduct,
      Boolean toInvoice,
      Expense newExpense,
      ProjectTask projectTask,
      List<Employee> employeeList)
      throws AxelorException {
    updateLineCurrency(expenseLine, currency, newExpense);
    if (project != null) {
      expenseLine.setProject(project);
    }
    if (comments != null) {
      expenseLine.setComments(comments);
    }
    if (employee != null) {
      expenseLine.setEmployee(employee);
    }
    if (currency != null) {
      expenseLine.setCurrency(currency);
    }
    if (expenseDate != null) {
      expenseLine.setExpenseDate(expenseDate);
    }
    if (expenseProduct != null) {
      expenseLine.setExpenseProduct(expenseProduct);
    }

    if (expenseLine.getExpenseProduct() != null
        && expenseLine.getExpenseProduct().getDeductLunchVoucher()) {
      if (employeeList != null) {
        Set<Employee> employeeSet = new HashSet<>(employeeList);
        expenseLine.setInvitedCollaboratorSet(employeeSet);
      }
      expenseLine.setIsAloneMeal(CollectionUtils.isEmpty(expenseLine.getInvitedCollaboratorSet()));
    } else {
      expenseLine.setIsAloneMeal(false);
    }
    if (projectTask != null) {
      expenseLine.setProjectTask(projectTask);
    }
  }

  protected void updateLineCurrency(ExpenseLine expenseLine, Currency currency, Expense newExpense)
      throws AxelorException {
    if (currency != null) {
      if (newExpense != null) {
        Currency newExpenseCurrency = newExpense.getCurrency();
        if (newExpenseCurrency != null && !currency.equals(newExpenseCurrency)) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_UPDATED_CURRENCY_INCONSISTENCY));
        }
      }
      Expense oldExpense = expenseLine.getExpense();
      if (oldExpense != null) {
        Currency oldExpenseCurrency = oldExpense.getCurrency();
        if (oldExpenseCurrency != null && !currency.equals(oldExpenseCurrency)) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(
                  HumanResourceExceptionMessage
                      .EXPENSE_LINE_UPDATED_CURRENCY_CURRENT_EXPENSE_INCONSISTENCY));
        }
      }
      expenseLine.setCurrency(currency);
    }
  }

  protected void updateKilometricExpenseLineInfo(
      ExpenseLine expenseLine,
      KilometricAllowParam kilometricAllowParam,
      Integer kilometricType,
      String fromCity,
      String toCity,
      BigDecimal distance)
      throws AxelorException {
    if (kilometricType != null) {
      expenseLine.setKilometricTypeSelect(kilometricType);
    }
    if (kilometricAllowParam != null) {
      expenseLine.setKilometricAllowParam(kilometricAllowParam);
    }
    if (StringUtils.notEmpty(fromCity)) {
      expenseLine.setFromCity(fromCity);
    }
    if (StringUtils.notEmpty(toCity)) {
      expenseLine.setToCity(toCity);
    }

    if ((StringUtils.notEmpty(fromCity) || StringUtils.notEmpty(toCity)) && distance == null) {
      expenseLineToolService.computeDistance(distance, expenseLine);
    }

    if (distance != null) {
      expenseLine.setDistance(distance);
    }

    expenseLineToolService.computeAmount(expenseLine.getEmployee(), expenseLine);

    expenseLineRepository.save(expenseLine);
  }

  protected void checkParentStatus(Expense expense) throws AxelorException {
    if (expense == null) {
      return;
    }
    if (expense.getStatusSelect() != ExpenseRepository.STATUS_DRAFT) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_EXPENSE_NOT_DRAFT));
    }
  }
}
