package com.axelor.apps.production.rest.dto;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.tool.api.ResponseStructure;
import java.util.List;

public class ConsumedProductListResponse extends ResponseStructure {

  private final List<ConsumedProductResponse> consumedProductList;

  public ConsumedProductListResponse(
      List<ConsumedProductResponse> consumedProductList, ManufOrder manufOrder) {
    super(manufOrder.getVersion());
    this.consumedProductList = consumedProductList;
  }

  public List<ConsumedProductResponse> getConsumedProductList() {
    return consumedProductList;
  }
}
