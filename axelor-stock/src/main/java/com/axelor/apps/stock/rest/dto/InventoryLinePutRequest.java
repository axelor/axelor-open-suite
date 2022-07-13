package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.tool.api.RequestStructure;
import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class InventoryLinePutRequest extends RequestStructure {

  @NotNull
  @Min(0)
  private BigDecimal realQty;

  private String description;

  public InventoryLinePutRequest() {}

  public BigDecimal getRealQty() {
    return realQty;
  }

  public void setRealQty(BigDecimal realQty) {
    this.realQty = realQty;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
