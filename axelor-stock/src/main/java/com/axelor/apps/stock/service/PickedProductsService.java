package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.MassMove;
import com.axelor.apps.stock.db.PickedProducts;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import java.math.BigDecimal;

public interface PickedProductsService {

  public PickedProducts createPickedProductMobility(
      MassMove massMove,
      Product pickedProduct,
      TrackingNumber trackingNumber,
      StockLocation fromStockLocation,
      BigDecimal currentQty,
      BigDecimal pickedQty,
      StockMoveLine stockMoveLine);

  public void updatePickedProductMobility(
      MassMove massMove,
      Long pickedProductId,
      Product pickedProduct,
      TrackingNumber trackingNumber,
      StockLocation fromStockLocation,
      BigDecimal currentQty,
      BigDecimal pickedQty,
      StockMoveLine stockMoveLine);

  public void duplicateProduct(PickedProducts pickedProducts);

  public void createStockMoveAndStockMoveLine(MassMove massMove, PickedProducts pickedProducts)
      throws AxelorException;

  public void cancelStockMoveAndStockMoveLine(MassMove massMove, PickedProducts pickedProducts);

  public PickedProducts createPickedProduct(
      MassMove massMove,
      Product pickedProduct,
      TrackingNumber trackingNumber,
      StockLocation fromStockLocation,
      BigDecimal currentQty,
      BigDecimal pickedQty,
      StockMoveLine stockMoveLine);
}
