package com.axelor.apps.sale.rest.dto;

import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.ResponseStructure;
import java.math.BigDecimal;

public class PriceResponse extends ResponseStructure {
  protected String type;
  protected BigDecimal price;

  public PriceResponse(String type, BigDecimal price) {
    super(ObjectFinder.NO_VERSION);
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
