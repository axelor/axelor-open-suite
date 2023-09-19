package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.StoredProduct;
import com.axelor.apps.stock.db.TrackingNumber;
import java.math.BigDecimal;

public interface StoredProductService {

  public void createStockMoveAndStockMoveLine(StoredProduct storedProduct) throws AxelorException;

  public void cancelStockMoveAndStockMoveLine(StoredProduct storedProduct) throws AxelorException;

  public StoredProduct createStoredProduct(
      Product product,
      TrackingNumber trackingNumber,
      BigDecimal currentQty,
      StockLocation toStockLocation,
      BigDecimal storedQty,
      StockMoveLine stockMoveLine,
      MassStockMove massStockMove,
      PickedProduct pickedProduct,
      Unit unit);
}
