package com.axelor.apps.stock.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.MassStockMove;
import java.util.List;

public interface MassStockMoveService {

  public void realizePicking(MassStockMove massStockMove);

  public void realizeStorage(MassStockMove massStockMove);

  public int cancelPicking(MassStockMove massStockMove);

  public void cancelStorage(MassStockMove massStockMove);

  public void importProductFromStockLocation(MassStockMove massStockMove) throws AxelorException;

  public void setStatusSelectToDraft(MassStockMove massStockMove);

  public String getSequence(MassStockMove massStockMoveToSet) throws AxelorException;

  public void useStockMoveLinesIdsToCreateMassStockMoveNeeds(
      MassStockMove massStockMove, List<Long> stockMoveLinesToAdd);

  public void clearProductToMoveList(MassStockMove massStockMove);
}
