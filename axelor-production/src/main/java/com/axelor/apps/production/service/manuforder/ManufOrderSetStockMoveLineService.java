package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ManufOrder;

public interface ManufOrderSetStockMoveLineService {
  void setProducedStockMoveLineStockLocation(ManufOrder manufOrder) throws AxelorException;

  void setResidualStockMoveLineStockLocation(ManufOrder manufOrder) throws AxelorException;

  void setConsumedStockMoveLineStockLocation(ManufOrder manufOrder) throws AxelorException;
}
