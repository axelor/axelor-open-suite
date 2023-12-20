package com.axelor.apps.sale.service.saleorder.attributes;

import com.axelor.apps.sale.db.SaleOrder;
import java.util.Map;

public interface SaleOrderAttrsService {

  void setSaleOrderLineScale(SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap);

  void setSaleOrderLineTaxScale(SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap);

  void addIncotermRequired(SaleOrder saleOrder, Map<String, Map<String, Object>> attrsMap);

  Map<String, Map<String, Object>> onChangeSaleOrderLine(SaleOrder saleOrder);
}
