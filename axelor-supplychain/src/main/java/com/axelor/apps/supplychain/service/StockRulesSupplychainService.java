package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockRules;

public interface StockRulesSupplychainService {

  void processNonCompliantStockLocationLine(
      StockRules stockRules, StockLocationLine stockLocationLine) throws AxelorException;
}
