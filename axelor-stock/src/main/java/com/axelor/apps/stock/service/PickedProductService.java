package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.MassStockMoveNeed;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
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

  public void createPickedProductFromMassStockMoveNeed(MassStockMoveNeed massStockMoveNeed);

  public void createPickedProductFromStockLocationLine(
      StockLocationLine stockLocationLine, Product product, MassStockMove massStockMove);
}
