package com.axelor.apps.stock.rest.dto;

import java.math.BigDecimal;

public class StockProductVariantAttributeResponse {

  private final String attrName;
  private final String attrValue;
  private final BigDecimal priceExtra;
  private final int applicationPriceSelect;

  public StockProductVariantAttributeResponse(
      String attrName, String attrValue, BigDecimal priceExtra, int applicationPriceSelect) {
    this.attrName = attrName;
    this.attrValue = attrValue;
    this.priceExtra = priceExtra;
    this.applicationPriceSelect = applicationPriceSelect;
  }

  public String getAttrName() {
    return attrName;
  }

  public String getAttrValue() {
    return attrValue;
  }

  public BigDecimal getPriceExtra() {
    return priceExtra;
  }

  public int getApplicationPriceSelect() {
    return applicationPriceSelect;
  }
}
