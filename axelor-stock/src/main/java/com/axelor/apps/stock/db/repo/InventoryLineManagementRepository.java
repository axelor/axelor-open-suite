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
