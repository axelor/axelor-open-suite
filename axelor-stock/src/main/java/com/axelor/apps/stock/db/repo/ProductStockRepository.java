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
package com.axelor.apps.stock.db.repo;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductBaseRepository;
import com.axelor.apps.stock.service.StockMoveService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ProductStockRepository extends ProductBaseRepository {

  @Inject private StockMoveService stockMoveService;

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    if (!context.containsKey("fromStockWizard")) {
      return json;
    }
    try {
      Long productId = (Long) json.get("id");
      Long locationId = Long.parseLong(context.get("locationId").toString());
      LocalDate fromDate = LocalDate.parse(context.get("stockFromDate").toString());
      LocalDate toDate = LocalDate.parse(context.get("stockToDate").toString());
      List<Map<String, Object>> stock =
          stockMoveService.getStockPerDate(locationId, productId, fromDate, toDate);

      if (stock != null && !stock.isEmpty()) {
        LocalDate minDate = null;
        LocalDate maxDate = null;
        BigDecimal minQty = BigDecimal.ZERO;
        BigDecimal maxQty = BigDecimal.ZERO;
        for (Map<String, Object> dateStock : stock) {
          LocalDate date = (LocalDate) dateStock.get("$date");
          BigDecimal qty = (BigDecimal) dateStock.get("$qty");
          if (minDate == null
              || qty.compareTo(minQty) < 0
              || qty.compareTo(minQty) == 0 && date.isAfter(minDate)) {
            minDate = date;
            minQty = qty;
          }
          if (maxDate == null
              || qty.compareTo(maxQty) > 0
              || qty.compareTo(maxQty) == 0 && date.isBefore(maxDate)) {
            maxDate = date;
            maxQty = qty;
          }
        }
        json.put("$stockMinDate", minDate);
        json.put("$stockMin", minQty);
        json.put("$stockMaxDate", maxDate);
        json.put("$stockMax", maxQty);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return json;
  }

  @Override
  public Product copy(Product product, boolean deep) {
    Product copy = super.copy(product, deep);
    copy.setAvgPrice(BigDecimal.ZERO);
    return copy;
  }
}
