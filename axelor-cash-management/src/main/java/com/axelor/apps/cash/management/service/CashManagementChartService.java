package com.axelor.apps.cash.management.service;

import com.axelor.apps.base.db.BankDetails;
import com.axelor.auth.db.User;
import java.util.List;
import java.util.Map;

public interface CashManagementChartService {

  public List<Map<String, Object>> getCashBalanceData(User user, BankDetails bankDetails);
}
