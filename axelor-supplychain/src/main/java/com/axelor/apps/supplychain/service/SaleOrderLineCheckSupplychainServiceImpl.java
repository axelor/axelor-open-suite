/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.google.inject.Inject;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderLineCheckSupplychainServiceImpl
    implements SaleOrderLineCheckSupplychainService {

  protected StockMoveLineRepository stockMoveLineRepository;

  @Inject
  public SaleOrderLineCheckSupplychainServiceImpl(StockMoveLineRepository stockMoveLineRepository) {
    this.stockMoveLineRepository = stockMoveLineRepository;
  }

  @Override
  public void detachCanceledStockMoveLines(SaleOrderLine saleOrderLine) {
    List<StockMoveLine> stockMoveLineList =
        stockMoveLineRepository
            .all()
            .autoFlush(false)
            .filter(
                "self.saleOrderLine.id = :id AND (self.stockMove.statusSelect = :statusSelect OR self.plannedStockMove.statusSelect = :statusSelect)")
            .bind("id", saleOrderLine.getId())
            .bind("statusSelect", StockMoveRepository.STATUS_CANCELED)
            .fetch();
    if (!CollectionUtils.isEmpty(stockMoveLineList)) {
      for (StockMoveLine stockMoveLine : stockMoveLineList) {
        stockMoveLine.setSaleOrderLine(null);
      }
    }
  }
}
