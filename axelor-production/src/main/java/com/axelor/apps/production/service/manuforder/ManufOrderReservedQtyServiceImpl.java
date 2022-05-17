/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.service.ReservedQtyService;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ManufOrderReservedQtyServiceImpl implements ManufOrderReservedQtyService {

  protected ReservedQtyService reservedQtyService;

  @Inject
  public ManufOrderReservedQtyServiceImpl(ReservedQtyService reservedQtyService) {
    this.reservedQtyService = reservedQtyService;
  }

  @Override
  public void allocateAll(ManufOrder manufOrder) throws AxelorException {
    for (StockMoveLine stockMoveLine : getPlannedConsumedStockMoveLines(manufOrder)) {
      reservedQtyService.allocateAll(stockMoveLine);
    }
  }

  @Override
  public void deallocateAll(ManufOrder manufOrder) throws AxelorException {
    for (StockMoveLine stockMoveLine : getPlannedConsumedStockMoveLines(manufOrder)) {
      reservedQtyService.updateReservedQty(stockMoveLine, BigDecimal.ZERO);
    }
  }

  @Override
  public void reserveAll(ManufOrder manufOrder) throws AxelorException {
    for (StockMoveLine stockMoveLine : getPlannedConsumedStockMoveLines(manufOrder)) {
      reservedQtyService.requestQty(stockMoveLine);
    }
  }

  @Override
  public void cancelReservation(ManufOrder manufOrder) throws AxelorException {
    for (StockMoveLine stockMoveLine : getPlannedConsumedStockMoveLines(manufOrder)) {
      reservedQtyService.cancelReservation(stockMoveLine);
    }
  }

  /**
   * Fetch planned stock move lines for given manufacturing order, handling the case of managing the
   * consumed stock move lines by operation. This method never returns null.
   *
   * @param manufOrder non null manufacturing order
   * @return found planned stock move or an empty list.
   */
  protected List<StockMoveLine> getPlannedConsumedStockMoveLines(ManufOrder manufOrder) {
    Stream<StockMoveLine> consumedStockMoveLineStream;

    if (manufOrder.getIsConsProOnOperation()) {
      List<OperationOrder> operationOrderList = manufOrder.getOperationOrderList();
      if (operationOrderList == null) {
        return new ArrayList<>();
      }
      consumedStockMoveLineStream =
          operationOrderList.stream()
              .map(OperationOrder::getConsumedStockMoveLineList)
              .filter(Objects::nonNull)
              .flatMap(Collection::stream);
    } else {
      if (manufOrder.getConsumedStockMoveLineList() == null) {
        return new ArrayList<>();
      }
      consumedStockMoveLineStream = manufOrder.getConsumedStockMoveLineList().stream();
    }
    return consumedStockMoveLineStream
        .filter(
            stockMoveLine ->
                stockMoveLine.getStockMove() != null
                    && stockMoveLine.getStockMove().getStatusSelect()
                        == StockMoveRepository.STATUS_PLANNED)
        .collect(Collectors.toList());
  }
}
