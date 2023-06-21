package com.axelor.apps.account.service;

import com.axelor.meta.CallMethod;

public interface AccountingReportAnalyticConfigLineService {
  @CallMethod
  boolean getIsNotValidRuleLevel(int ruleLevel);
}
