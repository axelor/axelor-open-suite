package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.stock.db.StockCorrection;
import com.axelor.apps.tool.api.ResponseStructure;

public class StockCorrectionResponse extends ResponseStructure {

  private final Long id;
  private final Long productId;
  private final int realQty;

  public StockCorrectionResponse(StockCorrection stockCorrection) {
    super(stockCorrection.getVersion());
    this.id = stockCorrection.getId();
    this.productId = stockCorrection.getProduct().getId();
    this.realQty = stockCorrection.getRealQty().intValue();
  }

  public Long getId() {
    return id;
  }

  public Long getProductId() {
    return productId;
  }

  public int getRealQty() {
    return realQty;
  }
}
