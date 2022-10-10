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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.stock.db.StockHistoryLine;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.service.StockHistoryServiceImpl;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

public class StockHistoryServiceSupplyChainImpl extends StockHistoryServiceImpl {

  @Inject
  public StockHistoryServiceSupplyChainImpl(
      StockMoveLineRepository stockMoveLineRepository,
      UnitConversionService unitConversionService,
      StockLocationRepository stockLocationRepository) {
    super(stockMoveLineRepository, unitConversionService, stockLocationRepository);
  }

  @Override
  protected void fillOutgoingStockHistoryLineFields(
      StockHistoryLine stockHistoryLine, List<StockMoveLine> stockMoveLineList)
      throws AxelorException {

    super.fillOutgoingStockHistoryLineFields(stockHistoryLine, stockMoveLineList);

    BigDecimal sumOutQtyPeriod = BigDecimal.ZERO;
    BigDecimal sumOneoffSaleOutQtyPeriod = BigDecimal.ZERO;
    BigDecimal qtyConverted = BigDecimal.ZERO;
    for (StockMoveLine stockMoveLine : stockMoveLineList) {
      qtyConverted =
          unitConversionService.convert(
              stockMoveLine.getUnit(),
              stockMoveLine.getProduct().getUnit(),
              stockMoveLine.getRealQty(),
              stockMoveLine.getRealQty().scale(),
              stockMoveLine.getProduct());
      if (stockMoveLine.getSaleOrderLine() != null
          && stockMoveLine.getSaleOrderLine().getSaleOrder().getOneoffSale()) {
        sumOneoffSaleOutQtyPeriod = sumOneoffSaleOutQtyPeriod.add(qtyConverted);
      } else {
        sumOutQtyPeriod = sumOutQtyPeriod.add(qtyConverted);
      }
    }
    stockHistoryLine.setSumOutQtyPeriod(sumOutQtyPeriod);
    stockHistoryLine.setSumOneoffSaleOutQtyPeriod(sumOneoffSaleOutQtyPeriod);
  }
}
