package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import java.math.BigDecimal;

public interface SaleOrderDiscountService {
  void applyGlobalDiscountOnLines(SaleOrder saleOrder) throws AxelorException;

  BigDecimal computeDiscountFixedEquivalence(SaleOrder saleOrder);

  BigDecimal computeDiscountPercentageEquivalence(SaleOrder saleOrder);
}
