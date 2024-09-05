package com.axelor.apps.production.service;

import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineViewProductionService {
  Map<String, Map<String, Object>> hideBomAndProdProcess(SaleOrderLine saleOrderLine);

  Map<String, Map<String, Object>> getSolDetailsScale();
}
