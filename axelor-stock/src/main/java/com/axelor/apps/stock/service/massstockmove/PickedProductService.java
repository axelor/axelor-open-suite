package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StoredProduct;
import java.math.BigDecimal;

public interface PickedProductService {

  StoredProduct createFromPickedProduct(PickedProduct pickedProduct) throws AxelorException;

  PickedProduct createPickedProduct(
      MassStockMove massStockMove, Product product, StockLocation stockLocation, BigDecimal qty);
}
