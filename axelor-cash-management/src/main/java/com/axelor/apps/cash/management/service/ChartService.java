package com.axelor.apps.cash.management.service;

import java.util.List;
import java.util.Map;

import com.axelor.apps.base.db.BankDetails;
import com.axelor.auth.db.User;

public interface ChartService {

	public List<Map<String, Object>> getCashBalanceData(User user, BankDetails bankDetails);

}
