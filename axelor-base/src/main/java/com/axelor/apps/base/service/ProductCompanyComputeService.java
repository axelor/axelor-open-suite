package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Product;
import com.axelor.exception.AxelorException;

public interface ProductCompanyComputeService {
  void updateProductCompanySalePriceWithSalesUnit(Product product) throws AxelorException;
}
