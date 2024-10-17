package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.stock.db.PickedProduct;

public interface PickedProductAttrsService {
  String getPickedProductDomain(PickedProduct pickedProduct);

  String getTrackingNumberDomain(PickedProduct pickedProduct);
}
