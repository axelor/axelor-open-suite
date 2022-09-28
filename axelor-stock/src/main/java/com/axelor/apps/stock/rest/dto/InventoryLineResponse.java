package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.stock.db.InventoryLine;
import com.axelor.apps.tool.api.ResponseStructure;

public class InventoryLineResponse extends ResponseStructure {

  private final Long inventoryLineId;
  private final Long inventoryId;

  public InventoryLineResponse(InventoryLine inventoryLine) {
    super(inventoryLine.getVersion());
    this.inventoryLineId = inventoryLine.getId();
    this.inventoryId = inventoryLine.getInventory().getId();
  }

  public Long getInventoryLineId() {
    return inventoryLineId;
  }

  public Long getInventoryId() {
    return inventoryId;
  }
}
