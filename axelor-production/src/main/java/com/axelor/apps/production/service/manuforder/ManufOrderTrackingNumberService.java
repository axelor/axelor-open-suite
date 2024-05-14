package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.production.db.ManufOrder;

public interface ManufOrderTrackingNumberService {
  void setParentTrackingNumbers(ManufOrder manufOrder);
}
