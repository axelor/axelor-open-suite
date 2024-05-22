package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;
import java.util.Map;

public interface SaleOrderLineRecordUpdateService {

  void setCompanyCurrencyValue(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap);

  void setCurrencyValue(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, Map<String, Map<String, Object>> attrsMap);

  void initDummyFields(Map<String, Map<String, Object>> attrsMap);

  void setNonNegotiableValue(SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap);

  void setInitialQty(Map<String, Map<String, Object>> attrsMap, BigDecimal qty);
}
