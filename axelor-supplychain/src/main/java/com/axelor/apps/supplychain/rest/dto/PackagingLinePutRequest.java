package com.axelor.apps.supplychain.rest.dto;

import com.axelor.utils.api.RequestStructure;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

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
