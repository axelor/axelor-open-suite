package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.interfaces.massstockmove.MassStockMovableProduct;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class MassStockMovableProductQuantityServiceImpl
    implements MassStockMovableProductQuantityService {

  protected final StockLocationLineService stockLocationLineService;

  @Inject
  public MassStockMovableProductQuantityServiceImpl(
      StockLocationLineService stockLocationLineService) {
    this.stockLocationLineService = stockLocationLineService;
  }

  @Override
  public BigDecimal getCurrentAvailableQty(
      MassStockMovableProduct movableProduct, StockLocation fromStockLocation)
      throws AxelorException {

    if (movableProduct.getTrackingNumber() != null) {
      return stockLocationLineService.getTrackingNumberAvailableQty(
          fromStockLocation, movableProduct.getTrackingNumber());
    }

    return stockLocationLineService.getAvailableQty(fromStockLocation, movableProduct.getProduct());
  }
}
