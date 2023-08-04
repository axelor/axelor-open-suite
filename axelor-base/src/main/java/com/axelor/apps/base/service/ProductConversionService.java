package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import java.math.BigDecimal;

public interface ProductConversionService {

  BigDecimal convertFromPurchaseToStockUnitPrice(Product product, BigDecimal lastPurchasePrice)
      throws AxelorException;
}
