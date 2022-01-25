package com.axelor.apps.sale.service.saleorder.model;

import java.util.Map;

public class SaleOrderMergeControlResponse {

  private boolean isDiff = false;
  private String message;

  private Map<String, SaleOrderMergeObject> commonMap;

  public boolean isDiff() {
    return isDiff;
  }

  public void setDiff(boolean isDiff) {
    this.isDiff = isDiff;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Map<String, SaleOrderMergeObject> getCommonMap() {
    return commonMap;
  }

  public void setCommonMap(Map<String, SaleOrderMergeObject> map) {
    this.commonMap = map;
  }
}
