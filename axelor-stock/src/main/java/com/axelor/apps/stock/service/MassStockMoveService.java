package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.MassStockMove;

public interface MassStockMoveService {

  public void realizePicking(MassStockMove massStockMove);

  public void realizeStorage(MassStockMove massStockMove);

  public void cancelPicking(MassStockMove massStockMove);

  public void cancelStorage(MassStockMove massStockMove);

  public void importProductFromStockLocation(MassStockMove massStockMove) throws AxelorException;

  public void setStatusSelectToDraft(MassStockMove massStockMove);

  public String getSequence(MassStockMove massStockMoveToSet) throws AxelorException;
}
