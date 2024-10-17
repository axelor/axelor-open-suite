package com.axelor.apps.production.service;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class ProdProductServiceImpl implements ProdProductService {

  // TODO add conversion unit
  @Override
  public BigDecimal computeQuantity(List<ProdProduct> prodProductList) {

    BigDecimal qty = BigDecimal.ZERO;

    if (prodProductList != null) {

      for (ProdProduct prodProduct : prodProductList) {

        qty = qty.add(prodProduct.getQty());
      }
    }

    return qty;
  }

  @Override
  public boolean existInFinishedProduct(ManufOrder manufOrder, ProdProduct prodProduct) {
    Objects.requireNonNull(manufOrder);
    Objects.requireNonNull(prodProduct);

    if (manufOrder.getProducedStockMoveLineList() != null) {
      return manufOrder.getProducedStockMoveLineList().stream()
          .anyMatch(
              stockMoveLine ->
                  stockMoveLine.getProduct().equals(prodProduct.getProduct())
                      && stockMoveLine
                          .getTrackingNumber()
                          .equals(prodProduct.getWasteProductTrackingNumber()));
    }
    return false;
  }
}
