package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.PickedProduct;

public interface PickedProductAttrsService {
  String getStockLocationDomain(PickedProduct pickedProduct, MassStockMove massStockMove);
}
