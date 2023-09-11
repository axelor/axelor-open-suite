package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.PickedProducts;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import java.math.BigDecimal;

public interface PickedProductsService {

  public void createStockMoveAndStockMoveLine(
      MassStockMove massStockMove, PickedProducts pickedProducts) throws AxelorException;

  public void cancelStockMoveAndStockMoveLine(
      MassStockMove massStockMove, PickedProducts pickedProducts) throws AxelorException;

  public PickedProducts createPickedProduct(
      MassStockMove massStockMove,
      Product pickedProduct,
      TrackingNumber trackingNumber,
      StockLocation fromStockLocation,
      BigDecimal currentQty,
      BigDecimal pickedQty,
      StockMoveLine stockMoveLine);
}
