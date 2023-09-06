package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.stock.db.MassStockMove;

public interface MassStockMoveService {

  public void importProductFromStockLocation(MassStockMove massStockMove) throws AxelorException;

  public void setStatusSelectToDraft(MassStockMove massStockMove);

  public String getAndSetSequence(Company company, MassStockMove massStockMoveToSet)
      throws AxelorException;
}
