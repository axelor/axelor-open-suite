package com.axelor.apps.sale.rest.dto;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestPostStructure;
import javax.validation.constraints.NotNull;

public class ProductResquest extends RequestPostStructure {
  @NotNull private Long productId;

  private Long unitId;

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public Long getUnitId() {
    return unitId;
  }

  public void setUnitId(Long unitId) {
    this.unitId = unitId;
  }

  public Product fetchProduct() {
    if (productId == null || productId == 0L) {
      return null;
    }
    return ObjectFinder.find(Product.class, productId, ObjectFinder.NO_VERSION);
  }

  public Unit fetchUnit() {
    if (unitId == null || unitId == 0L) {
      return null;
    }
    return ObjectFinder.find(Unit.class, unitId, ObjectFinder.NO_VERSION);
  }
}
