package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StoredProduct;
import java.util.Objects;

public class StoredProductLocationServiceImpl
    implements MassStockMovableProductLocationService<StoredProduct> {

  @Override
  public StockLocation getFromStockLocation(StoredProduct movableProduct) {
    Objects.requireNonNull(movableProduct);

    return movableProduct.getMassStockMove().getCartStockLocation();
  }

  @Override
  public StockLocation getToStockLocation(StoredProduct movableProduct) {
    Objects.requireNonNull(movableProduct);
    return movableProduct.getStockLocation();
  }
}
