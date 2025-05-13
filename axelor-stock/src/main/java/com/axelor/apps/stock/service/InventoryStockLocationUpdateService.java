package com.axelor.apps.stock.service;

import com.axelor.apps.stock.db.Inventory;

public interface InventoryStockLocationUpdateService {
  void storeLastInventoryData(Inventory inventory);
}
