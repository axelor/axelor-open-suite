package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.StoredProduct;

public interface StoredProductAttrsService {
  String getStoredProductDomain(MassStockMove massStockMove);

  String getTrackingNumberDomain(StoredProduct storedProduct, MassStockMove massStockMove);
}
