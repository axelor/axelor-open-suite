/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.service;

import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.exception.AxelorException;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class StockMoveReservedQtyServiceImpl implements StockMoveReservedQtyService {

  protected ReservedQtyService reservedQtyService;

  @Inject
  public StockMoveReservedQtyServiceImpl(ReservedQtyService reservedQtyService) {
    this.reservedQtyService = reservedQtyService;
  }

  @Override
  public void allocateAll(StockMove stockMove) throws AxelorException {
    if (stockMove.getStockMoveLineList() == null) {
      return;
    }
    List<StockMoveLine> stockMoveLineToAllocateList =
        stockMove.getStockMoveLineList().stream()
            .filter(stockMoveLine -> stockMoveLine.getRealQty().signum() != 0)
            .collect(Collectors.toList());
    for (StockMoveLine stockMoveLine : stockMoveLineToAllocateList) {
      reservedQtyService.allocateAll(stockMoveLine);
    }
  }
}
