package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineDummyService {
  Map<String, Object> getOnNewDummies(SaleOrderLine saleOrderLine, SaleOrder saleOrder);

  Map<String, Object> getOnLoadDummies(SaleOrderLine saleOrderLine, SaleOrder saleOrder);
}
