package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.db.ProdResidualProduct;

public class ManufOrderResidualProductServiceImpl implements ManufOrderResidualProductService {

  @Override
  public boolean hasResidualProduct(ManufOrder manufOrder) {

    return manufOrder.getToProduceProdProductList().stream()
        .anyMatch(prodProduct -> isResidualProduct(prodProduct, manufOrder));
  }

  @Override
  public boolean isResidualProduct(ProdProduct prodProduct, ManufOrder manufOrder) {
    if (manufOrder.getBillOfMaterial() != null
        && manufOrder.getBillOfMaterial().getProdResidualProductList() != null) {
      return manufOrder.getBillOfMaterial().getProdResidualProductList().stream()
          .map(ProdResidualProduct::getProduct)
          .anyMatch(product -> product.equals(prodProduct.getProduct()));
    }
    return false;
  }
}
