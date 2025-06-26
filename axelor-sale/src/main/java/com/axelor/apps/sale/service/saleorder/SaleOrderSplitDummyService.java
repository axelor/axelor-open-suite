package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.sale.db.SaleOrder;
import java.util.List;
import java.util.Map;

public interface SaleOrderSplitDummyService {
  List<Map<String, Object>> getSaleOrderLineMapList(SaleOrder saleOrder);
}
