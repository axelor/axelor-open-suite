package com.axelor.apps.supplychain.service.saleorder;

import com.axelor.apps.sale.db.SaleOrder;
import java.util.Map;

public interface SaleOrderTaxNumberService {
  Map<String, Object> getTaxNumber(SaleOrder saleOrder);
}
