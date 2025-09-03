/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.stock.db.repo;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.InventoryLine;
import com.axelor.apps.stock.service.InventoryLineService;
import com.axelor.inject.Beans;
import java.util.Map;

public class InventoryLineManagementRepository extends InventoryLineRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {

    if (context.get("_model") != null
        && context.get("_model").toString().equals(InventoryLine.class.getName())) {

      Long id = (Long) json.get("id");
      if (id != null) {
        InventoryLine inventoryLine = find(id);
        json.put(
            "isPresentInStockLocation",
            inventoryLine != null
                && Beans.get(InventoryLineService.class).isPresentInStockLocation(inventoryLine));
      } else {
        json.put("isPresentInStockLocation", false);
      }

      json.put(
          "nbDecimalDigitForUnitPrice",
          Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice());
    }
    return super.populate(json, context);
  }
}
