package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.tool.api.ResponseStructure;

public class StockInternalMoveResponse extends ResponseStructure {
  private final long id;
  private final int typeSelect;
  private final long fromLocationId;
  private final long toLocationId;

  public StockInternalMoveResponse(StockMove stockMove) {
    super(stockMove.getVersion());
    this.id = stockMove.getId();
    this.typeSelect = stockMove.getTypeSelect();
    this.fromLocationId = stockMove.getFromStockLocation().getId();
    this.toLocationId = stockMove.getToStockLocation().getId();
  }

  public long getId() {
    return id;
  }

  public int getTypeSelect() {
    return typeSelect;
  }

  public long getFromLocationId() {
    return fromLocationId;
  }

  public long getToLocationId() {
    return toLocationId;
  }
}
