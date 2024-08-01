package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.StoredProduct;
import java.math.BigDecimal;

public class StoredProductProcessingCancelServiceImpl
    implements MassStockMovableProductProcessingCancelService<StoredProduct> {

  @Override
  public void preCancel(StoredProduct movableProduct) throws AxelorException {
    // Nothing
  }

  @Override
  public void postCancel(StoredProduct movableProduct) throws AxelorException {
    movableProduct.setStoredQty(BigDecimal.ZERO);
  }
}
