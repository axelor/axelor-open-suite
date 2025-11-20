package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineComputeQtyService {
  Map<String, Object> initQty(SaleOrderLine saleOrderLine);
}
