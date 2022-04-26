package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.stock.db.StockCorrection;
import com.axelor.apps.tool.api.ApiStructure;

public class StockCorrectionResponse implements ApiStructure {

  private final Long id;
  private final Long idProduct;
  private final int realQty;

  public StockCorrectionResponse(StockCorrection stockCorrection) {
    this.id = stockCorrection.getId();
    this.idProduct = stockCorrection.getProduct().getId();
    this.realQty = stockCorrection.getRealQty().intValue();
  }

  @Override
  public String getObjectName() {
    return "stockCorrection";
  }

  public Long getId() {
    return id;
  }

  public Long getIdProduct() {
    return idProduct;
  }

  public int getRealQty() {
    return realQty;
  }
}
