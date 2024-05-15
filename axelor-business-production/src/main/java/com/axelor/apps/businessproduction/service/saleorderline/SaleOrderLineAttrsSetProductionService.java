package com.axelor.apps.businessproduction.service.saleorderline;

import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineAttrsSetProductionService {

  void hideBillOfMaterialAndProdProcess(
      SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap);
}
