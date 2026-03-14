package com.axelor.apps.businessproject.service.extraexpense;

public interface ExtraExpenseLineService {
  String getCodeFromTypeSelect(int typeSelect);

  Integer getTypeSelectFromCode(String code);

  String getTravelExpensesCode();

  String getToolsUsageCode();

  String getCode25KmTravelExpense();
}
