/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.service.StockMoveService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class ManufOrderUpdateStockMoveServiceImpl implements ManufOrderUpdateStockMoveService {

  protected ManufOrderGetStockMoveService manufOrderGetStockMoveService;
  protected ManufOrderService manufOrderService;
  protected StockMoveService stockMoveService;

  @Inject
  public ManufOrderUpdateStockMoveServiceImpl(
      ManufOrderGetStockMoveService manufOrderGetStockMoveService,
      ManufOrderService manufOrderService,
      StockMoveService stockMoveService) {
    this.manufOrderGetStockMoveService = manufOrderGetStockMoveService;
    this.manufOrderService = manufOrderService;
    this.stockMoveService = stockMoveService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateConsumedStockMoveFromManufOrder(ManufOrder manufOrder) throws AxelorException {
    manufOrderService.updateDiffProdProductList(manufOrder);
    List<StockMoveLine> consumedStockMoveLineList = manufOrder.getConsumedStockMoveLineList();
    if (consumedStockMoveLineList == null) {
      return;
    }
    updateStockMoveFromManufOrder(
        consumedStockMoveLineList,
        manufOrderGetStockMoveService.getConsumedStockMoveFromManufOrder(manufOrder));
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateProducedStockMoveFromManufOrder(ManufOrder manufOrder) throws AxelorException {
    List<StockMoveLine> producedStockMoveLineList = manufOrder.getProducedStockMoveLineList();
    if (producedStockMoveLineList == null) {
      return;
    }
    updateStockMoveFromManufOrder(
        producedStockMoveLineList,
        manufOrderGetStockMoveService.getProducedStockMoveFromManufOrder(manufOrder));
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateResidualStockMoveFromManufOrder(ManufOrder manufOrder) throws AxelorException {
    List<StockMoveLine> residualStockMoveLineList = manufOrder.getResidualStockMoveLineList();
    if (residualStockMoveLineList == null) {
      return;
    }
    updateStockMoveFromManufOrder(
        residualStockMoveLineList,
        manufOrderGetStockMoveService.getResidualStockMoveFromManufOrder(manufOrder));
  }

  @Override
  public void updateStockMoveFromManufOrder(
      List<StockMoveLine> stockMoveLineList, StockMove stockMove) throws AxelorException {
    if (stockMoveLineList == null) {
      return;
    }

    // add missing lines in stock move
    stockMoveLineList.stream()
        .filter(stockMoveLine -> stockMoveLine.getStockMove() == null)
        .forEach(stockMove::addStockMoveLineListItem);

    // remove lines in stock move removed in manuf order
    if (stockMove.getStockMoveLineList() != null) {
      stockMove
          .getStockMoveLineList()
          .removeIf(stockMoveLine -> !stockMoveLineList.contains(stockMoveLine));
    }
    // update stock location by cancelling then planning stock move.
    stockMoveService.cancel(stockMove);
    stockMoveService.goBackToDraft(stockMove);
    stockMoveService.plan(stockMove);
  }
}
