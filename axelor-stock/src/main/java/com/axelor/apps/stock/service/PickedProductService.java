package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.*;
import java.math.BigDecimal;

public interface PickedProductService {
  public void createStockMoveAndStockMoveLine(
      MassStockMove massStockMove, PickedProduct pickedProduct) throws AxelorException;

  public void cancelStockMoveAndStockMoveLine(
      MassStockMove massStockMove, PickedProduct pickedProduct) throws AxelorException;

  public PickedProduct createPickedProduct(
      MassStockMove massStockMove,
      Product pickedProduct,
      TrackingNumber trackingNumber,
      StockLocation fromStockLocation,
      BigDecimal currentQty,
      BigDecimal pickedQty,
      StockMoveLine stockMoveLine);
}
