package com.axelor.apps.stock.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.MassStockMoveNeed;
import java.math.BigDecimal;

public interface MassStockMoveNeedService {

  public MassStockMoveNeed createMassStockMoveNeed(
      MassStockMove massStockMove, Product product, BigDecimal qtyToMove);
}
