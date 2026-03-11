package com.axelor.apps.businessproject.service.extraexpense;

import com.axelor.apps.businessproject.db.repo.ExtraExpenseLineRepository;
import java.util.Map;

public class ExtraExpenseLineServiceImpl implements ExtraExpenseLineService {

  // Extra Expense product codes
  protected static final String CODE_TRAVEL_EXPENSES = "11KM";
  protected static final String CODE_25_KM_TRAVEL_EXPENSE = "25KM";
  protected static final String CODE_TOOLS_USAGE = "TOOLUSAGE";

  protected static final Map<String, Integer> CODE_TO_TYPE_SELECT =
      Map.of(
          CODE_TRAVEL_EXPENSES,
          ExtraExpenseLineRepository.TRAVEL_EXPENSES_EXTRA_EXPENSE_LINE_TYPE,
          CODE_25_KM_TRAVEL_EXPENSE,
          ExtraExpenseLineRepository.TRAVEL_EXPENSES_EXTRA_EXPENSE_LINE_TYPE,
          CODE_TOOLS_USAGE,
          ExtraExpenseLineRepository.TOOLS_USAGE_EXTRA_EXPENSE_LINE_TYPE);

  protected static final Map<Integer, String> TYPE_SELECT_TO_CODE =
      Map.of(
          ExtraExpenseLineRepository.TRAVEL_EXPENSES_EXTRA_EXPENSE_LINE_TYPE, CODE_TRAVEL_EXPENSES,
          ExtraExpenseLineRepository.TOOLS_USAGE_EXTRA_EXPENSE_LINE_TYPE, CODE_TOOLS_USAGE);

  @Override
  public String getCodeFromTypeSelect(int typeSelect) {
    return TYPE_SELECT_TO_CODE.get(typeSelect);
  }

  @Override
  public Integer getTypeSelectFromCode(String code) {
    return CODE_TO_TYPE_SELECT.get(code);
  }

  @Override
  public String getTravelExpensesCode() {
    return CODE_TRAVEL_EXPENSES;
  }

  @Override
  public String getToolsUsageCode() {
    return CODE_TOOLS_USAGE;
  }

  @Override
  public String getCode25KmTravelExpense() {
    return CODE_25_KM_TRAVEL_EXPENSE;
  }
}
