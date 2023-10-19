package com.axelor.apps.hr.service.expense;

import com.axelor.apps.base.rest.dto.CheckResponse;
import com.axelor.apps.base.rest.dto.CheckResponseLine;
import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.expense.expenseline.ExpenseLineCheckResponseService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ExpenseCheckResponseServiceImpl implements ExpenseCheckResponseService {
  protected ExpenseLineService expenseLineService;
  protected ExpenseLineCheckResponseService expenseLineCheckResponseService;

  @Inject
  public ExpenseCheckResponseServiceImpl(
      ExpenseLineService expenseLineService,
      ExpenseLineCheckResponseService expenseLineCheckResponseService) {
    this.expenseLineService = expenseLineService;
    this.expenseLineCheckResponseService = expenseLineCheckResponseService;
  }

  @Override
  public CheckResponse createResponse(Expense expense) {
    List<CheckResponseLine> checkResponseLineList = new ArrayList<>();
    checkResponseLineList.add(checkLineTotalAmount(expense));
    List<CheckResponseLine> filteredList =
        checkResponseLineList.stream().filter(Objects::nonNull).collect(Collectors.toList());

    List<CheckResponse> expenseLineCheckResponseList = getExpenseLineCheckResponseList(expense);

    return new CheckResponse(expense, filteredList, expenseLineCheckResponseList);
  }

  protected List<CheckResponse> getExpenseLineCheckResponseList(Expense expense) {
    List<CheckResponse> expenseLineCheckResponseList = new ArrayList<>();
    List<ExpenseLine> generalExpenseLineList = expense.getGeneralExpenseLineList();
    List<ExpenseLine> kilometricExpenseLineList = expense.getKilometricExpenseLineList();

    addExpenseLineCheckResponse(generalExpenseLineList, expenseLineCheckResponseList);
    addExpenseLineCheckResponse(kilometricExpenseLineList, expenseLineCheckResponseList);
    return expenseLineCheckResponseList;
  }

  protected void addExpenseLineCheckResponse(
      List<ExpenseLine> expenseLineList, List<CheckResponse> expenseLineCheckResponseList) {
    for (ExpenseLine expenseLine : expenseLineList) {
      CheckResponse expenseLineCheckResponse =
          expenseLineCheckResponseService.createResponse(expenseLine);
      if (CollectionUtils.isNotEmpty(expenseLineCheckResponse.getChecks())) {
        expenseLineCheckResponseList.add(expenseLineCheckResponse);
      }
    }
  }

  /**
   * action-expense-method-check-total-amount
   *
   * @param expense
   * @return
   */
  protected CheckResponseLine checkLineTotalAmount(Expense expense) {
    if (expenseLineService.isThereOverAmountLimit(expense)) {
      return new CheckResponseLine(
          expense,
          I18n.get(HumanResourceExceptionMessage.EXPENSE_AMOUNT_LIMIT_ERROR),
          CheckResponseLine.CHECK_TYPE_ALERT);
    }
    return null;
  }
}
