package com.axelor.apps.supplychain.rest.dto;

import com.axelor.utils.api.RequestStructure;
import java.math.BigDecimal;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class PackagingLinePutRequest extends RequestStructure {

  @NotNull
  @Min(0)
  private BigDecimal quantity;

  public BigDecimal getQuantity() {
    return quantity;
  }

  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }
}
