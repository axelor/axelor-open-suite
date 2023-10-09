package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;
import java.util.Map;

public interface SaleOrderLineCalculationComboService {

  Map<String, BigDecimal> computePriceAndRelatedFields(SaleOrderLine saleOrderLine)
      throws AxelorException;
}
