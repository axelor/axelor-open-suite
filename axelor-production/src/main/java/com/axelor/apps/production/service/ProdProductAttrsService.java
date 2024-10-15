package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;

public interface ProdProductAttrsService {

  String getTrackingNumberDomain(ManufOrder manufOrder, ProdProduct prodProduct)
      throws AxelorException;
}
