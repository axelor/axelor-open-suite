/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.csv.script;

import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.InventoryLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.service.InventoryService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Map;

public class ImportInventory {

  @Inject InventoryService inventoryService;

  @Transactional
  public Object validateInventory(Object bean, Map<String, Object> values) throws AxelorException {

    assert bean instanceof InventoryLine;

    Inventory inventory = (Inventory) bean;
    StockMove stockMove = inventoryService.validateInventory(inventory);
    stockMove.setRealDate(inventory.getDateT().toLocalDate());

    return inventory;
  }
}
