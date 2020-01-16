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
package com.axelor.apps.stock.db.repo;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductBaseRepository;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ProductStockRepository extends ProductBaseRepository {

  @Inject private StockMoveService stockMoveService;

  @Inject private StockLocationRepository stockLocationRepo;

  @Inject private StockLocationLineService stockLocationLineService;

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {

    this.setAvailableQty(json, context);

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

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void setAvailableQty(Map<String, Object> json, Map<String, Object> context) {
    try {
      Long productId = (Long) json.get("id");
      Product product = find(productId);

      if (context.get("_parent") != null) {
        Map<String, Object> _parent = (Map<String, Object>) context.get("_parent");

        StockLocation stockLocation = null;
        if (context.get("_model").toString().equals("com.axelor.apps.stock.db.StockMoveLine")) {
          if (_parent.get("fromStockLocation") != null) {
            stockLocation =
                stockLocationRepo.find(
                    Long.parseLong(((Map) _parent.get("fromStockLocation")).get("id").toString()));
          }
        } else {
          if (_parent.get("stockLocation") != null) {
            stockLocation =
                stockLocationRepo.find(
                    Long.parseLong(((Map) _parent.get("stockLocation")).get("id").toString()));
          }
        }

        if (stockLocation != null) {
          BigDecimal availableQty =
              stockLocationLineService.getAvailableQty(stockLocation, product);

          json.put("$availableQty", availableQty);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public Product copy(Product product, boolean deep) {
    Product copy = super.copy(product, deep);
    copy.setAvgPrice(BigDecimal.ZERO);
    return copy;
  }
}
