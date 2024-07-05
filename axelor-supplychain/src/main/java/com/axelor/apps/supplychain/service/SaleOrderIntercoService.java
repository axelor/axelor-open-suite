package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import java.util.Map;

public interface SaleOrderIntercoService {
  Map<String, Object> getInterco(SaleOrder saleOrder) throws AxelorException;
}
