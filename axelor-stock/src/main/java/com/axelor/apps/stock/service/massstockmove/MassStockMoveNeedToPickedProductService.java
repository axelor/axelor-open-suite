package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.MassStockMoveNeed;
import java.util.List;

public interface MassStockMoveNeedToPickedProductService {

  void generatePickedProductsFromMassStockMoveNeedList(
      MassStockMove massStockMove, List<MassStockMoveNeed> massStockMoveNeedList)
      throws AxelorException;

  void generatePickedProductFromMassStockMoveNeed(MassStockMoveNeed massStockMoveNeed)
      throws AxelorException;
}
