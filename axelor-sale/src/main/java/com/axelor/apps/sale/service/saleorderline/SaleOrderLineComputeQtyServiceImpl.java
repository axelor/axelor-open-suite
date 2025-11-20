package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineComputeQtyServiceImpl implements SaleOrderLineComputeQtyService {

  @Inject
  public SaleOrderLineComputeQtyServiceImpl() {}

  @Override
  public Map<String, Object> initQty(SaleOrderLine saleOrderLine) {
    Map<String, Object> values = new HashMap<>();
    saleOrderLine.setQty(BigDecimal.ONE);
    values.put("qty", saleOrderLine.getQty());
    return values;
  }
}
