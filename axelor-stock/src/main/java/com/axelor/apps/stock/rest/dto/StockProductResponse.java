package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.tool.api.ApiStructure;

public class StockProductResponse implements ApiStructure {

  private final long id;
  private final String name;
  private final String code;
  private final String description;

  public StockProductResponse(Product product) {
    this.id = product.getId();
    this.name = product.getName();
    this.code = product.getCode();
    this.description = product.getDescription();
  }

  @Override
  public String getObjectName() {
    return "product";
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }
}
