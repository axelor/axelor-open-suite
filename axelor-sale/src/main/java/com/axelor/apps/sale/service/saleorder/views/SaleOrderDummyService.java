package com.axelor.apps.sale.service.saleorder.views;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import java.util.Map;

public interface SaleOrderDummyService {
  Map<String, Object> getOnNewDummies(SaleOrder saleOrder) throws AxelorException;
}
