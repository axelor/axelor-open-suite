package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ManufOrder;

public interface ManufOrderOperationOrderService {

  void preFillOperations(ManufOrder manufOrder) throws AxelorException;

  void updateOperationsName(ManufOrder manufOrder);
}
