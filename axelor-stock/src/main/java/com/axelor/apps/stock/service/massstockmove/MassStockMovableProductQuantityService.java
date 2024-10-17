package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.interfaces.massstockmove.MassStockMovableProduct;
import java.math.BigDecimal;

public interface MassStockMovableProductQuantityService {

  BigDecimal getCurrentAvailableQty(
      MassStockMovableProduct movableProduct, StockLocation fromStockLocation)
      throws AxelorException;
}
