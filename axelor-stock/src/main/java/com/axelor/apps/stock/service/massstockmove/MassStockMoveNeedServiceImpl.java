package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.MassStockMoveNeed;
import com.axelor.apps.stock.db.repo.MassStockMoveNeedRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class MassStockMoveNeedServiceImpl implements MassStockMoveNeedService {

  protected final StockMoveLineRepository stockMoveLineRepository;
  protected final MassStockMoveNeedRepository massStockMoveNeedRepository;

  @Inject
  public MassStockMoveNeedServiceImpl(
      StockMoveLineRepository stockMoveLineRepository,
      MassStockMoveNeedRepository massStockMoveNeedRepository) {
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.massStockMoveNeedRepository = massStockMoveNeedRepository;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void createMassStockMoveNeedFromStockMoveLinesId(
      MassStockMove massStockMove, List<Long> stockMoveLinesToAdd) {
    Objects.requireNonNull(massStockMove);
    Objects.requireNonNull(stockMoveLinesToAdd);

    for (Long stockMoveLineId : stockMoveLinesToAdd) {
      var stockMoveLine = stockMoveLineRepository.find(stockMoveLineId);
      if (stockMoveLine != null) {
        var massStockMoveNeed =
            this.createMassStockMoveNeed(
                massStockMove, stockMoveLine.getProduct(), stockMoveLine.getRealQty());
        massStockMove.addProductToMoveListItem(massStockMoveNeed);
      }
    }
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public MassStockMoveNeed createMassStockMoveNeed(
      MassStockMove massStockMove, Product product, BigDecimal qtyToMove) {
    Objects.requireNonNull(massStockMove);
    Objects.requireNonNull(product);
    Objects.requireNonNull(qtyToMove);

    var massStockMoveNeed = new MassStockMoveNeed();
    massStockMoveNeed.setProductToMove(product);
    massStockMoveNeed.setQtyToMove(qtyToMove);
    massStockMoveNeed.setMassStockMove(massStockMove);

    return massStockMoveNeedRepository.save(massStockMoveNeed);
  }
}
