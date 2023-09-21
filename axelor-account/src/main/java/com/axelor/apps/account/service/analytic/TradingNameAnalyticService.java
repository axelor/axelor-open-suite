package com.axelor.apps.account.service.analytic;

import com.axelor.apps.base.db.TradingName;

public interface TradingNameAnalyticService {
  String getDomainOnCompany(TradingName tradingName);

  boolean isAnalyticTypeByTradingName(TradingName tradingName);
}
