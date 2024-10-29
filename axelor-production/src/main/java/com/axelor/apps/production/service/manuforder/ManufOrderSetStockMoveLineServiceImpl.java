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
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ManufOrderSetStockMoveLineServiceImpl implements ManufOrderSetStockMoveLineService {

  protected ManufOrderGetStockMoveService manufOrderGetStockMoveService;

  @Inject
  public ManufOrderSetStockMoveLineServiceImpl(
      ManufOrderGetStockMoveService manufOrderGetStockMoveService) {
    this.manufOrderGetStockMoveService = manufOrderGetStockMoveService;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void setProducedStockMoveLineStockLocation(ManufOrder manufOrder) throws AxelorException {

    if (manufOrder.getProducedStockMoveLineList() != null) {
      StockMove stockMove =
          manufOrderGetStockMoveService.getProducedStockMoveFromManufOrder(manufOrder);

      for (StockMoveLine stockMoveLine : manufOrder.getProducedStockMoveLineList()) {
        if (stockMoveLine.getFromStockLocation() == null) {
          stockMoveLine.setFromStockLocation(stockMove.getFromStockLocation());
        }
        if (stockMoveLine.getToStockLocation() == null) {
          stockMoveLine.setToStockLocation(stockMove.getToStockLocation());
        }
      }
    }
  }

  @Override
  public void setResidualStockMoveLineStockLocation(ManufOrder manufOrder) throws AxelorException {

    if (manufOrder.getResidualStockMoveLineList() != null) {
      StockMove stockMove =
          manufOrderGetStockMoveService.getResidualStockMoveFromManufOrder(manufOrder);

      for (StockMoveLine stockMoveLine : manufOrder.getResidualStockMoveLineList()) {
        if (stockMoveLine.getFromStockLocation() == null) {
          stockMoveLine.setFromStockLocation(stockMove.getFromStockLocation());
        }
        if (stockMoveLine.getToStockLocation() == null) {
          stockMoveLine.setToStockLocation(stockMove.getToStockLocation());
        }
      }
    }
  }

  @Override
  public void setConsumedStockMoveLineStockLocation(ManufOrder manufOrder) throws AxelorException {

    if (manufOrder.getConsumedStockMoveLineList() != null) {
      StockMove stockMove =
          manufOrderGetStockMoveService.getConsumedStockMoveFromManufOrder(manufOrder);

      for (StockMoveLine stockMoveLine : manufOrder.getConsumedStockMoveLineList()) {
        if (stockMoveLine.getFromStockLocation() == null) {
          stockMoveLine.setFromStockLocation(stockMove.getFromStockLocation());
        }
        if (stockMoveLine.getToStockLocation() == null) {
          stockMoveLine.setToStockLocation(stockMove.getToStockLocation());
        }
      }
    }
  }
}
