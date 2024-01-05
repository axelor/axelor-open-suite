package com.axelor.apps.stock.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.MassStockMoveNeed;
import com.axelor.apps.stock.db.repo.MassStockMoveNeedRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class MassStockMoveNeedServiceImpl implements MassStockMoveNeedService {
  protected MassStockMoveNeedRepository massStockMoveNeedRepository;

  @Inject
  public MassStockMoveNeedServiceImpl(MassStockMoveNeedRepository massStockMoveNeedRepository) {
    this.massStockMoveNeedRepository = massStockMoveNeedRepository;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public MassStockMoveNeed createMassStockMoveNeed(
      MassStockMove massStockMove, Product product, BigDecimal qtyToMove) {
    MassStockMoveNeed massStockMoveNeed = new MassStockMoveNeed();
    massStockMoveNeed.setProductToMove(product);
    massStockMoveNeed.setQtyToMove(qtyToMove);
    massStockMoveNeed.setMassStockMove(massStockMove);
    massStockMoveNeedRepository.save(massStockMoveNeed);
    return massStockMoveNeed;
  }
}
