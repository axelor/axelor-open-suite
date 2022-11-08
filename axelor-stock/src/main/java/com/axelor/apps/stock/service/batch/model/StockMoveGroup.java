package com.axelor.apps.stock.service.batch.model;

import java.time.LocalDate;
import java.util.Objects;

public class StockMoveGroup {

  private final LocalDate realDate;
  private final Long idFromStockLocation;
  private final Long idToStockLocation;
  private final int statusSelect;

  public StockMoveGroup(
      LocalDate realDate, Long fromStockLocation, Long toStockLocation, int statusSelect) {
    this.realDate = realDate;
    this.idFromStockLocation = Objects.requireNonNull(fromStockLocation);
    this.idToStockLocation = Objects.requireNonNull(toStockLocation);
    this.statusSelect = statusSelect;
  }

  public LocalDate getRealDate() {
    return realDate;
  }

  public Long getFromStockLocation() {
    return idFromStockLocation;
  }

  public Long getToStockLocation() {
    return idToStockLocation;
  }

  public int getStatusSelect() {
    return statusSelect;
  }
}
