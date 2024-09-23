package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.interfaces.massstockmove.MassStockMovableProduct;
import com.axelor.apps.stock.service.StockLocationLineFetchService;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class MassStockMovableProductQuantityServiceImpl
    implements MassStockMovableProductQuantityService {

  protected final StockLocationLineFetchService stockLocationLineFetchService;

  @Inject
  public MassStockMovableProductQuantityServiceImpl(
      StockLocationLineFetchService stockLocationLineFetchService) {
    this.stockLocationLineFetchService = stockLocationLineFetchService;
  }

  @Override
  public BigDecimal getCurrentAvailableQty(
      MassStockMovableProduct movableProduct, StockLocation fromStockLocation)
      throws AxelorException {

    if (movableProduct.getTrackingNumber() != null) {
      return stockLocationLineFetchService.getTrackingNumberAvailableQty(
          fromStockLocation, movableProduct.getTrackingNumber());
    }

    return stockLocationLineFetchService.getAvailableQty(
        fromStockLocation, movableProduct.getProduct());
  }
}
