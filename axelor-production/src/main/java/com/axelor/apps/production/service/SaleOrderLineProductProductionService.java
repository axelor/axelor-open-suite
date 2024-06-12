package com.axelor.apps.production.service;

import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineProductProductionService {
  Map<String, Object> setBillOfMaterial(SaleOrderLine saleOrderLine);
}
