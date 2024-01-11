package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.MassStockMove;

public interface MassStockMoveService {
  public String getSequence(MassStockMove massStockMoveToSet) throws AxelorException;

  public void importProductFromStockLocation(MassStockMove massStockMove) throws AxelorException;

  public void realizePicking(MassStockMove massStockMove);

  public int cancelPicking(MassStockMove massStockMove);

  public void setStatusSelectToDraft(MassStockMove massStockMove);

  public void realizeStorage(MassStockMove massStockMove);

  public void cancelStorage(MassStockMove massStockMove);
}
