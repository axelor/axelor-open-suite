package com.axelor.apps.sale.rest.dto;

import com.axelor.apps.base.db.Product;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import javax.validation.constraints.NotNull;

public class CartLinePostRequest extends RequestPostStructure {
  @NotNull Long productId;

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public Product fetchProduct() {
    if (productId == null || productId == 0L) {
      return null;
    }
    return ObjectFinder.find(Product.class, productId, ObjectFinder.NO_VERSION);
  }
}
