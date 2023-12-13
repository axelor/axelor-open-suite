package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.stock.db.MassStockMove;

public interface MassStockMoveService {
  public String getAndSetSequence(Company company, MassStockMove massStockMoveToSet)
      throws AxelorException;
}
