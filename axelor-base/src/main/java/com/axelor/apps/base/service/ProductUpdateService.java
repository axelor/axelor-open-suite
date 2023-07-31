package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;

public interface ProductUpdateService {

  void updateCostPriceFromView(Product product) throws AxelorException;
}
