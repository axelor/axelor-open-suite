package com.axelor.apps.production.rest.dto;

import com.axelor.apps.tool.api.RequestStructure;
import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class WastedProductPutRequest extends RequestStructure {

  @NotNull
  @Min(0)
  private BigDecimal qty;

  public WastedProductPutRequest() {}

  public BigDecimal getQty() {
    return qty;
  }

  public void setQty(BigDecimal qty) {
    this.qty = qty;
  }
}
