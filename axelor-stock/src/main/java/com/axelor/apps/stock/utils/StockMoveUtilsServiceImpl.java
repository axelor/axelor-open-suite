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
package com.axelor.apps.stock.utils;

import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.db.JPA;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockMoveUtilsServiceImpl implements StockMoveUtilsService {

  @Override
  public List<Map<String, Object>> getStockPerDate(
      Long locationId, Long productId, LocalDate fromDate, LocalDate toDate) {

    List<Map<String, Object>> stock = new ArrayList<>();

    while (!fromDate.isAfter(toDate)) {
      Double qty = getStock(locationId, productId, fromDate);
      Map<String, Object> dateStock = new HashMap<>();
      dateStock.put("$date", fromDate);
      dateStock.put("$qty", new BigDecimal(qty));
      stock.add(dateStock);
      fromDate = fromDate.plusDays(1);
    }
    return stock;
  }

  protected Double getStock(Long locationId, Long productId, LocalDate date) {
    List<StockMoveLine> inLines =
        JPA.all(StockMoveLine.class)
            .filter(
                "self.product.id = ?1 AND self.toStockLocation.id = ?2 AND self.stockMove.statusSelect != ?3 AND (self.stockMove.estimatedDate <= ?4 OR self.stockMove.realDate <= ?4)",
                productId,
                locationId,
                StockMoveRepository.STATUS_CANCELED,
                date)
            .fetch();

    List<StockMoveLine> outLines =
        JPA.all(StockMoveLine.class)
            .filter(
                "self.product.id = ?1 AND self.fromStockLocation.id = ?2 AND self.stockMove.statusSelect != ?3 AND (self.stockMove.estimatedDate <= ?4 OR self.stockMove.realDate <= ?4)",
                productId,
                locationId,
                StockMoveRepository.STATUS_CANCELED,
                date)
            .fetch();

    Double inQty =
        inLines.stream().mapToDouble(inl -> Double.parseDouble(inl.getQty().toString())).sum();

    Double outQty =
        outLines.stream().mapToDouble(out -> Double.parseDouble(out.getQty().toString())).sum();

    Double qty = inQty - outQty;

    return qty;
  }
}
