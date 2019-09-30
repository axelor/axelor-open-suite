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
package com.axelor.apps.stock.web;

import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.InventoryLine;
import com.axelor.apps.stock.service.InventoryLineService;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class InventoryLineController {

  @Inject InventoryLineService inventoryLineService;

  public void updateInventoryLine(ActionRequest request, ActionResponse response)
      throws AxelorException {

    InventoryLine inventoryLine = request.getContext().asType(InventoryLine.class);
    Inventory inventory =
        request.getContext().getParent() != null
            ? request.getContext().getParent().asType(Inventory.class)
            : inventoryLine.getInventory();
    inventoryLine = inventoryLineService.updateInventoryLine(inventoryLine, inventory);
    response.setValue("rack", inventoryLine.getRack());
    response.setValue("currentQty", inventoryLine.getCurrentQty());
  }

  public void compute(ActionRequest request, ActionResponse response) {
    InventoryLine inventoryLine = request.getContext().asType(InventoryLine.class);
    Inventory inventory =
        request.getContext().getParent() != null
            ? request.getContext().getParent().asType(Inventory.class)
            : inventoryLine.getInventory();
    inventoryLine = inventoryLineService.compute(inventoryLine, inventory);
    response.setValue("gap", inventoryLine.getGap());
    response.setValue("gapValue", inventoryLine.getGapValue());
  }
}
