package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.MassStockMove;

public interface MassStockMoveSequenceService {
  public String getSequence(MassStockMove massStockMoveToSet) throws AxelorException;
}
