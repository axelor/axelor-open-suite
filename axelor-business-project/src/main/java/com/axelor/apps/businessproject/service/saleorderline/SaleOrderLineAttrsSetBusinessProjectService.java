package com.axelor.apps.businessproject.service.saleorderline;

import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineAttrsSetBusinessProjectService {
  void setProjectTitle(Map<String, Map<String, Object>> attrsMap);

  void showDeliveryPanel(SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap);
}
