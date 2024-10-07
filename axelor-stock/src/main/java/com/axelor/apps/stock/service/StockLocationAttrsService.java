package com.axelor.apps.stock.service;

import com.axelor.apps.stock.db.StockLocation;

public interface StockLocationAttrsService {

  String getParentStockLocationDomain(StockLocation stockLocation);
}
