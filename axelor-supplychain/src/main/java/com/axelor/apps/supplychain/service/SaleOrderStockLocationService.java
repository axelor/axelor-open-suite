package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import java.util.Map;

public interface SaleOrderStockLocationService {
  Map<String, Object> getStockLocation(SaleOrder saleOrder) throws AxelorException;

  Map<String, Object> getToStockLocation(SaleOrder saleOrder) throws AxelorException;
}
