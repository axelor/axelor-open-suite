package com.axelor.apps.stock.rest.dto;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.tool.api.ResponseStructure;
import java.util.List;

public class StockProductVariantResponse extends ResponseStructure {

  private final Long productId;
  private final List<StockProductVariantAttributeResponse> attributes;

  public StockProductVariantResponse(
      Product product, List<StockProductVariantAttributeResponse> attributes) {
    super(product.getVersion());
    this.productId = product.getId();
    this.attributes = attributes;
  }

  public Long getProductId() {
    return productId;
  }

  public List<StockProductVariantAttributeResponse> getAttributes() {
    return attributes;
  }
}
