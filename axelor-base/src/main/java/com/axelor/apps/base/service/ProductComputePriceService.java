package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;

public interface ProductComputePriceService {
  BigDecimal computeSalePrice(
      BigDecimal managePriceCoef, BigDecimal costPrice, Product product, Company company)
      throws AxelorException;
}
