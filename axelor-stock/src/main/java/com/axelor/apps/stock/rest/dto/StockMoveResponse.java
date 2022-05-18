package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.tool.api.ResponseStructure;

public class StockMoveResponse extends ResponseStructure {
  private final long id;
  private final int typeSelect;
  private final long idFromLocation;
  private final long idToLocation;

  public StockMoveResponse(StockMove stockMove) {
    super(stockMove.getVersion());
    this.id = stockMove.getId();
    this.typeSelect = stockMove.getTypeSelect();
    this.idFromLocation = stockMove.getFromStockLocation().getId();
    this.idToLocation = stockMove.getToStockLocation().getId();
  }

  public String getObjectName() {
    return "stockMove";
  }

  public long getId() {
    return id;
  }

  public int getTypeSelect() {
    return typeSelect;
  }

  public long getIdFromLocation() {
    return idFromLocation;
  }

  public long getIdToLocation() {
    return idToLocation;
  }
}
