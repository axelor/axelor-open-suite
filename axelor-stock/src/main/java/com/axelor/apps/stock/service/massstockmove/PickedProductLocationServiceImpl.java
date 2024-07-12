package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.StockLocation;
import java.util.Objects;

public class PickedProductLocationServiceImpl
    implements MassStockMovableProductLocationService<PickedProduct> {

  @Override
  public StockLocation getFromStockLocation(PickedProduct movableProduct) {
    Objects.requireNonNull(movableProduct);

    return movableProduct.getStockLocation();
  }

  @Override
  public StockLocation getToStockLocation(PickedProduct movableProduct) {
    Objects.requireNonNull(movableProduct);
    return movableProduct.getMassStockMove().getCartStockLocation();
  }
}
