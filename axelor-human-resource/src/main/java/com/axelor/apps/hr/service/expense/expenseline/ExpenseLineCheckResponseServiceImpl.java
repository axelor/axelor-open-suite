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
package com.axelor.apps.hr.service.expense.expenseline;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.rest.dto.CheckResponse;
import com.axelor.apps.base.rest.dto.CheckResponseLine;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.expense.ExpenseLineService;
import com.axelor.apps.hr.service.expense.ExpenseToolService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ExpenseLineCheckResponseServiceImpl implements ExpenseLineCheckResponseService {
  protected AppBaseService appBaseService;
  protected ExpenseLineService expenseLineService;
  protected ExpenseToolService expenseToolService;

  @Inject
  public ExpenseLineCheckResponseServiceImpl(
      AppBaseService appBaseService,
      ExpenseLineService expenseLineService,
      ExpenseToolService expenseToolService) {
    this.appBaseService = appBaseService;
    this.expenseLineService = expenseLineService;
    this.expenseToolService = expenseToolService;
  }

  @Override
  public CheckResponse createResponse(ExpenseLine expenseLine) {
    List<CheckResponseLine> checkResponseLineList = new ArrayList<>();
    checkExpenseLine(expenseLine, checkResponseLineList);
    List<CheckResponseLine> filteredList =
        checkResponseLineList.stream().filter(Objects::nonNull).collect(Collectors.toList());
    return new CheckResponse(expenseLine, filteredList);
  }

  protected void checkExpenseLine(
      ExpenseLine expenseLine, List<CheckResponseLine> checkResponseLineList) {
    checkResponseLineList.add(checkExpenseDate(expenseLine));
    checkResponseLineList.add(checkTotalAmount(expenseLine));
    checkResponseLineList.add(checkJustificationFile(expenseLine));
    checkResponseLineList.add(checkKilometricExpenseLineDistance(expenseLine));
  }

  /**
   * action-expense-line-validate-expenseDate
   *
   * @param expenseLine
   * @return
   */
  protected CheckResponseLine checkExpenseDate(ExpenseLine expenseLine) {
    Expense expense = expenseLine.getExpense();
    LocalDate todayDate =
        expense == null
            ? appBaseService.getTodayDate(null)
            : appBaseService.getTodayDate(expense.getCompany());
    if (expenseLine.getExpenseDate().isAfter(todayDate)) {
      return new CheckResponseLine(
          expenseLine,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_DATE_ERROR),
          CheckResponseLine.CHECK_TYPE_ERROR);
    }
    return null;
  }

  /**
   * action-expense-line-validate-totalAmount
   *
   * @param expenseLine
   * @return
   */
  protected CheckResponseLine checkTotalAmount(ExpenseLine expenseLine) {
    Product expenseProduct = expenseLine.getExpenseProduct();
    if (expenseProduct == null) {
      return null;
    }
    BigDecimal amountLimit = expenseProduct.getAmountLimit();
    BigDecimal totalAmount = expenseLine.getTotalAmount();
    if (amountLimit.compareTo(BigDecimal.ZERO) != 0 && totalAmount.compareTo(amountLimit) > 0) {
      return new CheckResponseLine(
          expenseLine,
          String.format(
              I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_VALIDATE_TOTAL_AMOUNT),
              amountLimit),
          CheckResponseLine.CHECK_TYPE_ALERT);
    }
    return null;
  }

  protected CheckResponseLine checkJustificationFile(ExpenseLine expenseLine) {
    if (expenseLine.getJustificationMetaFile() == null) {
      return null;
    }

    if (!expenseLineService.isFilePdfOrImage(expenseLine)) {
      return new CheckResponseLine(
          expenseLine,
          I18n.get(
              HumanResourceExceptionMessage.EXPENSE_LINE_JUSTIFICATION_FILE_NOT_CORRECT_FORMAT),
          CheckResponseLine.CHECK_TYPE_ALERT);
    }
    return null;
  }

  protected CheckResponseLine checkKilometricExpenseLineDistance(ExpenseLine expenseLine) {
    if (!expenseToolService.isKilometricExpenseLine(expenseLine)) {
      return null;
    }
    if (expenseLine.getDistance().compareTo(BigDecimal.ZERO) == 0) {
      return new CheckResponseLine(
          expenseLine,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_LINE_DISTANCE_ERROR),
          CheckResponseLine.CHECK_TYPE_ERROR);
    }
    return null;
  }
}
