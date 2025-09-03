/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.stock.service.massstockmove;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.MassStockMove;
import com.axelor.apps.stock.db.MassStockMoveNeed;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.MassStockMoveNeedRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MassStockMoveNeedServiceImpl implements MassStockMoveNeedService {

  protected final StockMoveLineRepository stockMoveLineRepository;
  protected final MassStockMoveNeedRepository massStockMoveNeedRepository;
  protected final StockLocationLineRepository stockLocationLineRepository;

  @Inject
  public MassStockMoveNeedServiceImpl(
      StockMoveLineRepository stockMoveLineRepository,
      MassStockMoveNeedRepository massStockMoveNeedRepository,
      StockLocationLineRepository stockLocationLineRepository) {
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.massStockMoveNeedRepository = massStockMoveNeedRepository;
    this.stockLocationLineRepository = stockLocationLineRepository;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void createMassStockMoveNeedFromStockMoveLinesId(
      MassStockMove massStockMove, List<Long> stockMoveLinesToAdd) {
    Objects.requireNonNull(massStockMove);
    Objects.requireNonNull(stockMoveLinesToAdd);

    Map<Product, BigDecimal> mapStockMoveNeed =
        stockMoveLinesToAdd.stream()
            .map(stockMoveLineRepository::find)
            .filter(Objects::nonNull)
            .collect(
                Collectors.toMap(
                    StockMoveLine::getProduct, StockMoveLine::getRealQty, BigDecimal::add));

    mapStockMoveNeed.forEach(
        (product, qty) -> {
          this.merge(massStockMove, product, qty);
        });
  }

  protected void merge(MassStockMove massStockMove, Product product, BigDecimal qty) {
    if (massStockMove.getMassStockMoveNeedList() != null) {
      var moveNeedWithSameProductOpt =
          massStockMove.getMassStockMoveNeedList().stream()
              .filter(moveNeed -> moveNeed.getProductToMove().equals(product))
              .findAny();
      if (moveNeedWithSameProductOpt.isPresent()) {
        var moveNeedWithSameProduct = moveNeedWithSameProductOpt.get();
        moveNeedWithSameProduct.setQtyToMove(moveNeedWithSameProduct.getQtyToMove().add(qty));
      } else {
        massStockMove.addMassStockMoveNeedListItem(
            this.createMassStockMoveNeed(massStockMove, product, qty));
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
