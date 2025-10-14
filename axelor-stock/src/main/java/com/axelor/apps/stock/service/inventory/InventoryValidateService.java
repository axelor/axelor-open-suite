package com.axelor.apps.stock.service.inventory;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.Inventory;

public interface InventoryValidateService {

  void validate(Inventory inventory) throws AxelorException;
}
