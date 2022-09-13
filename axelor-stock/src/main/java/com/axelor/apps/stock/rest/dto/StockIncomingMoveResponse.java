package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.tool.api.ResponseStructure;

public class StockIncomingMoveResponse extends ResponseStructure {
  private final long id;
  private final int typeSelect;
  private final long fromAddressId;
  private final long toStockLocationId;

  public StockIncomingMoveResponse(StockMove stockMove) {
    super(stockMove.getVersion());
    this.id = stockMove.getId();
    this.typeSelect = stockMove.getTypeSelect();
    this.fromAddressId = stockMove.getFromAddress().getId();
    this.toStockLocationId = stockMove.getToStockLocation().getId();
  }

  public long getId() {
    return id;
  }

  public int getTypeSelect() {
    return typeSelect;
  }

  public long getFromAddressId() {
    return fromAddressId;
  }

  public long getToStockLocationId() {
    return toStockLocationId;
  }
}
