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

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SaleOrderReservedQtyServiceImpl implements SaleOrderReservedQtyService {

  protected ReservedQtyService reservedQtyService;
  protected StockMoveLineRepository stockMoveLineRepository;

  @Inject
  public SaleOrderReservedQtyServiceImpl(
      ReservedQtyService reservedQtyService, StockMoveLineRepository stockMoveLineRepository) {
    this.reservedQtyService = reservedQtyService;
    this.stockMoveLineRepository = stockMoveLineRepository;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void allocateAll(SaleOrder saleOrder) throws AxelorException {
    for (SaleOrderLine saleOrderLine : getNonDeliveredLines(saleOrder)) {
      reservedQtyService.allocateAll(saleOrderLine);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void deallocateAll(SaleOrder saleOrder) throws AxelorException {
    for (SaleOrderLine saleOrderLine : getNonDeliveredLines(saleOrder)) {
      reservedQtyService.updateReservedQty(saleOrderLine, BigDecimal.ZERO);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void reserveAll(SaleOrder saleOrder) throws AxelorException {
    for (SaleOrderLine saleOrderLine : getNonDeliveredLines(saleOrder)) {
      reservedQtyService.requestQty(saleOrderLine);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void cancelReservation(SaleOrder saleOrder) throws AxelorException {
    for (SaleOrderLine saleOrderLine : getNonDeliveredLines(saleOrder)) {
      reservedQtyService.cancelReservation(saleOrderLine);
    }
  }

  protected List<SaleOrderLine> getNonDeliveredLines(SaleOrder saleOrder) {
    List<SaleOrderLine> saleOrderLineList =
        saleOrder.getSaleOrderLineList() == null
            ? new ArrayList<>()
            : saleOrder.getSaleOrderLineList();
    return saleOrderLineList
        .stream()
        .filter(saleOrderLine -> getPlannedStockMoveLine(saleOrderLine) != null)
        .collect(Collectors.toList());
  }

  protected StockMoveLine getPlannedStockMoveLine(SaleOrderLine saleOrderLine) {
    return stockMoveLineRepository
        .all()
        .filter(
            "self.saleOrderLine = :saleOrderLine " + "AND self.stockMove.statusSelect = :planned")
        .bind("saleOrderLine", saleOrderLine)
        .bind("planned", StockMoveRepository.STATUS_PLANNED)
        .fetchOne();
  }
}
