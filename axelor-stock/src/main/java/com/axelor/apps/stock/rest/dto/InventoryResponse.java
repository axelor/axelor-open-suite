package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.tool.api.ResponseStructure;

public class InventoryResponse extends ResponseStructure {

  private final Long inventoryId;

  public InventoryResponse(Inventory inventory) {
    super(inventory.getVersion());
    this.inventoryId = inventory.getId();
  }

  public Long getInventoryId() {
    return inventoryId;
  }
}
