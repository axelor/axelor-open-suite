package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.MassMove;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.StoredProducts;
import com.axelor.apps.stock.db.TrackingNumber;
import java.math.BigDecimal;

public interface StoredProductsService {
  public void createStockMoveAndStockMoveLine(StoredProducts storedProducts) throws AxelorException;

  public void cancelStockMoveAndStockMoveLine(StoredProducts storedProducts) throws AxelorException;

  public StoredProducts updateStoreProductMobility(
      Product product,
      TrackingNumber trackingNumber,
      BigDecimal currentQty,
      Long storedProductId,
      StockLocation toStockLocation,
      BigDecimal storedQty,
      StockMoveLine stockMoveLine,
      MassMove massMove);

  public StoredProducts createStoredProductMobility(
      Product product,
      TrackingNumber trackingNumber,
      BigDecimal currentQty,
      StockLocation toStockLocation,
      BigDecimal storedQty,
      StockMoveLine stockMoveLine,
      MassMove massMove);

  public StoredProducts createStoredProduct(
      Product product,
      TrackingNumber trackingNumber,
      BigDecimal currentQty,
      StockLocation toStockLocation,
      BigDecimal storedQty,
      StockMoveLine stockMoveLine,
      MassMove massMove);
}
