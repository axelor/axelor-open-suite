package com.axelor.apps.hr.service.expense.expenseline;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.rest.dto.CheckResponse;
import com.axelor.apps.base.rest.dto.CheckResponseLine;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.expense.ExpenseLineService;
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

  @Inject
  public ExpenseLineCheckResponseServiceImpl(
      AppBaseService appBaseService, ExpenseLineService expenseLineService) {
    this.appBaseService = appBaseService;
    this.expenseLineService = expenseLineService;
  }

  @Override
  public CheckResponse createResponse(ExpenseLine expenseLine) {
    List<CheckResponseLine> checkResponseLineList = new ArrayList<>();
    checkResponseLineList.add(checkExpenseDate(expenseLine));
    checkResponseLineList.add(checkTotalAmount(expenseLine));
    checkResponseLineList.add(checkJustificationFile(expenseLine));
    List<CheckResponseLine> filteredList =
        checkResponseLineList.stream().filter(Objects::nonNull).collect(Collectors.toList());
    return new CheckResponse(expenseLine, filteredList);
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
}
