package com.axelor.apps.sale.rest.dto;

import java.math.BigDecimal;

public class PriceResponse {
  protected String type;
  protected BigDecimal price;

  public PriceResponse(String type, BigDecimal price) {
    this.type = type;
    this.price = price;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public String getType() {
    return type;
  }

  public BigDecimal getPrice() {
    return price;
  }
}
