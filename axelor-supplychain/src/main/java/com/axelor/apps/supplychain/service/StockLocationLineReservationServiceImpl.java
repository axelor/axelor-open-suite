/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;

public class StockLocationLineReservationServiceImpl
    implements StockLocationLineReservationService {

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void allocateAll(StockLocationLine stockLocationLine) throws AxelorException {
    // qty to allocate is the minimum value between requested and current qty subtracted by reserved
    // qty
    BigDecimal qtyToAllocate =
        stockLocationLine
            .getRequestedReservedQty()
            .min(stockLocationLine.getCurrentQty())
            .subtract(stockLocationLine.getReservedQty());
    BigDecimal qtyAllocated =
        Beans.get(ReservedQtyService.class)
            .allocateReservedQuantityInSaleOrderLines(
                qtyToAllocate,
                stockLocationLine.getStockLocation(),
                stockLocationLine.getProduct(),
                stockLocationLine.getUnit());
    stockLocationLine.setReservedQty(stockLocationLine.getReservedQty().add(qtyAllocated));
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, RuntimeException.class})
  public void deallocateAll(StockLocationLine stockLocationLine) throws AxelorException {
    List<StockMoveLine> stockMoveLineList =
        Beans.get(StockMoveLineRepository.class)
            .all()
            .filter(
                "self.product = :product "
                    + "AND self.stockMove.fromStockLocation = :stockLocation "
                    + "AND self.stockMove.statusSelect = :planned "
                    + "AND (self.stockMove.availabilityRequest IS FALSE "
                    + "OR self.stockMove.availabilityRequest IS NULL) "
                    + "AND self.reservedQty > 0")
            .bind("product", stockLocationLine.getProduct())
            .bind("stockLocation", stockLocationLine.getStockLocation())
            .bind("planned", StockMoveRepository.STATUS_PLANNED)
            .fetch();
    for (StockMoveLine stockMoveLine : stockMoveLineList) {
      stockMoveLine.setReservedQty(BigDecimal.ZERO);
      SaleOrderLine saleOrderLine = stockMoveLine.getSaleOrderLine();
      if (saleOrderLine != null) {
        saleOrderLine.setReservedQty(BigDecimal.ZERO);
      }
    }
    Beans.get(ReservedQtyService.class).updateReservedQty(stockLocationLine);
  }
}
