package com.axelor.apps.stock.service;

import com.axelor.apps.stock.db.Inventory;
import com.axelor.exception.AxelorException;

public interface InventoryProductService {
  void checkDuplicate(Inventory inventory) throws AxelorException;
}
