/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.csv.script;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.InventoryLine;
import com.axelor.apps.stock.service.InventoryService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Map;

public class ImportInventory {

  @Inject InventoryService inventoryService;

  @Transactional(rollbackOn = {Exception.class})
  public Object validateInventory(Object bean, Map<String, Object> values) throws AxelorException {

    assert bean instanceof InventoryLine;

    Inventory inventory = (Inventory) bean;
    inventoryService.validateInventory(inventory);

    return inventory;
  }

  public Object importInventory(Object bean, Map<String, Object> values) throws AxelorException {

    assert bean instanceof Inventory;

    Inventory inventory = (Inventory) bean;
    inventory.setInventoryTitle(inventoryService.computeTitle(inventory));

    return inventory;
  }
}
