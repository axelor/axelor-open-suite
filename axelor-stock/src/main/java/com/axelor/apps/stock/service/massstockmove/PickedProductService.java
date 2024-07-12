package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.PickedProduct;
import com.axelor.apps.stock.db.StoredProduct;

public interface PickedProductService {

  StoredProduct createFromPickedProduct(PickedProduct pickedProduct) throws AxelorException;
}
