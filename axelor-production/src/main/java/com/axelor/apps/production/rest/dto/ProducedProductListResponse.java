package com.axelor.apps.production.rest.dto;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.tool.api.ResponseStructure;
import java.util.List;

public class ProducedProductListResponse extends ResponseStructure {

  private final List<ProducedProductResponse> producedProductList;

  public ProducedProductListResponse(
      List<ProducedProductResponse> producedProductList, ManufOrder manufOrder) {
    super(manufOrder.getVersion());
    this.producedProductList = producedProductList;
  }

  public List<ProducedProductResponse> getProducedProductList() {
    return producedProductList;
  }
}
