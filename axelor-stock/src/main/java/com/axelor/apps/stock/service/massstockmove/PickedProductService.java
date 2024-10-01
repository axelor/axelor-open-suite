package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StoredProduct;
import com.axelor.apps.stock.db.TrackingNumber;
import java.math.BigDecimal;
import java.util.List;

public interface PickedProductService {

  StoredProduct createFromPickedProduct(PickedProduct pickedProduct) throws AxelorException;

  PickedProduct createPickedProduct(
      MassStockMove massStockMove,
      Product product,
      StockLocation stockLocation,
      BigDecimal qty,
      TrackingNumber trackingNumber);

  List<PickedProduct> generatePickedProductsFromStockLocation(
      MassStockMove massStockMove, StockLocation stockLocation) throws AxelorException;
}
