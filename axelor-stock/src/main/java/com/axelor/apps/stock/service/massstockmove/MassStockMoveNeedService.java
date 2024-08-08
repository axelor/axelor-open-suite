package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.MassStockMoveNeed;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;

public interface MassStockMoveNeedService {
  void createMassStockMoveNeedFromStockMoveLinesId(
      MassStockMove massStockMove, List<Long> stockMoveLinesIdList);

  @Transactional(rollbackOn = Exception.class)
  MassStockMoveNeed createMassStockMoveNeed(
      MassStockMove massStockMove, Product product, BigDecimal qtyToMove);
}
