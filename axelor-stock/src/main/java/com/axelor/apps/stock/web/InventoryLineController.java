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
package com.axelor.apps.stock.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.InventoryLine;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.service.InventoryLineService;
import com.axelor.apps.stock.service.InventoryService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.StringTool;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class InventoryLineController {

  public void updateInventoryLine(ActionRequest request, ActionResponse response)
      throws AxelorException {

    InventoryLine inventoryLine = request.getContext().asType(InventoryLine.class);
    Inventory inventory =
        request.getContext().getParent() != null
            ? request.getContext().getParent().asType(Inventory.class)
            : inventoryLine.getInventory();
    inventoryLine =
        Beans.get(InventoryLineService.class).updateInventoryLine(inventoryLine, inventory);
    response.setValue("rack", inventoryLine.getRack());
    response.setValue("currentQty", inventoryLine.getCurrentQty());
  }

  public void compute(ActionRequest request, ActionResponse response) {
    try {
      InventoryLine inventoryLine = request.getContext().asType(InventoryLine.class);
      Inventory inventory =
          request.getContext().getParent() != null
              ? request.getContext().getParent().asType(Inventory.class)
              : inventoryLine.getInventory();
      inventoryLine = Beans.get(InventoryLineService.class).compute(inventoryLine, inventory);
      response.setValue("unit", inventoryLine.getUnit());
      response.setValue("gap", inventoryLine.getGap());
      response.setValue("gapValue", inventoryLine.getGapValue());
      response.setValue("realValue", inventoryLine.getRealValue());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setStockLocationDomain(ActionRequest request, ActionResponse response) {
    try {
      InventoryLine inventoryLine = request.getContext().asType(InventoryLine.class);
      Inventory inventory =
          request.getContext().getParent() != null
              ? request.getContext().getParent().asType(Inventory.class)
              : inventoryLine.getInventory();

      if (inventory != null && inventory.getStockLocation() != null) {
        Set<StockLocation> stockLocationSet = new HashSet<StockLocation>();
        stockLocationSet.add(inventory.getStockLocation());
        stockLocationSet = Beans.get(InventoryService.class).getStockLocations(stockLocationSet);
        response.setAttr(
            "stockLocation",
            "domain",
            "self.id IN(" + StringTool.getIdListString(stockLocationSet) + ")");
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateCurrentQty(ActionRequest request, ActionResponse response) {
    try {
      InventoryLine inventoryLine = request.getContext().asType(InventoryLine.class);
      BigDecimal updatedCurrentQty =
          Beans.get(InventoryLineService.class)
              .getCurrentQty(inventoryLine.getStockLocation(), inventoryLine.getProduct());
      response.setValue("currentQty", updatedCurrentQty);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
