package com.axelor.apps.production.rest.dto;

import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.tool.api.ResponseStructure;

public class WastedProductResponse extends ResponseStructure {

  private Long prodProductId;

  public WastedProductResponse(ProdProduct prodProduct) {
    super(prodProduct.getVersion());
    this.prodProductId = prodProduct.getId();
  }

  public Long getProdProductId() {
    return prodProductId;
  }
}
