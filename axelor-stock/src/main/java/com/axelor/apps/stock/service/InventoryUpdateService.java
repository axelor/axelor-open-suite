package com.axelor.apps.stock.service;

import com.axelor.apps.stock.db.Inventory;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;

public interface InventoryUpdateService {

  void updateInventoryStatus(Inventory inventory, Integer wantedStatus, User user)
      throws AxelorException;
}
