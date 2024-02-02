package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ManufOrder;

public interface ManufOrderCreatePurchaseOrderService {
  void createPurchaseOrders(ManufOrder manufOrder) throws AxelorException;
}
