package com.axelor.apps.production.rest.dto;

import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.tool.api.ObjectFinder;
import com.axelor.apps.tool.api.RequestStructure;
import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class ManufOrderProductPutRequest extends RequestStructure {

  @NotNull
  @Min(0)
  private Long stockMoveLineId;

  @NotNull
  @Min(0)
  private BigDecimal prodProductQty;

  public ManufOrderProductPutRequest() {};

  public long getStockMoveLineId() {
    return stockMoveLineId;
  }

  public void setStockMoveLineId(long stockMoveLineId) {
    this.stockMoveLineId = stockMoveLineId;
  }

  public BigDecimal getProdProductQty() {
    return prodProductQty;
  }

  public void setProdProductQty(BigDecimal prodProductQty) {
    this.prodProductQty = prodProductQty;
  }

  public StockMoveLine fetchStockMoveLine() {
    return ObjectFinder.find(StockMoveLine.class, stockMoveLineId, super.getVersion());
  }
}
