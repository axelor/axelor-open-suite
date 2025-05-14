/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.stock.db.repo;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.service.WeightedAveragePriceService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.util.Map;
import javax.persistence.PersistenceException;

public class StockLocationLineStockRepository extends StockLocationLineRepository {

  protected WeightedAveragePriceService weightedAveragePriceService;

  @Inject
  public StockLocationLineStockRepository(WeightedAveragePriceService weightedAveragePriceService) {
    this.weightedAveragePriceService = weightedAveragePriceService;
  }

  @Override
  public StockLocationLine save(StockLocationLine entity) {
    try {
      Product product = entity.getProduct();
      if (entity.getIsAvgPriceChanged()) {
        weightedAveragePriceService.computeAvgPriceForProduct(product);
      }
      return super.save(entity);
    } catch (Exception e) {
      TraceBackService.traceExceptionFromSaveMethod(e);
      throw new PersistenceException(e.getMessage(), e);
    }
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    json.put("$nbDecimalDigitForQty", Beans.get(AppBaseService.class).getNbDecimalDigitForQty());
    json.put(
        "$nbDecimalDigitForUnitPrice",
        Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice());

    return super.populate(json, context);
  }
}
