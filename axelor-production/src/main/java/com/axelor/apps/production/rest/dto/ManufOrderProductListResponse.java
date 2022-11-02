package com.axelor.apps.production.rest.dto;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.tool.api.ResponseStructure;
import java.util.List;

public class ManufOrderProductListResponse extends ResponseStructure {

  private final List<ManufOrderProductResponse> productList;

  public ManufOrderProductListResponse(
      List<ManufOrderProductResponse> productList, ManufOrder manufOrder) {
    super(manufOrder.getVersion());
    this.productList = productList;
  }

  public List<ManufOrderProductResponse> getProductList() {
    return productList;
  }
}
