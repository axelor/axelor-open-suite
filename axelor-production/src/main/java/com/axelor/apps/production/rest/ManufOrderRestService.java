package com.axelor.apps.production.rest;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.exception.AxelorException;

public interface ManufOrderRestService {

  void updateStatusOfManufOrder(ManufOrder manufOrder, int targetStatus) throws AxelorException;
}
