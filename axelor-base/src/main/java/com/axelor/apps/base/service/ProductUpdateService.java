package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Product;
import com.axelor.exception.AxelorException;

public interface ProductUpdateService {

  void updateCostPriceFromView(Product product) throws AxelorException;
}
