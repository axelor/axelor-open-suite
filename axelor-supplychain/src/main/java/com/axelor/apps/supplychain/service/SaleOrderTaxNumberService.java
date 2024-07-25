package com.axelor.apps.supplychain.service;

import com.axelor.apps.sale.db.SaleOrder;
import java.util.Map;

public interface SaleOrderTaxNumberService {
  Map<String, Object> getTaxNumber(SaleOrder saleOrder);
}
