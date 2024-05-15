package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineDomainAttrSetService {
  void setBillOfMaterialDomain(
      SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap);

  void setProdProcessDomain(SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap);

  String setProjectDomain(SaleOrder saleOrder);
}
