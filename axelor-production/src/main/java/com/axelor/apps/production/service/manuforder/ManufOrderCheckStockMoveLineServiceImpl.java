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
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.i18n.I18n;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ManufOrderCheckStockMoveLineServiceImpl
    implements ManufOrderCheckStockMoveLineService {

  @Override
  public void checkConsumedStockMoveLineList(ManufOrder manufOrder, ManufOrder oldManufOrder)
      throws AxelorException {
    checkRealizedStockMoveLineList(
        manufOrder.getConsumedStockMoveLineList(), oldManufOrder.getConsumedStockMoveLineList());
  }

  @Override
  public void checkProducedStockMoveLineList(ManufOrder manufOrder, ManufOrder oldManufOrder)
      throws AxelorException {
    checkRealizedStockMoveLineList(
        manufOrder.getProducedStockMoveLineList(), oldManufOrder.getProducedStockMoveLineList());
  }

  @Override
  public void checkResidualStockMoveLineList(ManufOrder manufOrder, ManufOrder oldManufOrder)
      throws AxelorException {
    checkRealizedStockMoveLineList(
        manufOrder.getResidualStockMoveLineList(), oldManufOrder.getResidualStockMoveLineList());
  }

  @Override
  public void checkRealizedStockMoveLineList(
      List<StockMoveLine> stockMoveLineList, List<StockMoveLine> oldStockMoveLineList)
      throws AxelorException {

    List<StockMoveLine> realizedProducedStockMoveLineList =
        stockMoveLineList.stream()
            .filter(
                stockMoveLine ->
                    stockMoveLine.getStockMove() != null
                        && stockMoveLine.getStockMove().getStatusSelect()
                            == StockMoveRepository.STATUS_REALIZED)
            .sorted(Comparator.comparingLong(StockMoveLine::getId))
            .collect(Collectors.toList());
    List<StockMoveLine> oldRealizedProducedStockMoveLineList =
        oldStockMoveLineList.stream()
            .filter(
                stockMoveLine ->
                    stockMoveLine.getStockMove() != null
                        && stockMoveLine.getStockMove().getStatusSelect()
                            == StockMoveRepository.STATUS_REALIZED)
            .sorted(Comparator.comparingLong(StockMoveLine::getId))
            .collect(Collectors.toList());

    // the two lists must be equal
    if (!realizedProducedStockMoveLineList.equals(oldRealizedProducedStockMoveLineList)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.CANNOT_DELETE_REALIZED_STOCK_MOVE_LINES));
    }
  }
}
