package com.axelor.apps.sale.service;

import com.axelor.apps.sale.db.SaleOrder;
import java.util.Map;

public interface SaleOrderGroupService {

  Map<String, Map<String, Object>> onChangeSaleOrderLine(SaleOrder saleOrder);
}
